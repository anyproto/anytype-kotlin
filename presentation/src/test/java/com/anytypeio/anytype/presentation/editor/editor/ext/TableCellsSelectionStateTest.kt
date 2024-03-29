package com.anytypeio.anytype.presentation.editor.editor.ext

import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.selection.CellSelection
import com.anytypeio.anytype.presentation.editor.selection.TableCellsSelectionState
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Test
import kotlin.test.assertEquals

class TableCellsSelectionStateTest {

    private val tableId = MockDataFactory.randomUuid()

    private val rowId0 = MockDataFactory.randomUuid()
    private val rowId1 = MockDataFactory.randomUuid()
    private val rowId2 = MockDataFactory.randomUuid()
    private val columnId0 = MockDataFactory.randomUuid()
    private val columnId1 = MockDataFactory.randomUuid()
    private val columnId2 = MockDataFactory.randomUuid()

    @Test
    fun `when click on cell expecting selected state with this cell`() {

        //SETUP
        val cellsSelectionState = TableCellsSelectionState()

        //TESTING
        //click on cell row1, column1
        cellsSelectionState.set(
            cells = listOf(
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId1),
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId1),
                        index = BlockView.Table.ColumnIndex(1)
                    ),
                    block = null,
                    tableId = tableId
                )
            ),
            rowsSize = 3
        )

        val actual = cellsSelectionState.current()

        //EXPECTED
        val expected = mapOf(
            4 to CellSelection(
                cellId = "$rowId1-$columnId1",
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(1),
                left = true,
                top = true,
                right = true,
                bottom = true,
            )
        )

        //ASSERT
        assertEquals(expected, actual)
    }

    @Test
    fun `when clicking on the same sell expecting empty state`() {

        //SETUP
        val cellsSelectionState = TableCellsSelectionState()

        //TESTING
        //click on cell row1, column1
        cellsSelectionState.set(
            cells = listOf(
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId1),
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId1),
                        index = BlockView.Table.ColumnIndex(1)
                    ),
                    block = null,
                    tableId = tableId,
                )
            ),
            rowsSize = 3
        )

        //second click on cell row1, column1
        cellsSelectionState.set(
            cells = listOf(
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId1),
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId1),
                        index = BlockView.Table.ColumnIndex(1)
                    ),
                    block = null,
                    tableId = tableId
                )
            ),
            rowsSize = 3
        )

        val actual = cellsSelectionState.current()

        //EXPECTED
        val expected = emptyMap<Int, CellSelection>()

        //ASSERT
        assertEquals(expected, actual)
    }

    @Test
    fun `when clicking on different cells expecting proper borders visibility`() {

        //SETUP
        val cellsSelectionState = TableCellsSelectionState()

        //TESTING
        //click on cell row1, column0
        cellsSelectionState.set(
            cells = listOf(
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId1),
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId0),
                        index = BlockView.Table.ColumnIndex(0)
                    ),
                    block = null,
                    tableId = tableId
                )
            ),
            rowsSize = 3
        )
        //click on cell row1, column2
        cellsSelectionState.set(
            cells = listOf(
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId1),
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId2),
                        index = BlockView.Table.ColumnIndex(2)
                    ),
                    block = null,
                    tableId = tableId
                )
            ),
            rowsSize = 3
        )

        //click on cell row2, column0
        cellsSelectionState.set(
            cells = listOf(
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId2),
                        index = BlockView.Table.RowIndex(2),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId0),
                        index = BlockView.Table.ColumnIndex(0)
                    ),
                    block = null,
                    tableId = tableId
                )
            ),
            rowsSize = 3
        )

        //click on cell row0, column2
        cellsSelectionState.set(
            cells = listOf(
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId0),
                        index = BlockView.Table.RowIndex(0),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId2),
                        index = BlockView.Table.ColumnIndex(2)
                    ),
                    block = null,
                    tableId = tableId
                )
            ),
            rowsSize = 3
        )

        //click on cell row1, column1
        cellsSelectionState.set(
            cells = listOf(
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId1),
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId1),
                        index = BlockView.Table.ColumnIndex(1)
                    ),
                    block = null,
                    tableId = tableId
                )
            ),
            rowsSize = 3
        )

        val actual = cellsSelectionState.current()

        //EXPECTED
        val expected = mapOf(
            1 to CellSelection(
                cellId = "$rowId1-$columnId0",
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(0),
                left = true,
                top = true,
                right = false,
                bottom = false
            ),
            7 to CellSelection(
                cellId = "$rowId1-$columnId2",
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(2),
                left = false,
                top = false,
                right = true,
                bottom = true,
            ),
            2 to CellSelection(
                cellId = "$rowId2-$columnId0",
                rowIndex = BlockView.Table.RowIndex(2),
                columnIndex = BlockView.Table.ColumnIndex(0),
                left = true,
                top = false,
                right = true,
                bottom = true,
            ),
            6 to CellSelection(
                cellId = "$rowId0-$columnId2",
                rowIndex = BlockView.Table.RowIndex(0),
                columnIndex = BlockView.Table.ColumnIndex(2),
                left = true,
                top = true,
                right = true,
                bottom = false,
            ),
            4 to CellSelection(
                cellId = "$rowId1-$columnId1",
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(1),
                left = false,
                top = true,
                right = false,
                bottom = true,
            )
        )

        //ASSERT
        assertEquals(5, actual.size, message = "Size of selected cells expected 5")
        assertEquals(expected, actual)

        //TESTING
        //click on cell row1, column1
        cellsSelectionState.set(
            cells = listOf(
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId1),
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId1),
                        index = BlockView.Table.ColumnIndex(1)
                    ),
                    block = null,
                    tableId = tableId
                )
            ),
            rowsSize = 3
        )

        val actual2 = cellsSelectionState.current()

        //EXPECTED
        val expected2 = mapOf(
            1 to CellSelection(
                cellId = "$rowId1-$columnId0",
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(0),
                left = true,
                top = true,
                right = true,
                bottom = false
            ),
            7 to CellSelection(
                cellId = "$rowId1-$columnId2",
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(2),
                left = true,
                top = false,
                right = true,
                bottom = true
            ),
            2 to CellSelection(
                cellId = "$rowId2-$columnId0",
                rowIndex = BlockView.Table.RowIndex(2),
                columnIndex = BlockView.Table.ColumnIndex(0),
                left = true,
                top = false,
                right = true,
                bottom = true
            ),
            6 to CellSelection(
                cellId = "$rowId0-$columnId2",
                rowIndex = BlockView.Table.RowIndex(0),
                columnIndex = BlockView.Table.ColumnIndex(2),
                left = true,
                top = true,
                right = true,
                bottom = false
            )
        )

        //ASSERT
        assertEquals(4, actual2.size, message = "Size of selected cells expected 4")
        assertEquals(expected2, actual2)

        //TESTING
        //click on cell row0, column2
        cellsSelectionState.set(
            cells = listOf(
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId0),
                        index = BlockView.Table.RowIndex(0),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId2),
                        index = BlockView.Table.ColumnIndex(2)
                    ),
                    block = null,
                    tableId = tableId
                )
            ),
            rowsSize = 3
        )
        //click on cell row0, column2
        cellsSelectionState.set(
            cells = listOf(
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId2),
                        index = BlockView.Table.RowIndex(2),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId0),
                        index = BlockView.Table.ColumnIndex(0)
                    ),
                    block = null,
                    tableId = tableId
                )
            ),
            rowsSize = 3
        )

        val actual3 = cellsSelectionState.current()

        //EXPECTED
        val expected3 = mapOf(
            1 to CellSelection(
                cellId = "$rowId1-$columnId0",
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(0),
                left = true,
                top = true,
                right = true,
                bottom = true
            ),
            7 to CellSelection(
                cellId = "$rowId1-$columnId2",
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(2),
                left = true,
                top = true,
                right = true,
                bottom = true
            )
        )

        //ASSERT
        assertEquals(2, actual3.size, message = "Size of selected cells expected 2")
        assertEquals(expected3, actual3)
    }
}