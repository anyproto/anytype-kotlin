package com.anytypeio.anytype.core_ui.features.cover

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.UnsplashImage
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemCoverUnsplashImageBinding
import com.anytypeio.anytype.core_utils.ext.dimen
import coil3.load
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation

class UnsplashImageAdapter(
    private val onImageClicked: (UnsplashImage) -> Unit
) : ListAdapter<UnsplashImage, UnsplashImageAdapter.ViewHolder>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        binding = ItemCoverUnsplashImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ).apply {
        itemView.setOnClickListener {
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION)
                onImageClicked(getItem(pos))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        val binding: ItemCoverUnsplashImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UnsplashImage) = with(binding) {
            tvName.text = item.artist.name
            ivImage.load(item.url) {
                placeholder(R.drawable.rect_unsplash_image_placeholder)
                transformations(RoundedCornersTransformation(dimen(R.dimen.dp_4).toFloat()))
            }
        }
    }

    object Differ : DiffUtil.ItemCallback<UnsplashImage>() {
        override fun areItemsTheSame(oldItem: UnsplashImage, newItem: UnsplashImage): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: UnsplashImage, newItem: UnsplashImage): Boolean {
            return oldItem == newItem
        }
    }
}