package com.gdsc.bingo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gdsc.bingo.databinding.ComponentHistoryInformationItemBinding
import com.gdsc.bingo.model.User

class RankAdapter(

) : ListAdapter<User, RankAdapter.RankViewHolder>(RankDiffUtils()) {
    class RankDiffUtils : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.referencePath == newItem.referencePath
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.toFirebaseModel().toString() == newItem.toFirebaseModel().toString()
        }

    }

    inner class RankViewHolder(
        private val binding: ComponentHistoryInformationItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: User, position: Int) {
            binding.componentHistoryInformationScore.text = item.score.toString()
            binding.copmonentHistoryInformationTitle.text = item.username
            binding.copmonentHistoryInformationTitle.apply {
                isSelected = true
                isFocusable = true
            }
            val text = "Rank #${position + 1}"
            binding.componentHistoryInformationTextViewActivityRewardFrom.text = text
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankViewHolder {
        val binding = ComponentHistoryInformationItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return RankViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RankViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }



}