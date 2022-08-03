package com.anytypeio.anytype.core_ui.features.dataview.holders

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemViewerGridCellTagBinding
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.presentation.sets.model.CellView

class DVGridCellTagHolder(val binding: ItemViewerGridCellTagBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(cell: CellView.Tag) {
        for (i in 0..MAX_VISIBLE_TAGS_INDEX) getViewByIndex(i)?.gone()
        cell.tags.forEachIndexed { index, tagView ->
            when (index) {
                in 0..MAX_VISIBLE_TAGS_INDEX -> {
                    getViewByIndex(index)?.let { view ->
                        view.visible()
                        view.text = tagView.tag
                        val color = ThemeColor.values().find { v -> v.code == tagView.color }
                        val defaultTextColor = itemView.resources.getColor(R.color.text_primary, null)
                        val defaultBackground = itemView.resources.getColor(R.color.shape_primary, null)
                        if (color != null && color != ThemeColor.DEFAULT) {
                            view.background.setDrawableColor(itemView.resources.light(color, defaultBackground))
                            view.setTextColor(itemView.resources.dark(color, defaultTextColor))
                        } else {
                            view.background.setDrawableColor(defaultBackground)
                            view.setTextColor(defaultTextColor)
                        }
                    }
                }
            }
        }
    }

    private fun getViewByIndex(index: Int): TextView? = when (index) {
        0 -> binding.tag0
        1 -> binding.tag1
        2 -> binding.tag2
        3 -> binding.tag3
        4 -> binding.tag4
        else -> null
    }

    companion object {
        const val MAX_VISIBLE_TAGS_INDEX = 4
    }
}