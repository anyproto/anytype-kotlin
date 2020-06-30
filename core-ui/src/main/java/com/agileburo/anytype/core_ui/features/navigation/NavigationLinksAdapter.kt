package com.agileburo.anytype.core_ui.features.navigation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R

class NavigationLinksAdapter(val data: List<String>) :
    RecyclerView.Adapter<NavigationLinkViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavigationLinkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_navigation_link, parent, false)
        return NavigationLinkViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holderLink: NavigationLinkViewHolder, position: Int) {
        holderLink.bind()
    }
}