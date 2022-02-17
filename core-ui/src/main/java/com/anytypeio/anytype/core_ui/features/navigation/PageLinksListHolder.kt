package com.anytypeio.anytype.core_ui.features.navigation

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemPageLinkListBinding
import com.anytypeio.anytype.core_ui.extensions.toast
import com.anytypeio.anytype.core_utils.ui.BottomOffsetDecoration
import com.anytypeio.anytype.presentation.navigation.ObjectView

@Deprecated("Maybe legacy?")
class PageLinksListHolder(
    val binding: ItemPageLinkListBinding
) : RecyclerView.ViewHolder(binding.root) {

    private val recycler = binding.recyclerView
    private val searchView = binding.searchView
    private val sorting = binding.icSorting

    fun bind(
        links: List<ObjectView>,
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
                adapter = PageLinksAdapter(links.toMutableList()) { obj, _ ->
                    onClick(obj)
                }
            } else {
                (adapter as PageLinksAdapter).updateLinks(links)
            }
        }
    }
}