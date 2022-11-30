package com.anytypeio.anytype.presentation.editor.editor.table

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.domain.table.CreateTableColumn
import com.anytypeio.anytype.domain.table.CreateTableRow
import com.anytypeio.anytype.domain.table.DeleteTableColumn
import com.anytypeio.anytype.domain.table.DeleteTableRow
import com.anytypeio.anytype.domain.table.DuplicateTableColumn
import com.anytypeio.anytype.domain.table.DuplicateTableRow
import com.anytypeio.anytype.domain.table.FillTableColumn
import com.anytypeio.anytype.domain.table.FillTableRow
import com.anytypeio.anytype.domain.table.MoveTableColumn
import com.anytypeio.anytype.domain.table.MoveTableRow
import com.anytypeio.anytype.domain.table.SetTableRowHeader
import com.anytypeio.anytype.presentation.util.Dispatcher
import timber.log.Timber

sealed class EditorTableEvent {

    sealed class Column : EditorTableEvent() {

        abstract val ctx: Id
        abstract val columns: List<Id>

        data class CreateLeft(override val ctx: Id, override val columns: List<Id>) : Column()
        data class CreateRight(override val ctx: Id, override val columns: List<Id>) : Column()
        data class Delete(override val ctx: Id, override val columns: List<Id>) : Column()
        data class Duplicate(override val ctx: Id, override val columns: List<Id>) : Column()
        data class MoveLeft(
            override val ctx: Id,
            override val columns: List<Id>,
            val targetDrop: Id?
        ) :
            Column()

        data class MoveRight(
            override val ctx: Id,
            override val columns: List<Id>,
            val targetDrop: Id?
        ) :
            Column()

        data class ClearContents(override val ctx: Id, override val columns: List<Id>) : Column()
        data class Style(override val ctx: Id, override val columns: List<Id>) : Column()
        data class ResetStyle(override val ctx: Id, override val columns: List<Id>) : Column()
        data class Color(override val ctx: Id, override val columns: List<Id>) : Column()
    }

    sealed class Row : EditorTableEvent() {

        abstract val ctx: Id
        abstract val rows: List<Id>

        data class CreateAbove(override val ctx: Id, override val rows: List<Id>) : Row()
        data class CreateBelow(override val ctx: Id, override val rows: List<Id>) : Row()
        data class Delete(override val ctx: Id, override val rows: List<Id>) : Row()
        data class Duplicate(override val ctx: Id, override val rows: List<Id>) : Row()
        data class MoveDown(
            override val ctx: Id,
            override val rows: List<Id>,
            val targetDrop: Id?
        ) : Row()

        data class MoveUp(override val ctx: Id, override val rows: List<Id>, val targetDrop: Id?) :
            Row()

        data class ClearContents(override val ctx: Id, override val rows: List<Id>) : Row()
        data class Style(override val ctx: Id, override val rows: List<Id>) : Row()
        data class ResetStyle(override val ctx: Id, override val rows: List<Id>) : Row()
        data class Color(override val ctx: Id, override val rows: List<Id>) : Row()
    }
}

interface EditorTableDelegate {
    suspend fun onEditorTableColumnEvent(event: EditorTableEvent.Column)
    suspend fun onEditorTableRowEvent(event: EditorTableEvent.Row)
}

