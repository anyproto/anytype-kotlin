package com.anytypeio.anytype.ui.database.modals.helpers

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_utils.ui.ItemTouchHelperAdapter

class DetailsTouchHelper(
    dragDirs: Int,
    private val adapter: ItemTouchHelperAdapter
) :
    ItemTouchHelper.SimpleCallback(dragDirs, 0) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
}