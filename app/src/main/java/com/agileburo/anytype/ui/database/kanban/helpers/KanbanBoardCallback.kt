package com.agileburo.anytype.ui.database.kanban.helpers

import com.woxthebox.draglistview.BoardView

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-09-10.
 */
class KanbanBoardCallback : BoardView.BoardCallback {
    override fun canDragItemAtPosition(column: Int, row: Int): Boolean = true

    override fun canDropItemAtPosition(
        oldColumn: Int,
        oldRow: Int,
        newColumn: Int,
        newRow: Int
    ): Boolean = true
}