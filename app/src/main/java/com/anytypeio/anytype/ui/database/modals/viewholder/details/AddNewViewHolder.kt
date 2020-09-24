package com.anytypeio.anytype.ui.database.modals.viewholder.details

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.presentation.databaseview.models.ColumnView

class AddNewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(click: (ColumnView) -> Unit, detail: ColumnView.AddNew) {
        itemView.setOnClickListener {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                click.invoke(detail)
            }
        }
    }
}