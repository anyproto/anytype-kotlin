package com.anytypeio.anytype.core_ui.tools

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_utils.ui.ItemTouchHelperViewHolder
import timber.log.Timber

/**
 * Implements a default drag-and-drop behavior for dragging items up or down, left or right.
 * Item swiping is disabled in [getMovementFlags]
 * It is compatible with [GridLayoutManager] and [LinearLayoutManager]
 * @property onItemMoved callback notifying that the dragged item has been moved from one position to another
 * @property onItemDropped callback notifying that the dragged item has been dropped
 */
open class DefaultDragAndDropBehavior(
    private val onItemMoved: (Int, Int) -> Boolean,
    private val onItemDropped: (Int) -> Unit
) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled() = true

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return if (recyclerView.layoutManager is GridLayoutManager)
            makeMovementFlags(UP or DOWN or LEFT or RIGHT, 0)
        else
            makeMovementFlags(UP or DOWN, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = onItemMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        Timber.d("Adapter position: ${viewHolder.bindingAdapterPosition}")
        onItemDropped(viewHolder.bindingAdapterPosition)
        if (viewHolder is ItemTouchHelperViewHolder) {
            viewHolder.onItemClear()
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState != ACTION_STATE_IDLE) {
            if (viewHolder is ItemTouchHelperViewHolder) {
                viewHolder.onItemSelected()
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}