package com.anytypeio.anytype.core_ui.features.navigation

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.toast
import com.anytypeio.anytype.core_utils.ui.BottomOffsetDecoration
import kotlinx.android.synthetic.main.item_page_link_list.view.*

class PageLinksListHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val recycler = itemView.recyclerView
    private val searchView = itemView.searchView
    private val sorting = itemView.icSorting

    fun bind(
        links: List<PageLinkView>,
        onClick: (String) -> Unit,
        onSearchClick: () -> Unit
    ) {
        sorting.setOnClickListener { recycler.context.toast("Not implemented yet") }
        searchView.setOnClickListener { onSearchClick() }
        with(recycler) {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(context)
                val offset =
                    context.resources.getDimensionPixelOffset(R.dimen.default_page_links_bottom_offset)
                addItemDecoration(BottomOffsetDecoration(offset))
            }
            if (adapter == null) {
                adapter = PageLinksAdapter(links.toMutableList(), onClick)
            } else {
                (adapter as PageLinksAdapter).updateLinks(links)
            }
        }
    }
}