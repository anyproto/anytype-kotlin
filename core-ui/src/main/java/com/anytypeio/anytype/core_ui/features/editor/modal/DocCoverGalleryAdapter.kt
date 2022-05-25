package com.anytypeio.anytype.core_ui.features.editor.modal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemDocCoverGalleryGradientBinding
import com.anytypeio.anytype.core_ui.databinding.ItemDocCoverGalleryHeaderBinding
import com.anytypeio.anytype.core_ui.databinding.ItemDocCoverGalleryImageBinding
import com.anytypeio.anytype.core_ui.extensions.tint
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.editor.cover.DocCoverGalleryView
import com.bumptech.glide.Glide

class DocCoverGalleryAdapter(
    private val onSolidColorClicked: (CoverColor) -> Unit,
    private val onGradientClicked: (String) -> Unit,
    private val onImageClicked: (String) -> Unit,
) : RecyclerView.Adapter<DocCoverGalleryAdapter.ViewHolder>() {

    var views: List<DocCoverGalleryView> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_doc_cover_gallery_header -> {
                ViewHolder.Header(
                    binding = ItemDocCoverGalleryHeaderBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            R.layout.item_doc_cover_gallery_color -> {
                ViewHolder.Color(
                    view = inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val view = views[bindingAdapterPosition] as DocCoverGalleryView.Color
                        onSolidColorClicked(view.color)
                    }
                }
            }
            R.layout.item_doc_cover_gallery_image -> {
                ViewHolder.Image(
                    binding = ItemDocCoverGalleryImageBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val view = views[bindingAdapterPosition] as DocCoverGalleryView.Image
                        onImageClicked(view.hash)
                    }
                }
            }
            R.layout.item_doc_cover_gallery_gradient -> {
                ViewHolder.Gradient(
                    binding = ItemDocCoverGalleryGradientBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val view = views[bindingAdapterPosition] as DocCoverGalleryView.Gradient
                        onGradientClicked(view.gradient)
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) = when (holder) {
        is ViewHolder.Color -> holder.bind(views[position] as DocCoverGalleryView.Color)
        is ViewHolder.Header -> holder.bind(views[position] as DocCoverGalleryView.Section)
        is ViewHolder.Image -> holder.bind(views[position] as DocCoverGalleryView.Image)
        is ViewHolder.Gradient -> holder.bind(views[position] as DocCoverGalleryView.Gradient)
    }

    override fun getItemCount(): Int = views.size

    override fun getItemViewType(position: Int) = when (views[position]) {
        is DocCoverGalleryView.Section -> R.layout.item_doc_cover_gallery_header
        is DocCoverGalleryView.Color -> R.layout.item_doc_cover_gallery_color
        is DocCoverGalleryView.Image -> R.layout.item_doc_cover_gallery_image
        is DocCoverGalleryView.Gradient -> R.layout.item_doc_cover_gallery_gradient
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class Header(val binding: ItemDocCoverGalleryHeaderBinding) : ViewHolder(binding.root) {
            fun bind(item: DocCoverGalleryView.Section) {
                when (item) {
                    is DocCoverGalleryView.Section.Collection -> {
                        binding.tvHeader.text = item.title
                    }
                    DocCoverGalleryView.Section.Color -> {
                        binding.tvHeader.setText(R.string.cover_color_solid)
                    }
                    DocCoverGalleryView.Section.Gradient -> {
                        binding.tvHeader.setText(R.string.cover_gradients)
                    }
                }
            }
        }

        class Color(view: View) : ViewHolder(view) {
            fun bind(item: DocCoverGalleryView.Color) {
                itemView.tint(item.color.color)
            }
        }

        class Gradient(val binding: ItemDocCoverGalleryGradientBinding) : ViewHolder(binding.root) {
            fun bind(item: DocCoverGalleryView.Gradient) {
                binding.gradient.apply {
                    when (item.gradient) {
                        CoverGradient.YELLOW -> setBackgroundResource(R.drawable.cover_gradient_yellow)
                        CoverGradient.RED -> setBackgroundResource(R.drawable.cover_gradient_red)
                        CoverGradient.BLUE -> setBackgroundResource(R.drawable.cover_gradient_blue)
                        CoverGradient.TEAL -> setBackgroundResource(R.drawable.cover_gradient_teal)
                        CoverGradient.PINK_ORANGE -> setBackgroundResource(R.drawable.wallpaper_gradient_1)
                        CoverGradient.BLUE_PINK -> setBackgroundResource(R.drawable.wallpaper_gradient_2)
                        CoverGradient.GREEN_ORANGE -> setBackgroundResource(R.drawable.wallpaper_gradient_3)
                        CoverGradient.SKY -> setBackgroundResource(R.drawable.wallpaper_gradient_4)
                    }
                }
            }
        }

        class Image(val binding: ItemDocCoverGalleryImageBinding) : ViewHolder(binding.root) {

            fun bind(item: DocCoverGalleryView.Image) {
                Glide.with(itemView)
                    .load(item.url)
                    .centerCrop()
                    .into(binding.image)
            }
        }
    }
}