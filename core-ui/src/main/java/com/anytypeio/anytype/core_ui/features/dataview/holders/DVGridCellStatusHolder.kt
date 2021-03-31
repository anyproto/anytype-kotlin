package com.anytypeio.anytype.core_ui.features.dataview.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.sets.model.CellView
import kotlinx.android.synthetic.main.item_viewer_grid_cell_description.view.*

class DVGridCellStatusHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(cell: CellView.Status) = with(itemView) {
        val status = cell.status.firstOrNull()
        if (status != null) {
            tvText.text = status.status
            val color = ThemeColor.values().find { v -> v.title == status.color }
            if (color != null) {
                tvText.setTextColor(color.text)
            } else {
                tvText.setTextColor(context.color(R.color.default_filter_status_text_color))
            }
        } else {
            tvText.text = ""
        }
    }
}