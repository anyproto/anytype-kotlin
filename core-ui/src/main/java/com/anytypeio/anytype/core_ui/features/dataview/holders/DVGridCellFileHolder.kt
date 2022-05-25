package com.anytypeio.anytype.core_ui.features.dataview.holders

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemViewerGridCellFileBinding
import com.anytypeio.anytype.core_ui.widgets.GridCellFileItem
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.model.CellView

class DVGridCellFileHolder(val binding: ItemViewerGridCellFileBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(cell: CellView.File) {
        for (i in 0..MAX_VISIBLE_FILES_INDEX) getViewByIndex(i)?.gone()
        cell.files.forEachIndexed { index, fileView ->
            when (index) {
                in 0..MAX_VISIBLE_FILES_INDEX -> {
                    getViewByIndex(index)?.let { view ->
                        view.visible()
                        view.setup(name = fileView.name, mime = fileView.mime)
                    }
                }
            }
        }
    }

    private fun getViewByIndex(index: Int): GridCellFileItem? = when (index) {
        0 -> binding.file0
        1 -> binding.file1
        2 -> binding.file2
        else -> null
    }

    companion object {
        const val MAX_VISIBLE_FILES_INDEX = 2
    }
}