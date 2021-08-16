package com.anytypeio.anytype.core_ui.features.dataview.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.widgets.GridCellObjectItem
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.model.CellView
import kotlinx.android.synthetic.main.item_viewer_grid_cell_object.view.*

class DVGridCellObjectHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(cell: CellView.Object) = with(itemView) {
        for (i in 0..MAX_VISIBLE_OBJECTS_INDEX) getViewByIndex(i)?.gone()
        cell.objects.forEachIndexed { index, objectView ->
            when (index) {
                in 0..MAX_VISIBLE_OBJECTS_INDEX -> {
                    getViewByIndex(index)?.let { view ->
                        view.visible()
                        view.setup(name = objectView.name, icon = objectView.icon)
                    }
                }
            }
        }
    }

    private fun getViewByIndex(index: Int): GridCellObjectItem? = when (index) {
        0 -> itemView.object0
        1 -> itemView.object1
        2 -> itemView.object2
        3 -> itemView.object3
        else -> null
    }

    companion object {
        const val MAX_VISIBLE_OBJECTS_INDEX = 3
    }
}