class DefaultEditorTableDelegate(
    private val dispatcher: Dispatcher<Payload>,
    private val createTableColumn: CreateTableColumn,
    private val createTableRow: CreateTableRow,
    private val deleteTableColumn: DeleteTableColumn,
    private val deleteTableRow: DeleteTableRow,
    private val duplicateTableRow: DuplicateTableRow,
    private val duplicateTableColumn: DuplicateTableColumn,
    private val fillTableRow: FillTableRow,
    private val fillTableColumn: FillTableColumn,
    private val moveTableRow: MoveTableRow,
    private val moveTableColumn: MoveTableColumn,
    private val setTableRowHeader: SetTableRowHeader
) : EditorTableDelegate {

    override suspend fun onEditorTableColumnEvent(event: EditorTableEvent.Column) {
        fillTableColumn(
            params = FillTableColumn.Params(
                ctx = event.ctx,
                targetIds = event.columns
            )
        ).proceed(
            failure = { Timber.e("Error while fill table column:${it.message} ") },
            success = { payload ->
                dispatcher.send(payload)
                proceedWithTableColumnEvent(event)
            }
        )
    }

    override suspend fun onEditorTableRowEvent(event: EditorTableEvent.Row) {
        fillTableRow(
            params = FillTableRow.Params(
                ctx = event.ctx,
                targetIds = event.rows
            )
        ).proceed(
            failure = { Timber.e("Error while fill table row:${it.message} ") },
            success = { payload ->
                dispatcher.send(payload)
                proceedWithTableRowEvent(event)
            }
        )
    }

    private suspend fun proceedWithTableRowEvent(event: EditorTableEvent.Row) {
        when (event) {
            is EditorTableEvent.Row.CreateAbove -> {
                val params = CreateTableRow.Params(
                    ctx = event.ctx,
                    target = event.rows.first(),
                    position = Position.TOP
                )
                createTableRow.run(params).proceed(
                    failure = { Timber.e("Error while creating table row:${it.message} ") },
                    success = { payload -> dispatcher.send(payload) }
                )
            }
            is EditorTableEvent.Row.CreateBelow -> {
                val params = CreateTableRow.Params(
                    ctx = event.ctx,
                    target = event.rows.first(),
                    position = Position.BOTTOM
                )
                createTableRow.run(params).proceed(
                    failure = { Timber.e("Error while creating table row:${it.message} ") },
                    success = { payload -> dispatcher.send(payload) }
                )
            }
            is EditorTableEvent.Row.Delete -> {
                val params = DeleteTableRow.Params(
                    ctx = event.ctx,
                    target = event.rows.first()
                )
                deleteTableRow.run(params).proceed(
                    failure = { Timber.e("Error while deleting table row:${it.message} ") },
                    success = { payload -> dispatcher.send(payload) }
                )
            }
            is EditorTableEvent.Row.Duplicate -> {
                val params = DuplicateTableRow.Params(
                    ctx = event.ctx,
                    row = event.rows.first(),
                    targetDrop = event.rows.first(),
                    position = Position.BOTTOM
                )
                duplicateTableRow.run(params).proceed(
                    failure = { Timber.e("Error while duplicating table row:${it.message} ") },
                    success = { payload -> dispatcher.send(payload) }
                )
            }
            is EditorTableEvent.Row.MoveUp -> {
                if (event.rows.isEmpty() || event.targetDrop == null) {
                    Timber.e("Couldn't proceed with MoveUp command, row or drop is null ")
                    return
                }
                val params = MoveTableRow.Params(
                    context = event.ctx,
                    rowContext = event.ctx,
                    row = event.rows.first(),
                    targetDrop = event.targetDrop,
                    position = Position.TOP
                )
                moveTableRow.run(params).proceed(
                    failure = { Timber.e("Error while moving up table row:${it.message} ") },
                    success = { payload -> dispatcher.send(payload) }
                )
            }
            is EditorTableEvent.Row.MoveDown -> {
                if (event.rows.isEmpty() || event.targetDrop == null) {
                    Timber.e("Couldn't proceed with MoveDown command, row or drop is null ")
                    return
                }
                val params = MoveTableRow.Params(
                    context = event.ctx,
                    rowContext = event.ctx,
                    row = event.rows.first(),
                    targetDrop = event.targetDrop,
                    position = Position.BOTTOM
                )
                moveTableRow.run(params).proceed(
                    failure = { Timber.e("Error while moving down table row:${it.message} ") },
                    success = { payload -> dispatcher.send(payload) }
                )
            }
            is EditorTableEvent.Row.ClearContents -> {}
            is EditorTableEvent.Row.Color -> {}
            is EditorTableEvent.Row.ResetStyle -> {}
            is EditorTableEvent.Row.Style -> {}
        }
    }

    private suspend fun proceedWithTableColumnEvent(event: EditorTableEvent.Column) {
        when (event) {
            is EditorTableEvent.Column.CreateLeft -> {
                if (event.columns.isEmpty()) return
                val params = CreateTableColumn.Params(
                    ctx = event.ctx,
                    target = event.columns.first(),
                    position = Position.LEFT
                )
                createTableColumn.run(params).proceed(
                    failure = { Timber.e("Error while creating table column:${it.message} ") },
                    success = { payload -> dispatcher.send(payload) }
                )
            }
            is EditorTableEvent.Column.CreateRight -> {
                if (event.columns.isEmpty()) return
                val params = CreateTableColumn.Params(
                    ctx = event.ctx,
                    target = event.columns.first(),
                    position = Position.RIGHT
                )
                createTableColumn.run(params).proceed(
                    failure = { Timber.e("Error while creating table column:${it.message} ") },
                    success = { payload -> dispatcher.send(payload) }
                )
            }
            is EditorTableEvent.Column.Delete -> {
                if (event.columns.isEmpty()) return
                val params = DeleteTableColumn.Params(
                    ctx = event.ctx,
                    target = event.columns.first()
                )
                deleteTableColumn.run(params).proceed(
                    failure = { Timber.e("Error while deleting table column:${it.message} ") },
                    success = { payload -> dispatcher.send(payload) }
                )
            }
            is EditorTableEvent.Column.Duplicate -> {
                if (event.columns.isEmpty()) return
                val params = DuplicateTableColumn.Params(
                    ctx = event.ctx,
                    targetDrop = event.columns.first(),
                    column = event.columns.first(),
                    position = Position.RIGHT
                )
                duplicateTableColumn.run(params).proceed(
                    failure = { Timber.e("Error while duplicate table column:${it.message} ") },
                    success = { payload -> dispatcher.send(payload) }
                )
            }
            is EditorTableEvent.Column.MoveLeft -> {
                if (event.columns.isEmpty() || event.targetDrop == null) {
                    Timber.e("Couldn't proceed with MoveLeft command, column or drop is null ")
                    return
                }
                val params = MoveTableColumn.Params(
                    ctx = event.ctx,
                    targetDrop = event.targetDrop,
                    column = event.columns.first(),
                    position = Position.LEFT
                )
                moveTableColumn.run(params).proceed(
                    failure = { Timber.e("Error while moving table column:${it.message} ") },
                    success = { payload -> dispatcher.send(payload) }
                )
            }
            is EditorTableEvent.Column.MoveRight -> {
                if (event.columns.isEmpty() || event.targetDrop == null) {
                    Timber.e("Couldn't proceed with MoveRight command, column or drop is null ")
                    return
                }
                val params = MoveTableColumn.Params(
                    ctx = event.ctx,
                    targetDrop = event.targetDrop,
                    column = event.columns.first(),
                    position = Position.RIGHT
                )
                moveTableColumn.run(params).proceed(
                    failure = { Timber.e("Error while moving table column:${it.message} ") },
                    success = { payload -> dispatcher.send(payload) }
                )
            }
            is EditorTableEvent.Column.ClearContents -> {}
            is EditorTableEvent.Column.Style -> {}
            is EditorTableEvent.Column.Color -> {}
            is EditorTableEvent.Column.ResetStyle -> {}
        }
    }
}