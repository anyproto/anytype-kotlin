package com.anytypeio.anytype.core_ui.features.sets.viewer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemViewerCoverDefaultBinding
import com.anytypeio.anytype.core_ui.databinding.ItemViewerCoverRelationBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.viewer.ViewerImagePreviewSelectView

class ViewerCoverAdapter(
    val onItemClicked: (ViewerImagePreviewSelectView.Item) -> Unit
): RecyclerView.Adapter<ViewerCoverAdapter.ViewHolder>() {

    private var items: List<ViewerImagePreviewSelectView> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            VIEW_TYPE_SECTION -> Section(parent)
            VIEW_TYPE_DEFAULT -> Default(
                binding = ItemViewerCoverDefaultBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            ).apply {
                itemView.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onItemClicked(items[pos] as ViewerImagePreviewSelectView.Item)
                    }
                }
            }
            VIEW_TYPE_RELATION -> Relation(
                binding = ItemViewerCoverRelationBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            ).apply {
                itemView.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onItemClicked(items[pos] as ViewerImagePreviewSelectView.Item)
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(holder) {
            is Default -> {
                holder.bind(item = items[position] as ViewerImagePreviewSelectView.Item)
            }
            is Relation -> {
                holder.bind(item = items[position] as ViewerImagePreviewSelectView.Item.Relation)
            }
            else -> {
                // Do nothing
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when(items[position]) {
        is ViewerImagePreviewSelectView.Item.Cover -> VIEW_TYPE_DEFAULT
        is ViewerImagePreviewSelectView.Item.None -> VIEW_TYPE_DEFAULT
        is ViewerImagePreviewSelectView.Item.Relation -> VIEW_TYPE_RELATION
        is ViewerImagePreviewSelectView.Section -> VIEW_TYPE_SECTION
    }

    fun update(items: List<ViewerImagePreviewSelectView>) {
        this.items = items
        notifyDataSetChanged()
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class Default(val binding: ItemViewerCoverDefaultBinding) : ViewHolder(binding.root) {
        fun bind(item: ViewerImagePreviewSelectView.Item) = with(binding) {
            if (item.isSelected)
                ivCheckbox.visible()
            else
                ivCheckbox.gone()
            when(item) {
                is ViewerImagePreviewSelectView.Item.Cover -> {
                    tvCoverType.setText(R.string.cover)
                }
                is ViewerImagePreviewSelectView.Item.None -> {
                    tvCoverType.setText(R.string.none)
                }
                else -> {
                    // Do nothing.
                }
            }
        }
    }

    class Section(parent: ViewGroup) : ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_viewer_cover_section,
            parent,
            false
        )
    )

    class Relation(val binding: ItemViewerCoverRelationBinding) : ViewHolder(binding.root) {

        fun bind(item: ViewerImagePreviewSelectView.Item.Relation) = with(binding) {
            if (item.isSelected)
                ivRelationCheckbox.visible()
            else
                ivRelationCheckbox.gone()
            tvRelationName.text = item.name
        }
    }

    companion object {
        const val VIEW_TYPE_DEFAULT = 0
        const val VIEW_TYPE_RELATION = 1
        const val VIEW_TYPE_SECTION = 2
    }
}