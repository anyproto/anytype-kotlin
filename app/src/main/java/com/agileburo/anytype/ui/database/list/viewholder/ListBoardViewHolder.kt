package com.agileburo.anytype.ui.database.list.viewholder

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.layout.SpacingItemDecoration
import com.agileburo.anytype.core_utils.ext.DATE_FORMAT_MMMdYYYY
import com.agileburo.anytype.core_utils.ext.formatToDateString
import com.agileburo.anytype.presentation.databaseview.models.ListItem
import com.agileburo.anytype.presentation.databaseview.models.TagView
import com.agileburo.anytype.ui.database.tags.TagAdapter
import kotlinx.android.synthetic.main.item_list_board.view.*
import java.util.*

class ListBoardViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(item: ListItem, click: (String) -> Unit) {
        with(itemView) {
            name.text = item.name
            date.text = item.date.formatToDateString(DATE_FORMAT_MMMdYYYY, Locale.getDefault())
            avatar.bind(item.name)
            if (databaseViewTags.adapter == null) {
                initRecyclerView(databaseViewTags, item.tags)
            } else {
                (databaseViewTags.adapter as? TagAdapter)?.clear()
                (databaseViewTags.adapter as? TagAdapter)?.addData(item.tags)
            }
            this.setOnClickListener {
                click(item.id)
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