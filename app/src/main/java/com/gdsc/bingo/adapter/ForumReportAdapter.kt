package com.gdsc.bingo.adapter


import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.ComponentCardReportBinding
import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.model.User
import com.gdsc.bingo.services.points.Points
import com.gdsc.bingo.services.points.PointsRewardSystem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.time.ZoneId
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ForumReportAdapter(
    private val context: Context,
    private val storage: FirebaseStorage,
    var actionVerticalButton : (Forums) -> Unit = {},
    var actionOpenDetail : (Forums) -> Unit = {},
    var actionOpenRoute : (Forums) -> Unit = {}
) : ListAdapter<Forums, ForumReportAdapter.ForumPostViewHolder>(ForumsPostDiffUtil()) {
    class ForumsPostDiffUtil : DiffUtil.ItemCallback<Forums>() {
        override fun areItemsTheSame(oldItem: Forums, newItem: Forums): Boolean {
            return oldItem.referencePath == newItem.referencePath
        }

        override fun areContentsTheSame(oldItem: Forums, newItem: Forums): Boolean {
            return oldItem.toFirebaseModel().toString() == newItem.toFirebaseModel().toString()
        }

    }

    inner class ForumPostViewHolder(
        private val binding : ComponentCardReportBinding
    ) : RecyclerView.ViewHolder(binding.root), PointsRewardSystem {
        fun bind(forums: Forums) {
            Log.i("ForumReportAdapter", "Bind data to viewholder: \n\t${forums}")

            startAllShimmer()

            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.Main) {
                    setupPostTimestamp(forums.createdAt!!)
                    setupPostTitle(forums.title!!)
                    setupVerticalButton(forums)
                    setupOpenDetail(forums)
                    setupAddress(forums.vicinity)
                    setupActionRoute(forums)
                }
                withContext(Dispatchers.IO) {
                    setupProfile(forums.author!!)
                    setupThumbnail(forums.thumbnailPhotosUrl)
                }
            }
        }

        private fun setupActionRoute(forums: Forums) {
            binding.componentButtonNavigate.setOnClickListener {
                actionOpenRoute(forums)
            }
        }

        private fun setupAddress(vicinity: GeoPoint?) {
            CoroutineScope(Dispatchers.Main).launch {
                if (vicinity == null) {
                    binding.componentCardReportShimmerAddress.apply {
                        stopShimmer()
                        visibility = View.GONE
                    }
                    return@launch
                }


                val geoCoderAddressResult : String? = withContext(Dispatchers.IO) {
                    suspendCoroutine { continuation ->
                        this@launch.launch (Dispatchers.IO) {
                            withTimeoutOrNull(5000L) {
                                try {
                                    val geocoder = Geocoder(context, Locale.getDefault())
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        geocoder.getFromLocation(
                                            vicinity.latitude,
                                            vicinity.longitude,
                                            1
                                        ) { addresses ->
                                            if (addresses.isNotEmpty()) {
                                                val address = addresses[0].getAddressLine(0)
                                                Log.i("ForumReportAdapter", "geoCoder: $address")

                                                continuation.resume(address)
                                            } else {
                                                continuation.resume(null)
                                            }
                                        }


                                    } else {
                                        val addresses = withContext(Dispatchers.IO) {
                                            geocoder.getFromLocation(
                                                vicinity.latitude,
                                                vicinity.longitude,
                                                1
                                            )
                                        }

                                        if (addresses != null) {
                                            if (addresses.isNotEmpty()) {
                                                val address = addresses[0].getAddressLine(0)
                                                Log.i("ForumReportAdapter", "geoCoder: $address")

                                                continuation.resume(address)
                                            } else {
                                                continuation.resume(null)
                                            }
                                        } else {
                                            continuation.resume(null)
                                        }
                                    }
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                    continuation.resume(null)
                                }
                            }.let { if (it == null) continuation.resume(null) }
                        }
                    }
                }



                binding.componentCardReportTextViewAddress.text = if (geoCoderAddressResult == null)
                        "Lokasi Koordinat ${vicinity.latitude}, ${vicinity.longitude}"
                    else geoCoderAddressResult

                binding.componentCardReportShimmerAddress.apply {
                    stopShimmer()
                    visibility = View.GONE
                }
            }
        }

        private fun startAllShimmer() {
            with(binding) {
                componentCardReportLinearLayoutShimmerTextContainer.startShimmer()
                componentCardReportShimmerTitle.startShimmer()
                componentCardReportShimmerAddress.startShimmer()
            }
        }

        private fun setupOpenDetail(forums: Forums) {
            binding.componentCardReportContainer.setOnClickListener {
                actionOpenDetail(forums)
                forums.logClickAction("setupOpenDetail")
            }
        }

        private fun setupVerticalButton(forums: Forums) {
            binding.componentCardReportButtonMore.setOnClickListener {
                actionVerticalButton(forums)
                forums.logClickAction("setupVerticalButton")
            }
        }


        private fun setupPostTitle(title: String) {
            binding.componentCardReportTextViewTitle.text = title
            binding.componentCardReportShimmerTitle.apply {
                stopShimmer()
                visibility = View.GONE
            }
        }

        /**
         * [setupThumbnail]
         *
         *
         * Tidak semua post memiliki thumbnail, jika tidak ada thumbnail maka view akan dihilangkan
         */
        private fun setupThumbnail(thumbnailPhotosUrl: String?) {
            CoroutineScope(Dispatchers.Main).launch {
                binding.componentCardReportImageViewPost.visibility = if (thumbnailPhotosUrl == null) View.GONE else View.VISIBLE
                if (thumbnailPhotosUrl == null) return@launch


                storage.getReference(thumbnailPhotosUrl).downloadUrl.addOnSuccessListener { thumbnail ->
                    binding.componentCardReportImageViewPost.load(thumbnail)
                }.addOnFailureListener {
                    Log.e("ForumReportAdapter", "Error loading thumbnail", it)
                }

            }




        }



        private fun setupProfile(authorReference: DocumentReference) {
            CoroutineScope(Dispatchers.Main).launch {
                val user = withContext(Dispatchers.IO) {
                    val snapshot = authorReference.get().await()
                    User().toModel(snapshot)
                }

                if (user != null) {
                    loadUsername(user.username!!)
                    loadProfilePicture(user.profilePicturePath)
                } else {
                    Log.e("ForumReportAdapter", "User data is null")
                }
            }
        }

        private fun loadProfilePicture(profilePicturePath: String?) {
            if (profilePicturePath == null) {
                Log.e("ForumReportAdapter", "Profile picture path is null")
                binding.componentCardReportCardProfilImage.load(R.drawable.ic_person_24) {
                    transformations(CircleCropTransformation())
                }
                return
            }

            Log.i("ForumReportAdapter", "Profile picture path : $profilePicturePath")

            storage.getReference(profilePicturePath).downloadUrl.addOnSuccessListener {
                Log.i("ForumReportAdapter", "Profile picture loaded : $it")
                binding.componentCardReportCardProfilImage.load(it) {
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.ic_person_24)
                    error(R.drawable.ic_person_24)
                }
            }.addOnFailureListener {
                Log.e("ForumReportAdapter", "Error getting profile picture", it)
            }
        }

        private fun loadUsername(username: String) {
            binding.componentCardReportTextViewName.text = username
        }

        private fun setupPostTimestamp(createdAt: Timestamp) {
            val date = createdAt.toDate()

            val zonedDateTime = date.toInstant().atZone(ZoneId.systemDefault())
            val month = "${zonedDateTime.month}".lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            val text = "${zonedDateTime.dayOfMonth} $month ${zonedDateTime.year}, ${zonedDateTime.hour}:${zonedDateTime.minute}"
            binding.componentCardReportTextViewDateTime.text = text

            binding.componentCardReportLinearLayoutShimmerTextContainer.apply {
                stopShimmer()
                visibility = View.GONE
            }

        }

        override fun likePointRewards(user: DocumentReference, author: DocumentReference) {
            author.get(Source.SERVER)
                .addOnSuccessListener { userDocument ->
                    val newScore = userDocument.get(User.Keys.score) as Long + Points.RECEIVED_LIKE
                    userDocument.reference.update(User.Keys.score, newScore)
                }

            user.get(Source.SERVER)
                .addOnSuccessListener { userDocument ->
                    val newScore = userDocument.get(User.Keys.score) as Long + Points.DO_LIKE
                    userDocument.reference.update(User.Keys.score, newScore)
                }
        }

        override fun unlikePointRewards(user: DocumentReference, author: DocumentReference) {
            Log.d("ForumReportAdapter", "unlikePointRewards")
            author.get(Source.SERVER)
                .addOnSuccessListener { userDocument ->
                    val newScore = (userDocument.get(User.Keys.score) as Long - Points.RECEIVED_LIKE).let {
                        Log.i("ForumReportAdapter", "New score : $it")
                        if (it >= 0) it else 0
                    }
                    userDocument.reference.update(User.Keys.score, newScore)
                }

            user.get(Source.SERVER)
                .addOnSuccessListener { userDocument ->
                    val newScore = (userDocument.get(User.Keys.score) as Long - Points.DO_LIKE).let {
                        if (it >= 0) it else 0
                    }
                    userDocument.reference.update(User.Keys.score, newScore)
                }
        }

        override fun commentPointRewards(user: DocumentReference, author: DocumentReference) {}

    }

    private fun Forums.logClickAction(msg: String) {
        Log.i("ForumReportAdapter", "Successful run $msg" +
                "\n\tForum Detail :" +
                "\n\t\tTitle : ${this.title}" +
                "\n\t\tDatetime : ${this.createdAt?.toDate()}" +
                "\n\n---------------------------\n\n")
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumPostViewHolder {
        val binding = ComponentCardReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ForumPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForumPostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}