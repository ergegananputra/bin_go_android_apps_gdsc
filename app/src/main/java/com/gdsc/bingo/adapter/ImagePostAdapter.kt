package com.gdsc.bingo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.gdsc.bingo.databinding.ComponentPostImageBinding
import com.gdsc.bingo.model.PostImage

class ImagePostAdapter (
    private val isOnline : Boolean = false,
    private val useDeleteButton : Boolean = false,
    private var onDeleteItem : (Int) -> Unit = {}
) : ListAdapter<PostImage, ImagePostAdapter.ImagePostViewHolder>(ImagePostDiffUtil()) {
    inner class ImagePostViewHolder(
        private val binding : ComponentPostImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(postImage: PostImage, position: Int) {
            binding.componentPostImageButtonCancel.visibility = if (useDeleteButton) View.VISIBLE else View.GONE

            if (isOnline) {
                loadOnlineImage(postImage)
            } else {
                loadOfflineImage(postImage)
                buttonDeleteOfflineImage(position)
            }
        }

        private fun loadOfflineImage(image: PostImage) {
            val uri = image.path?.toUri()
            binding.componentPostImageView.load(uri)
        }

        private fun buttonDeleteOfflineImage(position: Int) {
            binding.componentPostImageButtonCancel.setOnClickListener {
                onDeleteItem(position)
            }
        }

        private fun loadOnlineImage(image: PostImage) {
            // TODO: Load image from Firebase Storage
        }
    }

    class ImagePostDiffUtil : DiffUtil.ItemCallback<PostImage>() {
        override fun areItemsTheSame(oldItem: PostImage, newItem: PostImage): Boolean {
            return oldItem.referencePath?.path == newItem.referencePath?.path
        }

        override fun areContentsTheSame(oldItem: PostImage, newItem: PostImage): Boolean {
            return oldItem.toFirebaseModel().toString() == newItem.toFirebaseModel().toString()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagePostViewHolder {
        val binding = ComponentPostImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ImagePostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImagePostViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

}