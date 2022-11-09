package com.anytypeio.anytype.presentation.editor.selection

import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class TableCellsSelectionState {

    private var memory = listOf<BlockView.Table.CellSelection>()

    fun set(cells: List<BlockView.Table.Cell>) {
        cells.forEach { cell ->
            val currentSelection = current()
            memory = updateTableCellsSelectionState(
                cellId = cell.getId(),
                rowIndex = cell.rowIndex,
                columnIndex = cell.columnIndex,
                selectionState = currentSelection,
                cellIndex = cell.cellIndex
            )
        }
    }

    fun clear() {
        memory = emptyList()
    }

    fun current(): List<BlockView.Table.CellSelection> = memory
}
