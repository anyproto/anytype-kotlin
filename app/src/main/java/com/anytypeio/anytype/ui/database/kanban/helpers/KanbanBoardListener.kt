package com.anytypeio.anytype.ui.database.kanban.helpers

import com.woxthebox.draglistview.BoardView
import timber.log.Timber

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-09-10.
 */
class KanbanBoardListener : BoardView.BoardListener {

    override fun onColumnDragChangedPosition(oldPosition: Int, newPosition: Int) {
        Timber.d("onColumnDragChangedPosition ")
    }

    override fun onItemChangedColumn(oldColumn: Int, newColumn: Int) {
        Timber.d("onItemChangedColumn ")
    }

    override fun onColumnDragStarted(position: Int) {
        Timber.d("onColumnDragStarted position:$position")
    }

    override fun onFocusedColumnChanged(oldColumn: Int, newColumn: Int) {
        Timber.d("onFocusedColumnChanged ")
    }

    override fun onItemDragEnded(
        fromColumn: Int,
        fromRow: Int,
        toColumn: Int,
        toRow: Int
    ) {
        Timber.d("onItemDragEnded ")
    }

    override fun onItemChangedPosition(
        oldColumn: Int,
        oldRow: Int,
        newColumn: Int,
        newRow: Int
    ) {
        Timber.d("onItemChangedPosition ")
    }

    override fun onColumnDragEnded(position: Int) {
        Timber.d("onColumnDragEnded ")
    }

    override fun onItemDragStarted(column: Int, row: Int) {
        Timber.d("onItemDragStarted ")
    }
}