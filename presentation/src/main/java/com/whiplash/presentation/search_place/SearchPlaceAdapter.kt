package com.whiplash.presentation.search_place

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.whiplash.domain.entity.auth.response.SearchPlaceEntity
import com.whiplash.presentation.databinding.ItemSearchPlaceBinding

class SearchPlaceAdapter(
    private val onPlaceClick: (SearchPlaceEntity) -> Unit
): ListAdapter<SearchPlaceEntity, SearchPlaceAdapter.SearchPlaceViewHolder>(SearchPlaceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPlaceViewHolder {
        val binding = ItemSearchPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchPlaceViewHolder(binding, onPlaceClick)
    }

    override fun onBindViewHolder(holder: SearchPlaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SearchPlaceViewHolder(
        private val binding: ItemSearchPlaceBinding,
        private val onPlaceClick: (SearchPlaceEntity) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SearchPlaceEntity) {
            with(binding) {
                tvSimpleAddress.text = data.name
                tvDetailAddress.text = data.address

                root.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onPlaceClick(data)
                    }
                }
            }
        }
    }

    private class SearchPlaceDiffCallback: DiffUtil.ItemCallback<SearchPlaceEntity>() {
        override fun areItemsTheSame(
            oldItem: SearchPlaceEntity,
            newItem: SearchPlaceEntity
        ): Boolean {
            return oldItem.latitude == newItem.latitude &&
                    oldItem.longitude == newItem.longitude
        }

        override fun areContentsTheSame(
            oldItem: SearchPlaceEntity,
            newItem: SearchPlaceEntity
        ): Boolean {
            return oldItem == newItem
        }

    }

}