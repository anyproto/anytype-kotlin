package com.anytypeio.anytype.presentation.editor.selection

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem.Column.InsertRight
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem.Column.InsertLeft
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem.Column.MoveLeft
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem.Column.MoveRight
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem.Row.InsertAbove
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem.Row.InsertBelow
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem.Row.MoveDown
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem.Row.MoveUp

typealias CellSelection = BlockView.Table.CellSelection

fun updateTableCellsSelectionState(
    cellId: Id,
    rowIndex: BlockView.Table.RowIndex,
    columnIndex: BlockView.Table.ColumnIndex,
    selectionState: Map<Int, BlockView.Table.CellSelection>,
    rowsSize: Int
): Map<Int, BlockView.Table.CellSelection> {

    val latestSelection = BlockView.Table.CellSelection(
        cellId = cellId,
        rowIndex = rowIndex,
        columnIndex = columnIndex,
        left = true,
        top = true,
        right = true,
        bottom = true,
    )
    val resultMap = selectionState.toMutableMap()
    val cellIndex = columnIndex.value * rowsSize + rowIndex.value
    return if (selectionState.contains(cellIndex)) {
        resultMap.remove(cellIndex)
        resultMap.restoreBordersVisibility(deselectedSelection = latestSelection)
    } else {
        resultMap.addCellSelection(newSelection = latestSelection, cellIndex = cellIndex)
    }
}

/**
 * In the case of deselecting the cell it is necessary
 * to restore the borders of the remaining selected cells
 */

fun Map<Int, CellSelection>.restoreBordersVisibility(deselectedSelection: CellSelection): Map<Int, CellSelection> {
    return this.mapValues {
        if (isLeftBorderMatch(cell1 = it.value, cell2 = deselectedSelection)) {
            it.value.left = true
        }
        if (isRightBorderMatch(cell1 = it.value, cell2 = deselectedSelection)) {
            it.value.right = true
        }
        if (isTopBorderMatch(cell1 = it.value, cell2 = deselectedSelection)) {
            it.value.top = true
        }
        if (isBottomBorderMatch(cell1 = it.value, cell2 = deselectedSelection)) {
            it.value.bottom = true
        }
        it.value
    }
}

fun MutableMap<Int, CellSelection>.addCellSelection(
    newSelection: CellSelection,
    cellIndex: Int
): Map<Int, CellSelection> {
    this.mapValues {
        if (isLeftBorderMatch(cell1 = it.value, cell2 = newSelection)) {
            it.value.left = false
            newSelection.right = false
        }

        if (isRightBorderMatch(cell1 = it.value, cell2 = newSelection)) {
            it.value.right = false
            newSelection.left = false
        }

        if (isTopBorderMatch(cell1 = it.value, cell2 = newSelection)) {
            it.value.top = false
            newSelection.bottom = false
        }

        if (isBottomBorderMatch(cell1 = it.value, cell2 = newSelection)) {
            it.value.bottom = false
            newSelection.top = false
        }
    }
    this[cellIndex] = newSelection
    return this
}

/**
 *  Check that the left border of the first cell coincides with the right border of the second cell
 *  |cell2|cell1|
 */
fun isLeftBorderMatch(
    cell1: BlockView.Table.CellSelection,
    cell2: BlockView.Table.CellSelection
): Boolean {
    val isSameRow = cell1.rowIndex.value == cell2.rowIndex.value
    if (!isSameRow) return false
    return cell1.columnIndex.value == (cell2.columnIndex.value + 1)
}

/**
 *  Check that the right border of the first cell coincides with the left border of the second cell
 *  |cell1|cell2|
 */
fun isRightBorderMatch(
    cell1: BlockView.Table.CellSelection,
    cell2: BlockView.Table.CellSelection
): Boolean {
    val isSameRow = cell1.rowIndex.value == cell2.rowIndex.value
    if (!isSameRow) return false
    return cell2.columnIndex.value == (cell1.columnIndex.value + 1)
}

/**
 *  Check that the bottom border of the first cell coincides with the top border of the second cell
 *  cell1
 *  ------
 *  cell2
 */
fun isBottomBorderMatch(
    cell1: BlockView.Table.CellSelection,
    cell2: BlockView.Table.CellSelection
): Boolean {
    val isSameColumn = cell1.columnIndex.value == cell2.columnIndex.value
    if (!isSameColumn) return false
    return cell1.rowIndex.value + 1 == cell2.rowIndex.value
}

