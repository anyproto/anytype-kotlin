package com.anytypeio.anytype.core_ui.features.navigation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.navigation.ObjectView

class PageNavigationAdapter(
    private val onClick: (String) -> Unit,
    private val onSearchClick: (MutableList<ObjectView>) -> Unit
) :
    RecyclerView.Adapter<PageLinksListHolder>() {

    private var inbound = mutableListOf<ObjectView>()
    private var outbound = mutableListOf<ObjectView>()

    fun setPageLinks(inbound: List<ObjectView>, outbound: List<ObjectView>) {
        this.inbound.clear()
        this.inbound.addAll(inbound)
        this.outbound.clear()
        this.outbound.addAll(outbound)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: PageLinksListHolder, position: Int) {
        if (position == 0) {
            holder.bind(inbound, onClick, { onSearchClick.invoke(inbound) })
        } else {
            holder.bind(outbound, onClick, { onSearchClick.invoke(outbound) })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageLinksListHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PageLinksListHolder(
            view = inflater.inflate(R.layout.item_page_link_list, parent, false)
        )
    }

    override fun getItemCount(): Int = 2
}