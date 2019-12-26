package com.agileburo.anytype.ui.database.table.viewholder.columns

import android.view.View
import com.agileburo.anytype.presentation.databaseview.models.ColumnView
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import kotlinx.android.synthetic.main.item_table_column_text.view.*

class ColumnTextViewHolder(itemView: View) : AbstractViewHolder(itemView) {

    fun bind(column: ColumnView.Text) {
        itemView.title.text = column.name
    }
}