/**
 *  Check that the top border of the first cell coincides with the top bottom of the second cell
 *  cell2
 *  ------
 *  cell1
 */
fun isTopBorderMatch(
    cell1: BlockView.Table.CellSelection,
    cell2: BlockView.Table.CellSelection
): Boolean {
    val isSameColumn = cell1.columnIndex.value == cell2.columnIndex.value
    if (!isSameColumn) return false
    return cell1.rowIndex.value == cell2.rowIndex.value + 1
}

fun List<BlockView>.toggleTableMode(
    cellsMode: BlockView.Mode,
    selectedCellsIds: List<Id>
): List<BlockView> {
    return map { view ->
        when (view) {
            is BlockView.Text.Paragraph -> view.copy(
                mode = cellsMode,
                isSelected = false,
                isFocused = false,
                cursor = null
            )
            is BlockView.Text.Checkbox -> view.copy(
                mode = cellsMode,
                isSelected = false,
                isFocused = false,
                cursor = null
            )
            is BlockView.Text.Bulleted -> view.copy(
                mode = cellsMode,
                isSelected = false,
                isFocused = false,
                cursor = null
            )
            is BlockView.Text.Numbered -> view.copy(
                mode = cellsMode,
                isSelected = false,
                isFocused = false,
                cursor = null
            )
            is BlockView.Text.Highlight -> view.copy(
                mode = cellsMode,
                isSelected = false,
                isFocused = false,
                cursor = null
            )
            is BlockView.Text.Callout -> view.copy(
                mode = cellsMode,
                isSelected = false,
                isFocused = false,
                cursor = null
            )
            is BlockView.Text.Header.One -> view.copy(
                mode = cellsMode,
                isSelected = false,
                isFocused = false,
                cursor = null
            )
            is BlockView.Text.Header.Two -> view.copy(
                mode = cellsMode,
                isSelected = false,
                isFocused = false,
                cursor = null
            )
            is BlockView.Text.Header.Three -> view.copy(
                mode = cellsMode,
                isSelected = false,
                isFocused = false,
                cursor = null
            )
            is BlockView.Text.Toggle -> view.copy(
                mode = cellsMode,
                isSelected = false,
                isFocused = false,
                cursor = null
            )
            is BlockView.Code -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.Error.File -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.Error.Video -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.Error.Picture -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.Error.Bookmark -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.Upload.File -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.Upload.Video -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.Upload.Picture -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.MediaPlaceholder.File -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.MediaPlaceholder.Video -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.MediaPlaceholder.Bookmark -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.MediaPlaceholder.Picture -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.Media.File -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.Media.Video -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.Media.Bookmark -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.Media.Picture -> view.copy(
                mode = cellsMode,
                isSelected = false
            )
            is BlockView.Title.Basic -> view.copy(
                mode = cellsMode
            )
            is BlockView.Title.Profile -> view.copy(
                mode = cellsMode
            )
            is BlockView.Title.Todo -> view.copy(
                mode = cellsMode
            )
            is BlockView.Title.Archive -> view.copy(
                mode = cellsMode
            )
            is BlockView.Description -> view.copy(
                mode = cellsMode
            )
            is BlockView.Relation.Placeholder -> view.copy(
                isSelected = false
            )
            is BlockView.Relation.Related -> view.copy(
                isSelected = false
            )
            is BlockView.LinkToObject.Default.Text -> view.copy(
                isSelected = false
            )
            is BlockView.LinkToObject.Default.Card.SmallIcon -> view.copy(
                isSelected = false
            )
            is BlockView.LinkToObject.Default.Card.MediumIcon -> view.copy(
                isSelected = false
            )
            is BlockView.LinkToObject.Default.Card.SmallIconCover -> view.copy(
                isSelected = false
            )
            is BlockView.LinkToObject.Default.Card.MediumIconCover -> view.copy(
                isSelected = false
            )
            is BlockView.LinkToObject.Archived -> view.copy(
                isSelected = false
            )
            is BlockView.LinkToObject.Deleted -> view.copy(
                isSelected = false
            )
            is BlockView.LinkToObject.Loading -> view.copy(
                isSelected = false
            )
            is BlockView.DividerDots -> view.copy(
                isSelected = false
            )
            is BlockView.DividerLine -> view.copy(
                isSelected = false
            )
            is BlockView.Latex -> view.copy(
                isSelected = false
            )
            is BlockView.TableOfContents -> view.copy(
                isSelected = false
            )
            is BlockView.Table -> {
                view.copy(
                    isSelected = false,
                    cells = view.cells.updateCellsMode(mode = cellsMode),
                    selectedCellsIds = selectedCellsIds
                )
            }
            is BlockView.FeaturedRelation -> view
            is BlockView.Unsupported -> view.copy(isSelected = false)
            is BlockView.Upload.Bookmark -> view.copy(isSelected = false)
            is BlockView.Relation.Deleted -> view.copy(isSelected = false)
        }
    }
}

