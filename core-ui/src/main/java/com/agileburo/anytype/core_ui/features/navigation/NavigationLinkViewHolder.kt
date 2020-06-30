package com.agileburo.anytype.core_ui.features.navigation

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_navigation_link.view.*

class NavigationLinkViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val title = itemView.tvTitle
    val subtitle = itemView.tvSubtitle

    //Todo will be updated in next PR
    fun bind() {
        title.text = "Is your feature request related to a problem? Please describe."
        subtitle.text =
            "The place where archived pages could be viewed and where you could temporary delete them or restore"
    }

}