package com.anytypeio.anytype.core_ui.features.dataview.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.sets.model.CellView

class DVGridCellUrlHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(cell: CellView.Url) {
        itemView.findViewById<TextView>(R.id.tvText).text = cell.url.orEmpty()
    }
}