fun List<BlockView.Table.Cell>.updateCellsMode(
    mode: BlockView.Mode
): List<BlockView.Table.Cell> = map { cell ->
    val block = cell.block
    if (block == null) {
        cell
    } else {
        cell.copy(
            block = block.copy(
                mode = mode,
                isSelected = false,
                isFocused = false,
                cursor = null
            )
        )
    }
}

fun List<BlockView>.updateTableBlockSelection(tableId: Id, selection: List<Id>): List<BlockView> =
    map {
        if (it.id == tableId && it is BlockView.Table) {
            it.copy(
                selectedCellsIds = selection
            )
        } else {
            it
        }
    }

fun List<BlockView>.updateTableBlockTab(
    tableId: Id,
    selection: List<Id>,
    tab: BlockView.Table.Tab
): List<BlockView> =
    map {
        if (it.id == tableId && it is BlockView.Table) {
            it.copy(
                selectedCellsIds = selection,
                tab = tab
            )
        } else {
            it
        }
    }

fun getSimpleTableWidgetCellItems(): List<SimpleTableWidgetItem> {
    return listOf(
        SimpleTableWidgetItem.Cell.ClearContents,
        SimpleTableWidgetItem.Cell.Color,
        SimpleTableWidgetItem.Cell.Style,
        SimpleTableWidgetItem.Cell.ResetStyle
    )
}

fun getSimpleTableWidgetColumnItems(
    selectedColumns: Set<BlockView.Table.Column>,
    columnsSize: Int
): List<SimpleTableWidgetItem> {
    return mutableListOf<SimpleTableWidgetItem>().apply {
        if (selectedColumns.size == 1) {
            addAll(selectedColumns.first().getTableWidgetItemsByColumn(columnsSize))
        }
        if (selectedColumns.isNotEmpty()) {
            val ids = selectedColumns.map { it.id }
            addAll(
                listOf(
                    SimpleTableWidgetItem.Column.ClearContents(ids),
                    SimpleTableWidgetItem.Column.Color(ids),
                    SimpleTableWidgetItem.Column.Style(ids),
                    SimpleTableWidgetItem.Column.ResetStyle(ids)
                )
            )
        }
    }
}

fun getSimpleTableWidgetRowItems(
    selectedRows: Set<BlockView.Table.Row>,
    rowsSize: Int
): List<SimpleTableWidgetItem> {
    return mutableListOf<SimpleTableWidgetItem>().apply {
        if (selectedRows.size == 1) {
            addAll(selectedRows.first().getTableWidgetItemsByRow(rowsSize))
        }
        if (selectedRows.isNotEmpty()) {
            val ids = selectedRows.map { it.id }
            addAll(
                listOf(
                    SimpleTableWidgetItem.Row.ClearContents(ids),
                    SimpleTableWidgetItem.Row.Color(ids),
                    SimpleTableWidgetItem.Row.Style(ids),
                    SimpleTableWidgetItem.Row.ResetStyle(ids)
                )
            )
        }
    }
}

fun BlockView.Table.Column.getTableWidgetItemsByColumn(
    columnsSize: Int
): List<SimpleTableWidgetItem> = mutableListOf<SimpleTableWidgetItem>().apply {
    val column = this@getTableWidgetItemsByColumn
    add(InsertLeft(id))
    add(InsertRight(id))
    if (index.value > 0) add(MoveLeft(column = column))
    if (index.value < columnsSize - 1) add(MoveRight(column = column))
    add(SimpleTableWidgetItem.Column.Duplicate(id))
    add(SimpleTableWidgetItem.Column.Delete(id))
}

