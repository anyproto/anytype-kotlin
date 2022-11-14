package com.anytypeio.anytype.presentation.editor.selection

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem

fun updateTableCellsSelectionState(
    cellId: Id,
    rowIndex: BlockView.Table.RowIndex,
    columnIndex: BlockView.Table.ColumnIndex,
    selectionState: List<BlockView.Table.CellSelection>,
    cellIndex: Int
): List<BlockView.Table.CellSelection> {

    val latestSelection = BlockView.Table.CellSelection(
        cellId = cellId,
        rowIndex = rowIndex,
        columnIndex = columnIndex,
        left = true,
        top = true,
        right = true,
        bottom = true,
        cellIndex = cellIndex
    )

    val latestSelectionIndex = selectionState.indexOfFirst { it.cellId == cellId }
    return if (latestSelectionIndex != -1) {
        //Latest selection is already in selection state - move on to deselecting a cell
        val newState = selectionState.toMutableList()
        newState.removeAt(latestSelectionIndex)
        newState.restoreBordersVisibility(deselectedSelection = latestSelection)
        newState
    } else {
        //Latest selection is not in selection state - move on to adding new selection to state
        selectionState.forEach { selection ->
            if (isLeftBorderMatch(cell1 = selection, cell2 = latestSelection)) {
                selection.left = false
                latestSelection.right = false
            }

            if (isRightBorderMatch(cell1 = selection, cell2 = latestSelection)) {
                selection.right = false
                latestSelection.left = false
            }

            if (isTopBorderMatch(cell1 = selection, cell2 = latestSelection)) {
                selection.top = false
                latestSelection.bottom = false
            }

            if (isBottomBorderMatch(cell1 = selection, cell2 = latestSelection)) {
                selection.bottom = false
                latestSelection.top = false
            }
        }
        val newState = mutableListOf<BlockView.Table.CellSelection>().apply {
            addAll(selectionState)
            add(latestSelection)
        }
        newState
    }
}

/**
 * In the case of deselecting the cell it is necessary
 * to restore the borders of the remaining selected cells
 */
fun List<BlockView.Table.CellSelection>.restoreBordersVisibility(deselectedSelection: BlockView.Table.CellSelection): List<BlockView.Table.CellSelection> =
    map { cellSelection ->
        if (isLeftBorderMatch(cell1 = cellSelection, cell2 = deselectedSelection)) {
            cellSelection.left = true
        }
        if (isRightBorderMatch(cell1 = cellSelection, cell2 = deselectedSelection)) {
            cellSelection.right = true
        }
        if (isTopBorderMatch(cell1 = cellSelection, cell2 = deselectedSelection)) {
            cellSelection.top = true
        }
        if (isBottomBorderMatch(cell1 = cellSelection, cell2 = deselectedSelection)) {
            cellSelection.bottom = true
        }
        cellSelection
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

fun List<BlockView.Table.Cell>.getSimpleTableWidgetItems(): List<SimpleTableWidgetItem> {
    return listOf(
        SimpleTableWidgetItem.Cell.ClearContents,
        SimpleTableWidgetItem.Cell.Color,
        SimpleTableWidgetItem.Cell.Style,
        SimpleTableWidgetItem.Cell.ClearStyle
    )
}