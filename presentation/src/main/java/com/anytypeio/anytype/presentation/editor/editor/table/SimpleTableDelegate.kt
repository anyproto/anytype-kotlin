package com.anytypeio.anytype.presentation.editor.editor.table

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.scan
import timber.log.Timber

interface SimpleTableDelegate {
    val simpleTableDelegateState: Flow<SimpleTableWidgetState>
    suspend fun onSimpleTableEvent(event: SimpleTableWidgetEvent)
}

class DefaultSimpleTableDelegate : SimpleTableDelegate {

    private val events = MutableSharedFlow<SimpleTableWidgetEvent>(replay = 0)

    override val simpleTableDelegateState =
        events.scan(SimpleTableWidgetState.init()) { state, event ->
            when (event) {
                is SimpleTableWidgetEvent.onStart -> {
                    SimpleTableWidgetState.UpdateItems(
                        cellItems = listOf(
                            SimpleTableWidgetItem.Cell.ClearContents,
                            SimpleTableWidgetItem.Cell.Style,
                            SimpleTableWidgetItem.Cell.Color,
                            SimpleTableWidgetItem.Cell.ClearStyle
                        ),
                        rowItems = listOf(
                            SimpleTableWidgetItem.Row.ClearContents,
                            SimpleTableWidgetItem.Row.Color,
                            SimpleTableWidgetItem.Row.Style,
                            SimpleTableWidgetItem.Row.Delete,
                            SimpleTableWidgetItem.Row.MoveUp,
                            SimpleTableWidgetItem.Row.MoveDown,
                            SimpleTableWidgetItem.Row.InsertAbove,
                            SimpleTableWidgetItem.Row.InsertBelow,
                            SimpleTableWidgetItem.Row.Duplicate,
                            SimpleTableWidgetItem.Row.Sort
                        ),
                        columnItems = listOf(
                            SimpleTableWidgetItem.Column.ClearContents,
                            SimpleTableWidgetItem.Column.Color,
                            SimpleTableWidgetItem.Column.Style,
                            SimpleTableWidgetItem.Column.Delete,
                            SimpleTableWidgetItem.Column.InsertLeft,
                            SimpleTableWidgetItem.Column.InsertRight,
                            SimpleTableWidgetItem.Column.MoveLeft,
                            SimpleTableWidgetItem.Column.MoveRight,
                            SimpleTableWidgetItem.Column.Sort,
                            SimpleTableWidgetItem.Column.Duplicate
                        )
                    )
                }
            }
        }.catch { e ->
            Timber.e(e, "Error while processing simple table ")
        }

    override suspend fun onSimpleTableEvent(event: SimpleTableWidgetEvent) {
        events.emit(event)
    }
}