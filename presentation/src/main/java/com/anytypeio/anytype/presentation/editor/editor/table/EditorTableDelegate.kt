package com.anytypeio.anytype.presentation.editor.editor.table

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.domain.table.CreateTableColumn
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.util.Dispatcher
import timber.log.Timber

sealed class EditorTableEvent {
    data class CreateColumn(val ctx: Id, val target: Id, val position: Position) :
        EditorTableEvent()
}

interface EditorTableDelegate {
    suspend fun onEditorTableEvent(event: EditorTableEvent)
}

class DefaultEditorTableDelegate(
    private val dispatcher: Dispatcher<Payload>,
    private val createTableColumn: CreateTableColumn
) : EditorTableDelegate {


    override suspend fun onEditorTableEvent(event: EditorTableEvent) {
        when (event) {
            is EditorTableEvent.CreateColumn -> {
                val params = CreateTableColumn.Params(
                    ctx = event.ctx,
                    target = event.target,
                    position = event.position
                )
                createTableColumn.run(params).proceed(
                    failure = { Timber.e("Error while creating table column:${it.message} ") },
                    success = { payload -> dispatcher.send(payload) }
                )
            }
        }
    }
}