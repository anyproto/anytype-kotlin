package com.agileburo.anytype.core_ui.features.page.modal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.ViewType

class AddBlockAdapter(
    val views: List<AddBlockView>
) : RecyclerView.Adapter<AddBlockAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_HOLDER_ITEM -> ViewHolder.Item(
                view = inflater.inflate(
                    R.layout.item_add_block,
                    parent,
                    false
                )
            )
            else -> throw IllegalStateException("Unexpected type: $viewType")
        }
    }

    override fun getItemCount(): Int = views.size
    override fun getItemViewType(position: Int) = views[position].getViewType()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class Section(view: View) : ViewHolder(view)

        class Item(view: View) : ViewHolder(view)

    }

    sealed class AddBlockView : ViewType {
        data class Section(val type: Int) : AddBlockView() {
            override fun getViewType(): Int = VIEW_HOLDER_SECTION
        }

        data class Item(val type: Int) : AddBlockView() {
            override fun getViewType(): Int = VIEW_HOLDER_ITEM
        }
    }

    companion object {
        const val VIEW_HOLDER_SECTION = 0
        const val VIEW_HOLDER_ITEM = 1
    }
}