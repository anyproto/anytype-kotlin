package com.anytypeio.anytype.core_ui.features.dataview

import android.view.View
import android.view.ViewGroup
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.AbstractAdapter
import com.anytypeio.anytype.core_ui.common.AbstractViewHolder
import kotlinx.android.synthetic.main.item_viewer_grid.view.*

class ViewerListAdapter(items: List<String> = emptyList()) : AbstractAdapter<String>(items) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder<String> = Holder(view = inflate(parent, R.layout.item_viewer_grid))

    class Holder(view: View) : AbstractViewHolder<String>(view) {

        override fun bind(item: String) {
            itemView.tvTitle.text = item
        }
    }
}