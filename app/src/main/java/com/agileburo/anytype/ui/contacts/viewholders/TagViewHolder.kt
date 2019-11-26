package com.agileburo.anytype.ui.contacts.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.presentation.contacts.model.TagView
import kotlinx.android.synthetic.main.item_tag.view.*

class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(tag: TagView) {
        itemView.name.text = tag.name
    }
}