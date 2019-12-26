package com.agileburo.anytype.ui.database.modals.viewholder.details

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ext.invisible
import com.agileburo.anytype.core_utils.ext.setVisible
import com.agileburo.anytype.core_utils.ext.visible
import com.agileburo.anytype.presentation.databaseview.models.ColumnView

class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val iconHide: ImageView = itemView.findViewById(R.id.personHide)
    private val iconForward: ImageView = itemView.findViewById(R.id.personForward)
    private val iconDrag: ImageView = itemView.findViewById(R.id.dragNDrop)

    fun bind(click: (ColumnView) -> Unit, detail: ColumnView.Person, isDragOn: Boolean) {
        if (isDragOn) {
            iconDrag.visible()
            iconHide.invisible()
            iconForward.invisible()
        } else {
            iconHide.setVisible(!detail.show)
        }
        itemView.setOnClickListener {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                click.invoke(detail)
            }
        }
    }
}