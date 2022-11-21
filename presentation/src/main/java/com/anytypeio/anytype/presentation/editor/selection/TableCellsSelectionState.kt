package com.anytypeio.anytype.presentation.editor.selection

import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class TableCellsSelectionState {

    private var memory = mapOf<Int, CellSelection>()

    fun set(cells: List<BlockView.Table.Cell>, rowsSize: Int) {
        cells.forEach { cell ->
            memory = updateTableCellsSelectionState(
                cellId = cell.getId(),
                rowIndex = cell.rowIndex,
                columnIndex = cell.columnIndex,
                selectionState = current(),
                rowsSize = rowsSize
            )
        }
    }

    fun clear() {
        memory = emptyMap()
    }

    fun current(): Map<Int, BlockView.Table.CellSelection> = memory
}
