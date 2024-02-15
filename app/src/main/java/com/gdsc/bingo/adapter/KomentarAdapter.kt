package com.gdsc.bingo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.facebook.shimmer.ShimmerFrameLayout
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.ComponentCommentBinding
import com.gdsc.bingo.model.Komentar
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZoneId

class KomentarAdapter(
    private val storage: FirebaseStorage
) : ListAdapter<Komentar, KomentarAdapter.KomentarViewHolder>(KomentarDiffUtil()) {
    inner class KomentarViewHolder(
        private val binding : ComponentCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(komentar: Komentar) {

            startShimmer()

            CoroutineScope(Dispatchers.Main).launch {
                this.launch(Dispatchers.IO) {
                    loadProfilePicture(komentar.profilePicturePath)
                }

                this.launch {
                    loadUsername(komentar.username)
                    loadTimestamps(komentar.createdAt)
                    loadComment(komentar.komentar)
                    setupButtonVerticalMore(komentar)
                }


            }

        }

        private fun setupButtonVerticalMore(komentar: Komentar) {
            binding.componentCommentButtonVerticalMore.setOnClickListener {
                Log.d("KomentarAdapter", "Vertical More Clicked $komentar")
                Toast.makeText(binding.root.context, "Vertical More Clicked", Toast.LENGTH_SHORT).show()
            }
        }

        private fun loadComment(komentar: String?) {
            komentar ?: run {
                Log.e("KomentarAdapter", "Komentar is null")
                return
            }

            binding.componentCommentTextViewComment.text = komentar
            binding.componentCommentShimmerComment.stop()
        }

        private fun loadTimestamps(timestamp: Timestamp?) {
            timestamp ?: run {
                Log.e("KomentarAdapter", "Timestamp is null")
                return
            }

            val date = timestamp.toDate()

            val zonedDateTime = date.toInstant().atZone(ZoneId.systemDefault())
            val month = "${zonedDateTime.month}".lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            val text = "${zonedDateTime.dayOfMonth} $month ${zonedDateTime.year}, ${zonedDateTime.hour}:${zonedDateTime.minute}"
            binding.componentCommentTextViewTimestamp.text = text
            binding.componentCommentShimmerTimestamp.stop()
        }

        private fun loadProfilePicture(profilePicturePath: String?) {
            profilePicturePath ?: run {
                Log.e("KomentarAdapter", "Profile picture path is null")
                return
            }

            Log.i("KomentarAdapter", "Profile picture path: $profilePicturePath")

            storage.getReference(profilePicturePath).downloadUrl
                .addOnSuccessListener { uri ->
                    binding.componentCommentImageViewProfilePicture.load(uri) {
                        transformations(CircleCropTransformation())
                        placeholder(R.drawable.ic_person_24)
                        error(R.drawable.ic_person_24)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("KomentarAdapter", "Error getting profile picture", exception)
                    binding.componentCommentImageViewProfilePicture.load(R.drawable.ic_person_24) {
                        transformations(CircleCropTransformation())
                    }
                }

        }

        private fun loadUsername(username: String?) {
            username ?: run {
                Log.e("KomentarAdapter", "Username is null")
                return
            }

            binding.componentCommentTextViewUsername.text = username
            binding.componentCommentShimmerUsername.stop()
        }

        private fun startShimmer() {
            with(binding) {
                componentCommentShimmerUsername.start()
                componentCommentShimmerComment.start()
                componentCommentShimmerTimestamp.start()
            }
        }



    }

    private fun ShimmerFrameLayout.start() {
        this.startShimmer()
        this.visibility = View.VISIBLE
    }

    private fun ShimmerFrameLayout.stop() {
        this.stopShimmer()
        this.visibility = View.GONE
    }

    class KomentarDiffUtil : DiffUtil.ItemCallback<Komentar>() {
        override fun areItemsTheSame(oldItem: Komentar, newItem: Komentar): Boolean {
            return oldItem.referencePath!!.path == newItem.referencePath!!.path
        }

        override fun areContentsTheSame(oldItem: Komentar, newItem: Komentar): Boolean {
            return oldItem.toFirebaseModel().toString() == newItem.toFirebaseModel().toString()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KomentarViewHolder {
        val binding = ComponentCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return KomentarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KomentarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}
