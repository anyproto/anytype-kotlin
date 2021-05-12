package com.anytypeio.anytype.core_ui.features.page.slash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.page.slash.holders.SubheaderMenuHolder
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem
import kotlinx.android.synthetic.main.item_slash_widget_subheader.view.*

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
                    view = inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.flBack.setOnClickListener {
                        clicks.invoke(SlashItem.Back)
                    }
                }
            }
            else -> throw IllegalArgumentException("Wrong viewtype:$viewType")
        }
    }

    override fun getItemCount(): Int = items.size
}