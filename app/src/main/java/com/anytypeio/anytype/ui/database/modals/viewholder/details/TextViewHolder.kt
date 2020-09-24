package com.anytypeio.anytype.ui.database.modals.viewholder.details

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.setVisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.databaseview.models.ColumnView

class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val iconHide: ImageView = itemView.findViewById(R.id.textHide)
    private val iconForward: ImageView = itemView.findViewById(R.id.textForward)
    private val iconDrag: ImageView = itemView.findViewById(R.id.dragNDrop)

    fun bind(click: (ColumnView) -> Unit, detail: ColumnView.Text, isDragOn: Boolean) {
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