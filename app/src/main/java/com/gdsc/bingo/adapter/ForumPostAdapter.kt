package com.gdsc.bingo.adapter


import android.content.Context
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
import com.gdsc.bingo.databinding.ComponentCardKomunitasBinding
import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.model.Like
import com.gdsc.bingo.model.User
import com.gdsc.bingo.services.points.Points
import com.gdsc.bingo.services.points.PointsRewardSystem
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.ZoneId

class ForumPostAdapter(
    private val context: Context,
    private val storage: FirebaseStorage,
    var actionComment : (Forums) -> Unit = {},
    var actionLike : (Forums) -> Unit = {},
    var actionVerticalButton : (Forums) -> Unit = {},
    var actionOpenDetail : (Forums) -> Unit = {},
) : ListAdapter<Forums, ForumPostAdapter.ForumPostViewHolder>(ForumsPostDiffUtil()) {
    class ForumsPostDiffUtil : DiffUtil.ItemCallback<Forums>() {
        override fun areItemsTheSame(oldItem: Forums, newItem: Forums): Boolean {
            return oldItem.referencePath == newItem.referencePath
        }

        override fun areContentsTheSame(oldItem: Forums, newItem: Forums): Boolean {
            return oldItem.toFirebaseModel().toString() == newItem.toFirebaseModel().toString()
        }

    }

    inner class ForumPostViewHolder(
        private val binding : ComponentCardKomunitasBinding
    ) : RecyclerView.ViewHolder(binding.root), PointsRewardSystem {
        fun bind(forums: Forums) {
            Log.i("ForumPostAdapter", "Bind data to viewholder: \n\t${forums}")

            startAllShimmer()

            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.Main) {
                    setupPostTimestamp(forums.createdAt!!)
                    setupPostTitle(forums.title!!)
                    setupCommentAndLikeCount(forums.commentCount, forums.dislikeCount, forums.likeCount)
                    setupCommentButton(forums)
                    setupLikeButton(forums)
                    setupVerticalButton(forums)
                    setupOpenDetail(forums)
                }
                withContext(Dispatchers.IO) {
                    setupProfile(forums.author!!)
                    setupThumbnail(forums.thumbnailPhotosUrl)
                }
            }
        }

        private fun startAllShimmer() {
            with(binding) {
                componentCardKomunitasLinearLayoutShimmerTextContainer.startShimmer()
                componentCardKomunitasShimmerTitle.startShimmer()
                componentCardKomunitasShimmerCommentLikeCount.startShimmer()
            }
        }

        private fun setupOpenDetail(forums: Forums) {
            binding.componentCardKomunitasContainer.setOnClickListener {
                actionOpenDetail(forums)
                forums.logClickAction("setupOpenDetail")
            }
        }

        private fun setupVerticalButton(forums: Forums) {
            binding.componentCardKomunitasButtonMore.setOnClickListener {
                actionVerticalButton(forums)
                forums.logClickAction("setupVerticalButton")
            }
        }

        private fun setupLikeButton(forums: Forums) {
            val auth = FirebaseAuth.getInstance()

            if (auth.uid == null) {
                Log.e("ForumPostAdapter", "User is not logged in")
                binding.componentCardKomunitasButtonLike.visibility = View.GONE
                return
            } else {
                binding.componentCardKomunitasButtonLike.visibility = View.VISIBLE
            }

            var initialState = true
            val fireStore = FirebaseFirestore.getInstance()

            forums.likesReference!!.collection(Like().table)
                .document(auth.uid!!)
                .get()
                .addOnSuccessListener { documentReference ->
                    if (documentReference.exists()) {
                        likeUI()
                        initialState = true
                        return@addOnSuccessListener
                    } else {
                        unlikeUI()
                        initialState = false
                    }
                }

            val forumRef = forums.referencePath!!
            val likeCount = forums.likeCount
            val dislikeCount = forums.dislikeCount
            val user = fireStore.collection(User().table).document(auth.uid!!)
            val author = forums.author!!

            if (user.path != author.path) {
                binding.componentCardKomunitasButtonLike.setOnCheckedChangeListener { _, isChecked ->

                    val like = Like(
                        referencePath = forumRef,
                        owner = user,
                        objectPerson = author,
                        createAt = Timestamp.now()
                    )


                    forums.likesReference!!
                        .collection(like.table)
                        .document(user.id)
                        .get(Source.SERVER)
                        .addOnSuccessListener { documentReference ->
                            if (isChecked) {
                                documentReference.reference.set(like.toFirebaseModel())
                                    .addOnSuccessListener {

                                        val newLike = if (initialState) likeCount else {
                                            likePointRewards(user, author)
                                            likeCount + 1
                                        }

                                        forumRef.update("like_count", newLike)



                                        setupCommentAndLikeCount(
                                            likeCount = newLike,
                                            dislikeCount = forums.dislikeCount,
                                            commentCount = forums.commentCount
                                        )
                                    }
                                return@addOnSuccessListener
                            } else {
                                documentReference.reference.delete()
                                    .addOnSuccessListener {

                                        val newLike = if (initialState) {
                                            unlikePointRewards(user, author)
                                            likeCount - 1
                                        } else likeCount

                                        forumRef.update("like_count", newLike)


                                        setupCommentAndLikeCount(
                                            likeCount = newLike,
                                            dislikeCount = forums.dislikeCount,
                                            commentCount = forums.commentCount
                                        )
                                    }
                            }


                        }

                }
            } else {
                binding.componentCardKomunitasButtonLike.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        binding.componentCardKomunitasButtonLike.isChecked = false
                    }
                }
            }


        }

        private fun unlikeUI() {
            binding.componentCardKomunitasButtonLike.isChecked = false
        }

        private fun likeUI() {
            binding.componentCardKomunitasButtonLike.isChecked = true
        }

        private fun setupCommentButton(forums: Forums) {
            binding.componentCardKomunitasButtonComment.setOnClickListener {
                actionComment(forums)
                forums.logClickAction("setupCommentButton")
            }
        }

        private fun setupCommentAndLikeCount(commentCount: Long, dislikeCount: Long ,likeCount: Long) {
            val komen = context.resources.getString(R.string.komentar)
            val suka = context.resources.getString(R.string.suka)
            val text = "$commentCount $komen, $likeCount $suka"
            binding.componentCardKomunitasTextViewCommentLikeCount.text = text
            binding.componentCardKomunitasShimmerCommentLikeCount.apply {
                stopShimmer()
                visibility = View.GONE
            }
        }


        private fun setupPostTitle(title: String) {
            binding.componentCardKomunitasTextViewTitle.text = title
            binding.componentCardKomunitasShimmerTitle.apply {
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
                binding.componentCardKomunitasImageViewPost.visibility = if (thumbnailPhotosUrl == null) View.GONE else View.VISIBLE
                if (thumbnailPhotosUrl == null) return@launch


                storage.getReference(thumbnailPhotosUrl).downloadUrl.addOnSuccessListener { thumbnail ->
                    binding.componentCardKomunitasImageViewPost.load(thumbnail)
                }.addOnFailureListener {
                    Log.e("ForumPostAdapter", "Error loading thumbnail", it)
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
                    Log.e("ForumPostAdapter", "User data is null")
                }
            }
        }

        private fun loadProfilePicture(profilePicturePath: String?) {
            if (profilePicturePath == null) {
                Log.e("ForumPostAdapter", "Profile picture path is null")
                binding.componentCardKomunitasCardProfilImage.load(R.drawable.ic_person_24) {
                    transformations(CircleCropTransformation())
                }
                return
            }

            Log.i("ForumPostAdapter", "Profile picture path : $profilePicturePath")

            storage.getReference(profilePicturePath).downloadUrl.addOnSuccessListener {
                Log.i("ForumPostAdapter", "Profile picture loaded : $it")
                binding.componentCardKomunitasCardProfilImage.load(it) {
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.ic_person_24)
                    error(R.drawable.ic_person_24)
                }
            }.addOnFailureListener {
                Log.e("ForumPostAdapter", "Error getting profile picture", it)
            }
        }

        private fun loadUsername(username: String) {
            binding.componentCardKomunitasTextViewName.text = username
        }

        private fun setupPostTimestamp(createdAt: Timestamp) {
            val date = createdAt.toDate()

            val zonedDateTime = date.toInstant().atZone(ZoneId.systemDefault())
            val month = "${zonedDateTime.month}".lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            val text = "${zonedDateTime.dayOfMonth} $month ${zonedDateTime.year}, ${zonedDateTime.hour}:${zonedDateTime.minute}"
            binding.componentCardKomunitasTextViewDateTime.text = text

            binding.componentCardKomunitasLinearLayoutShimmerTextContainer.apply {
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
            Log.d("ForumPostAdapter", "unlikePointRewards")
            author.get(Source.SERVER)
                .addOnSuccessListener { userDocument ->
                    val newScore = (userDocument.get(User.Keys.score) as Long - Points.RECEIVED_LIKE).let {
                        Log.i("ForumPostAdapter", "New score : $it")
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
        Log.i("ForumPostAdapter", "Successful run $msg" +
                "\n\tForum Detail :" +
                "\n\t\tTitle : ${this.title}" +
                "\n\t\tDatetime : ${this.createdAt?.toDate()}" +
                "\n\n---------------------------\n\n")
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumPostViewHolder {
        val binding = ComponentCardKomunitasBinding.inflate(
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