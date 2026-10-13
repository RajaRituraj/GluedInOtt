package com.gluedin.ott.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gluedin.ott.databinding.ItemContentRailBinding
import com.gluedin.ott.domain.model.ContentItem
import com.gluedin.ott.domain.model.ContentRail

class ContentRailsAdapter(
    private val onItemClick: (ContentItem) -> Unit
) : ListAdapter<ContentRail, ContentRailsAdapter.RailViewHolder>(DiffCallback) {

    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RailViewHolder {
        val binding = ItemContentRailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RailViewHolder(binding, onItemClick, viewPool)
    }

    override fun onBindViewHolder(holder: RailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RailViewHolder(
        private val binding: ItemContentRailBinding,
        private val onItemClick: (ContentItem) -> Unit,
        private val sharedPool: RecyclerView.RecycledViewPool
    ) : RecyclerView.ViewHolder(binding.root) {

        private val cardAdapter = ContentCardAdapter(onItemClick)

        init {
            binding.rvRailItems.apply {
                layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                adapter = cardAdapter
                setRecycledViewPool(sharedPool)
                setHasFixedSize(true)
            }
        }

        fun bind(rail: ContentRail) {
            binding.tvRailTitle.text = rail.title
            cardAdapter.submitList(rail.items)
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ContentRail>() {
            override fun areItemsTheSame(oldItem: ContentRail, newItem: ContentRail): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ContentRail, newItem: ContentRail): Boolean =
                oldItem == newItem
        }
    }
}