fun BlockView.Table.Row.getTableWidgetItemsByRow(
    rowSize: Int
): List<SimpleTableWidgetItem> = mutableListOf<SimpleTableWidgetItem>().apply {
    val row = this@getTableWidgetItemsByRow
    add(InsertAbove(id))
    add(InsertBelow(id))
    if (index.value > 0) add(MoveUp(row = row))
    if (index.value < rowSize - 1) add(MoveDown(row = row))
    add(SimpleTableWidgetItem.Row.Delete(id))
    add(SimpleTableWidgetItem.Row.Duplicate(id))
}

fun BlockView.Table.getIdsInRow(index: BlockView.Table.RowIndex): List<Id> =
    this.cells.mapNotNull {
        if (it.row.index == index) {
            it.getId()
        } else {
            null
        }
    }

fun BlockView.Table.getIdsInColumn(index: BlockView.Table.ColumnIndex): List<Id> =
    this.cells.mapNotNull {
        if (it.column.index == index) {
            it.getId()
        } else {
            null
        }
    }

fun BlockView.Table.getAllSelectedColumns(selectedCellsIds: Set<Id>): SelectedColumns {
    val selectedCells = cells.filter { selectedCellsIds.contains(it.getId()) }
    val selectedColumnCells = mutableSetOf<Id>()
    val selectedColumns = mutableSetOf<BlockView.Table.Column>()
    selectedCells.forEach { cell ->
        selectedColumns.add(cell.column)
        selectedColumnCells.addAll(getIdsInColumn(cell.column.index))
    }
    return SelectedColumns(
        cellsInColumns = selectedColumnCells.toList(),
        selectedColumns = selectedColumns
    )
}

fun BlockView.Table.getAllSelectedRows(selectedCellsIds: Set<Id>): SelectedRows {
    val selectedCells = cells.filter { selectedCellsIds.contains(it.getId()) }
    val selectedRowCells = mutableSetOf<Id>()
    val selectedRows = mutableSetOf<BlockView.Table.Row>()
    selectedCells.forEach { cell ->
        selectedRows.add(cell.row)
        selectedRowCells.addAll(getIdsInRow(cell.row.index))
    }
    return SelectedRows(
        cellsInRows = selectedRowCells.toList(), selectedRows = selectedRows
    )
}

data class SelectedRows(val cellsInRows: List<Id>, val selectedRows: Set<BlockView.Table.Row>)
data class SelectedColumns(
    val cellsInColumns: List<Id>,
    val selectedColumns: Set<BlockView.Table.Column>
)

data class TableRowsByIndex(
    val row: BlockView.Table.Row,
    val rowTop: BlockView.Table.Row?,
    val rowBottom: BlockView.Table.Row?
)

data class TableColumnsByIndex(
    val column: BlockView.Table.Column,
    val columnLeft: BlockView.Table.Column?,
    val columnRight: BlockView.Table.Column?
)

fun List<BlockView>.getTableRowsById(
    mode: Editor.Mode,
    row: BlockView.Table.Row
): TableRowsByIndex {
    val tableBlockId = (mode as? Editor.Mode.Table)?.tableId
    val tableBlock = find { it.id == tableBlockId }
    val rows = (tableBlock as? BlockView.Table)?.rows
    val rowTop = rows?.getOrNull(row.index.value - 1)
    val rowBottom = rows?.getOrNull(row.index.value + 1)
    return TableRowsByIndex(
        row = row,
        rowTop = rowTop,
        rowBottom = rowBottom
    )
}

fun List<BlockView>.getTableColumnsById(
    mode: Editor.Mode,
    column: BlockView.Table.Column
): TableColumnsByIndex {
    val tableBlockId = (mode as? Editor.Mode.Table)?.tableId
    val tableBlock = find { it.id == tableBlockId }
    val columns = (tableBlock as? BlockView.Table)?.columns
    val columnLeft = columns?.getOrNull(column.index.value - 1)
    val columnRight = columns?.getOrNull(column.index.value + 1)
    return TableColumnsByIndex(
        column = column,
        columnLeft = columnLeft,
        columnRight = columnRight
    )
}