package com.anytypeio.anytype.sample.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.anytypeio.anytype.sample.R
import com.anytypeio.anytype.sample.adapter.AbstractAdapter
import com.anytypeio.anytype.sample.adapter.AbstractHolder
import kotlinx.android.synthetic.main.item_editable.view.*

class SearchOnPageAdapter(
    private var items: List<Item>
) : AbstractAdapter<SearchOnPageAdapter.Item>(items) {

    override fun update(update: List<Item>) {
        this.items = update
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractHolder<Item> {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_editable, parent, false)
        return ItemViewHolder(view)
    }

    data class Item(
        val id: Int,
        val txt: String
    )

    class ItemViewHolder(view: View) : AbstractHolder<Item>(view) {
        override fun bind(item: Item) {
            itemView.input.setText(item.txt)
        }
    }
}