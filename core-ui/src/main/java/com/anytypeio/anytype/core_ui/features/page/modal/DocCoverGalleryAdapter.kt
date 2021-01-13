package com.anytypeio.anytype.core_ui.features.page.modal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.tint
import com.anytypeio.anytype.presentation.page.cover.CoverColor
import com.anytypeio.anytype.presentation.page.cover.DocCaverGalleryView
import kotlinx.android.synthetic.main.item_doc_cover_gallery_header.view.*

class DocCoverGalleryAdapter(
    private val onSolidColorClicked: (CoverColor) -> Unit
) : RecyclerView.Adapter<DocCoverGalleryAdapter.ViewHolder>() {

    var views: List<DocCaverGalleryView> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_doc_cover_gallery_header -> {
                ViewHolder.Header(
                    view = inflater.inflate(viewType, parent, false)
                )
            }
            R.layout.item_doc_cover_gallery_color -> {
                ViewHolder.Color(
                    view = inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val view = views[bindingAdapterPosition] as DocCaverGalleryView.Color
                        onSolidColorClicked(view.color)
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
        is ViewHolder.Color -> holder.bind(views[position] as DocCaverGalleryView.Color)
        is ViewHolder.Header -> holder.bind(views[position] as DocCaverGalleryView.Header)
    }

    override fun getItemCount(): Int = views.size

    override fun getItemViewType(position: Int) = when (views[position]) {
        is DocCaverGalleryView.Header -> R.layout.item_doc_cover_gallery_header
        is DocCaverGalleryView.Color -> R.layout.item_doc_cover_gallery_color
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class Header(view: View) : ViewHolder(view) {
            fun bind(item: DocCaverGalleryView.Header) {
                itemView.tvHeader.text = item.title
            }
        }

        class Color(view: View) : ViewHolder(view) {
            fun bind(item: DocCaverGalleryView.Color) {
                itemView.tint(item.color.color)
            }
        }
    }
}