package com.anytypeio.anytype.ui.desktop

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.tools.DefaultDragAndDropBehavior

class DashboardDragAndDropBehavior(
    onItemMoved: (Int, Int) -> Boolean,
    onItemDropped: (Int) -> Unit
) : DefaultDragAndDropBehavior(onItemMoved, onItemDropped) {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return if (viewHolder is ProfileContainerAdapter.ProfileContainerHolder)
            makeMovementFlags(0, 0)
        else
            super.getMovementFlags(recyclerView, viewHolder)
    }
}