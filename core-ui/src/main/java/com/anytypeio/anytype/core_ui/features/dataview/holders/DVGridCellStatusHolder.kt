package com.anytypeio.anytype.core_ui.features.dataview.holders

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemViewerGridCellDescriptionBinding
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.sets.model.CellView

class DVGridCellStatusHolder(val binding: ItemViewerGridCellDescriptionBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(cell: CellView.Status) = with(binding) {
        val status = cell.status.firstOrNull()
        if (status != null) {
            tvText.text = status.status
            val color = ThemeColor.values().find { v -> v.code == status.color }
            val defaultTextColor = itemView.resources.getColor(R.color.text_primary, null)
            if (color != null && color != ThemeColor.DEFAULT) {
                tvText.setTextColor(itemView.resources.dark(color, defaultTextColor))
            } else {
                tvText.setTextColor(defaultTextColor)
            }
        } else {
            tvText.text = ""
        }
    }
}