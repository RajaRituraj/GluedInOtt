package com.gluedin.ott.presentation.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.gluedin.ott.R
import com.gluedin.ott.databinding.ItemContentCardBinding
import com.gluedin.ott.domain.model.ContentItem

class ContentCardAdapter(
    private val onItemClick: (ContentItem) -> Unit
) : ListAdapter<ContentItem, ContentCardAdapter.CardViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemContentCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CardViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CardViewHolder(
        private val binding: ItemContentCardBinding,
        private val onItemClick: (ContentItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ContentItem) {
            binding.tvTitle.text = item.title

            if (!item.badge.isNullOrBlank()) {
                binding.tvBadge.visibility = View.VISIBLE
                binding.tvBadge.text = item.badge
            } else {
                binding.tvBadge.visibility = View.GONE
            }

            if (item.rating.isNotBlank()) {
                binding.tvRating.visibility = View.VISIBLE
                binding.tvRating.text = "★ ${item.rating}"
            } else {
                binding.tvRating.visibility = View.GONE
            }

            binding.ivThumbnail.load(item.thumbnailUrl) {
                crossfade(true)
                placeholder(R.drawable.placeholder_card)
                error(R.drawable.placeholder_card)
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ContentItem>() {
            override fun areItemsTheSame(oldItem: ContentItem, newItem: ContentItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ContentItem, newItem: ContentItem): Boolean =
                oldItem == newItem
        }
    }
}
