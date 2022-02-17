package com.anytypeio.anytype.core_ui.features.editor.slash

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetSubheaderBinding
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.SubheaderMenuHolder
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem

abstract class SlashBaseAdapter(
    protected var items: List<SlashItem>,
    protected val clicks: (SlashItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    abstract fun createHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder

    fun update(items: List<SlashItem>) {
        if (items.isEmpty()) {
            clear()
        } else {
            this.items = items
            notifyDataSetChanged()
        }
    }

    fun clear() {
        val size = items.size
        if (size > 0) {
            items = listOf()
            notifyItemRangeRemoved(0, size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_slash_widget_style -> {
                createHolder(inflater, parent, viewType)
                    .apply {
                        itemView.setOnClickListener {
                            clicks(items[bindingAdapterPosition])
                        }
                    }
            }
            R.layout.item_slash_widget_subheader -> {
                SubheaderMenuHolder(
                    binding = ItemSlashWidgetSubheaderBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.findViewById<FrameLayout>(R.id.flBack).setOnClickListener {
                        clicks.invoke(SlashItem.Back)
                    }
                }
            }
            else -> throw IllegalArgumentException("Wrong viewtype:$viewType")
        }
    }

    override fun getItemCount(): Int = items.size
}