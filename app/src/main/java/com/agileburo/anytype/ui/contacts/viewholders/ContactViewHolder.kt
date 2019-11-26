package com.agileburo.anytype.ui.contacts.viewholders

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.layout.SpacingItemDecoration
import com.agileburo.anytype.core_utils.ext.DATE_FORMAT_MMMdYYYY
import com.agileburo.anytype.core_utils.ext.formatToDateString
import com.agileburo.anytype.presentation.contacts.model.ContactView
import com.agileburo.anytype.presentation.contacts.model.TagView
import com.agileburo.anytype.ui.contacts.TagAdapter
import kotlinx.android.synthetic.main.item_list_contact.view.*
import java.util.*

class ContactViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(contact: ContactView, click: (String) -> Unit) {
        with(itemView) {
            name.text = contact.name
            date.text = contact.date.formatToDateString(DATE_FORMAT_MMMdYYYY, Locale.getDefault())
            avatar.bind(contact.name)
            if (databaseViewTags.adapter == null) {
                contact.tags?.let {
                    initRecyclerView(databaseViewTags, it)
                }
            } else {
                (databaseViewTags.adapter as? TagAdapter)?.clear()
                contact.tags?.let {
                    (databaseViewTags.adapter as? TagAdapter)?.addData(it)
                }
            }
            this.setOnClickListener {
                click(contact.id)
            }
        }
    }

    private fun initRecyclerView(rv: RecyclerView, tags: List<TagView>) = with(rv) {
        addItemDecoration(SpacingItemDecoration(spacingStart = 8, firstItemSpacingStart = 0))
        layoutManager =
            LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
        adapter = TagAdapter().also {
            it.addData(tags)
        }
    }
}