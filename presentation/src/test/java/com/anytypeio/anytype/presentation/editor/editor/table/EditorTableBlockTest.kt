package com.anytypeio.anytype.presentation.editor.editor.table

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubLayoutColumns
import com.anytypeio.anytype.core_models.StubLayoutRows
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.StubTable
import com.anytypeio.anytype.core_models.StubTableCells
import com.anytypeio.anytype.core_models.StubTableColumns
import com.anytypeio.anytype.core_models.StubTableRows
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.domain.table.FillTableRow
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.EditorPresentationTestSetup
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.verifyNoInteractions
import kotlin.test.assertEquals

class EditorTableBlockTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    private var tableId = MockDataFactory.randomUuid()

    @Test
    fun `should not amend second empty cell click`() {

        val columns = StubTableColumns(size = 3)
        val rows = StubTableRows(size = 2)
        val cells = StubTableCells(
            columns = listOf(columns[0], columns[1], columns[2]),
            rows = listOf(rows[0], rows[1])
        )
        val columnLayout = StubLayoutColumns(children = columns.map { it.id })
        val rowLayout = StubLayoutRows(children = rows.map { it.id })
        val table = StubTable(children = listOf(columnLayout.id, rowLayout.id))

        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(
            id = root,
            children = listOf(header.id, table.id)
        )

        val document =
            listOf(page, header, title, table, columnLayout, rowLayout) + columns + rows + cells

        stubInterceptEvents()
        stubOpenDocument(document)
        stubFillRow()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onClickListener(
                ListenerType.TableEmptyCell(
                    cell = BlockView.Table.Cell(
                        row = BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[0].id),
                            index = BlockView.Table.RowIndex(0),
                            isHeader = false
                        ),
                        column = BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[1].id),
                            index = BlockView.Table.ColumnIndex(1)
                        ),
                        block = null,
                        tableId = table.id
                    )
                )
            )
            onClickListener(
                ListenerType.TableEmptyCell(
                    cell = BlockView.Table.Cell(
                        row = BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[1].id),
                            index = BlockView.Table.RowIndex(1),
                            isHeader = false
                        ),
                        column = BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[0].id),
                            index = BlockView.Table.ColumnIndex(0)
                        ),
                        block = null,
                        tableId = table.id
                    )
                )
            )
        }

        coroutineTestRule.advanceTime(50L)

        runBlocking {
            val inOrder = inOrder(fillTableRow)
            inOrder.verify(fillTableRow).invoke(
                params = FillTableRow.Params(ctx = root, targetIds = listOf(rows[0].id))
            )
            inOrder.verify(fillTableRow).invoke(
                params = FillTableRow.Params(ctx = root, targetIds = listOf(rows[1].id))
            )
        }
    }

    @Test
    fun `should not amend second text cell click`() {

        val columns = StubTableColumns(size = 3)
        val rows = StubTableRows(size = 2)
        val cells = StubTableCells(columns = columns, rows = rows)
        val columnLayout = StubLayoutColumns(children = columns.map { it.id })
        val rowLayout = StubLayoutRows(children = rows.map { it.id })
        val table = StubTable(children = listOf(columnLayout.id, rowLayout.id))

        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(
            id = root,
            children = listOf(header.id, table.id)
        )

        val document =
            listOf(page, header, title, table, columnLayout, rowLayout) + columns + rows + cells

        stubInterceptEvents()
        stubOpenDocument(document)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onClickListener(
                ListenerType.TableTextCell(
                    cell = BlockView.Table.Cell(
                        row = BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[0].id),
                            index = BlockView.Table.RowIndex(0),
                            isHeader = false
                        ),
                        column = BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[1].id),
                            index = BlockView.Table.ColumnIndex(1)
                        ),
                        block = BlockView.Text.Paragraph(
                            id = cells[0].id,
                            text = cells[0].content.asText().text
                        ),
                        tableId = table.id
                    )
                )
            )
            onClickListener(
                ListenerType.TableTextCell(
                    cell = BlockView.Table.Cell(
                        row = BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[1].id),
                            index = BlockView.Table.RowIndex(1),
                            isHeader = false
                        ),
                        column = BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[0].id),
                            index = BlockView.Table.ColumnIndex(0)
                        ),
                        block = BlockView.Text.Paragraph(
                            id = cells[1].id,
                            text = cells[1].content.asText().text
                        ),
                        tableId = table.id
                    )
                )
            )
        }


        val selectedState = vm.currentSelection()
        runBlocking {
            assertEquals(0, selectedState.size)
            verifyNoInteractions(fillTableRow)
        }
    }

    @Test
    fun `when click on menu icon - should enter table mode with 1 selected cell`() {

        //SETUP
        val columns = StubTableColumns(size = 3)
        val rows = StubTableRows(size = 3)
        val cells = StubTableCells(columns = columns, rows = rows)
        val columnLayout = StubLayoutColumns(children = columns.map { it.id })
        val rowLayout = StubLayoutRows(children = rows.map { it.id })
        val table = StubTable(children = listOf(columnLayout.id, rowLayout.id))
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id, table.id))
        val document =
            listOf(page, header, title, table, columnLayout, rowLayout) + columns + rows + cells
        stubInterceptEvents()
        stubOpenDocument(document)

        //TESTING
        val vm = buildViewModel()
        vm.apply {
            onStart(root)

            onBlockFocusChanged(
                id = cells[0].id,
                hasFocus = true
            )

            onBlockToolbarBlockActionsClicked()

            onBlockFocusChanged(
                id = cells[0].id,
                hasFocus = false
            )
        }

        //EXPECTED
        val expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cells[0].id),
            targets = setOf(cells[0].id),
            tab = BlockView.Table.Tab.CELL
        )
        val expectedSelectedState = listOf(cells[0].id)


        //ASSERT
        assertEquals(
            expected = expectedSelectedState,
            actual = vm.currentSelection().toList()
        )
        assertEquals(
            expected = expectedMode,
            actual = vm.mode as Editor.Mode.Table
        )
    }

    @Test
    fun `when click on multiply cells in table - should be proper select state`() {

        //SETUP
        val columns = StubTableColumns(size = 3)
        val rows = StubTableRows(size = 3)
        val cells = StubTableCells(columns = columns, rows = rows)
        val columnLayout = StubLayoutColumns(children = columns.map { it.id })
        val rowLayout = StubLayoutRows(children = rows.map { it.id })
        val table = StubTable(children = listOf(columnLayout.id, rowLayout.id))
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id, table.id))
        val document =
            listOf(page, header, title, table, columnLayout, rowLayout) + columns + rows + cells
        stubInterceptEvents()
        stubOpenDocument(document)

        //TESTING
        val vm = buildViewModel()
        vm.apply {
            onStart(root)

            onBlockFocusChanged(
                id = cells[0].id,
                hasFocus = true
            )

            onBlockToolbarBlockActionsClicked()

            onBlockFocusChanged(
                id = cells[0].id,
                hasFocus = false
            )

            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = BlockView.Table.Cell(
                        row = BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[1].id),
                            index = BlockView.Table.RowIndex(1),
                            isHeader = false
                        ),
                        column = BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[1].id),
                            index = BlockView.Table.ColumnIndex(1)
                        ),
                        block = BlockView.Text.Paragraph(
                            id = cells[4].id,
                            text = cells[4].content.asText().text
                        ),
                        tableId = table.id
                    )
                )
            )

            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = BlockView.Table.Cell(
                        row = BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[2].id),
                            index = BlockView.Table.RowIndex(2),
                            isHeader = false
                        ),
                        column = BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[2].id),
                            index = BlockView.Table.ColumnIndex(2)
                        ),
                        block = BlockView.Text.Paragraph(
                            id = cells[8].id,
                            text = cells[8].content.asText().text
                        ),
                        tableId = table.id
                    )
                )
            )

            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = BlockView.Table.Cell(
                        row = BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[1].id),
                            index = BlockView.Table.RowIndex(1),
                            isHeader = false
                        ),
                        column = BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[1].id),
                            index = BlockView.Table.ColumnIndex(1)
                        ),
                        block = BlockView.Text.Paragraph(
                            id = cells[4].id,
                            text = cells[4].content.asText().text
                        ),
                        tableId = table.id
                    )
                )
            )
        }

        //EXPECTED
        val expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cells[0].id, cells[8].id),
            targets = setOf(cells[0].id, cells[8].id),
            tab = BlockView.Table.Tab.CELL
        )
        val expectedSelectedState = listOf(cells[0].id, cells[8].id)


        //ASSERT
        assertEquals(
            expected = expectedSelectedState,
            actual = vm.currentSelection().toList()
        )
        assertEquals(
            expected = expectedMode,
            actual = vm.mode as Editor.Mode.Table
        )
    }

    @Test
    fun `when enter table mode then change tab then click on cell - two columns should be select`() {

        //SETUP
        val columns = StubTableColumns(size = 3)
        val rows = StubTableRows(size = 3)
        val cells = StubTableCells(columns = columns, rows = rows)
        val columnLayout = StubLayoutColumns(children = columns.map { it.id })
        val rowLayout = StubLayoutRows(children = rows.map { it.id })
        val table = StubTable(children = listOf(columnLayout.id, rowLayout.id))
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id, table.id))
        val document =
            listOf(page, header, title, table, columnLayout, rowLayout) + columns + rows + cells
        stubInterceptEvents()
        stubOpenDocument(document)

        //TESTING
        val vm = buildViewModel()
        vm.apply {
            onStart(root)

            onBlockFocusChanged(
                id = cells[0].id,
                hasFocus = true
            )

            onBlockToolbarBlockActionsClicked()

            onBlockFocusChanged(
                id = cells[0].id,
                hasFocus = false
            )

            onSimpleTableWidgetItemClicked(
                item = SimpleTableWidgetItem.Tab.Column
            )

            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = BlockView.Table.Cell(
                        row = BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[2].id),
                            index = BlockView.Table.RowIndex(2),
                            isHeader = false
                        ),
                        column = BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[2].id),
                            index = BlockView.Table.ColumnIndex(2)
                        ),
                        block = BlockView.Text.Paragraph(
                            id = cells[8].id,
                            text = cells[8].content.asText().text
                        ),
                        tableId = table.id
                    )
                )
            )
        }

        //EXPECTED
        val expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cells[0].id),
            targets = setOf(
                cells[0].id,
                cells[3].id,
                cells[6].id,
                cells[2].id,
                cells[5].id,
                cells[8].id
            ),
            tab = BlockView.Table.Tab.COLUMN
        )
        val expectedSelectedState =
            listOf(cells[0].id, cells[3].id, cells[6].id, cells[2].id, cells[5].id, cells[8].id)

        //ASSERT
        assertEquals(
            expected = expectedSelectedState,
            actual = vm.currentSelection().toList()
        )
        assertEquals(
            expected = expectedMode,
            actual = vm.mode as Editor.Mode.Table
        )
    }

    @Test
    fun `when enter table mode then change tab then click on cell then unselect cell - one column should be select`() {

        //SETUP
        val columns = StubTableColumns(size = 3)
        val rows = StubTableRows(size = 3)
        val cells = StubTableCells(columns = columns, rows = rows)
        val columnLayout = StubLayoutColumns(children = columns.map { it.id })
        val rowLayout = StubLayoutRows(children = rows.map { it.id })
        val table = StubTable(children = listOf(columnLayout.id, rowLayout.id))
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id, table.id))
        val document =
            listOf(page, header, title, table, columnLayout, rowLayout) + columns + rows + cells
        stubInterceptEvents()
        stubOpenDocument(document)

        //TESTING
        val vm = buildViewModel()
        vm.apply {
            onStart(root)

            onBlockFocusChanged(
                id = cells[0].id,
                hasFocus = true
            )

            onBlockToolbarBlockActionsClicked()

            onBlockFocusChanged(
                id = cells[0].id,
                hasFocus = false
            )

            onSimpleTableWidgetItemClicked(
                item = SimpleTableWidgetItem.Tab.Column
            )

            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = BlockView.Table.Cell(
                        row = BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[2].id),
                            index = BlockView.Table.RowIndex(2),
                            isHeader = false
                        ),
                        column = BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[2].id),
                            index = BlockView.Table.ColumnIndex(2)
                        ),
                        block = BlockView.Text.Paragraph(
                            id = cells[8].id,
                            text = cells[8].content.asText().text
                        ),
                        tableId = table.id
                    )
                )
            )

            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = BlockView.Table.Cell(
                        row = BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[2].id),
                            index = BlockView.Table.RowIndex(2),
                            isHeader = false
                        ),
                        column = BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[2].id),
                            index = BlockView.Table.ColumnIndex(2)
                        ),
                        block = BlockView.Text.Paragraph(
                            id = cells[8].id,
                            text = cells[8].content.asText().text
                        ),
                        tableId = table.id
                    )
                )
            )
        }

        //EXPECTED
        val expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cells[0].id),
            targets = setOf(cells[0].id, cells[3].id, cells[6].id),
            tab = BlockView.Table.Tab.COLUMN
        )
        val expectedSelectedState = listOf(cells[0].id, cells[3].id, cells[6].id)

        //ASSERT
        assertEquals(
            expected = expectedSelectedState,
            actual = vm.currentSelection().toList()
        )
        assertEquals(
            expected = expectedMode,
            actual = vm.mode as Editor.Mode.Table
        )
    }

    @Test
    fun `when enter table mode then change tab then click on two cells then unselect cell - two rows should be select`() {

        //SETUP
        val columns = StubTableColumns(size = 4)
        val rows = StubTableRows(size = 4)
        val cells = StubTableCells(columns = columns, rows = rows)
        val columnLayout = StubLayoutColumns(children = columns.map { it.id })
        val rowLayout = StubLayoutRows(children = rows.map { it.id })
        val table = StubTable(id = tableId, children = listOf(columnLayout.id, rowLayout.id))
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id, table.id))
        val document =
            listOf(page, header, title, table, columnLayout, rowLayout) + columns + rows + cells
        stubInterceptEvents()
        stubOpenDocument(document)

        //TESTING Focus Cell[0] - Enter Table Mode - Click Cell[13] - Click Cell[11] - Click Tab COLUMN
        val vm = buildViewModel()
        vm.apply {
            onStart(root)

            onBlockFocusChanged(
                id = cells[0].id,
                hasFocus = true
            )

            onBlockToolbarBlockActionsClicked()

            onBlockFocusChanged(
                id = cells[0].id,
                hasFocus = false
            )

            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = mapToViewCell(
                        cell = cells[13],
                        row = rows[3],
                        rowIndex = 3,
                        column = columns[1],
                        columnIndex = 1
                    )
                )
            )

            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = mapToViewCell(
                        cell = cells[11],
                        row = rows[2],
                        rowIndex = 2,
                        column = columns[3],
                        columnIndex = 3
                    )
                )
            )

            onSimpleTableWidgetItemClicked(
                item = SimpleTableWidgetItem.Tab.Column
            )
        }

        //EXPECTED
        val expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cells[0].id, cells[13].id, cells[11].id),
            targets = setOf(
                cells[0].id,
                cells[4].id,
                cells[8].id,
                cells[12].id,
                cells[1].id,
                cells[5].id,
                cells[9].id,
                cells[13].id,
                cells[3].id,
                cells[7].id,
                cells[11].id,
                cells[15].id
            ),
            tab = BlockView.Table.Tab.COLUMN
        )
        val expectedSelectedState = listOf(
            cells[0].id,
            cells[13].id,
            cells[11].id,
            cells[4].id,
            cells[8].id,
            cells[12].id,
            cells[1].id,
            cells[5].id,
            cells[9].id,
            cells[3].id,
            cells[7].id,
            cells[15].id
        )

        var ids = listOf(
            BlockView.Table.ColumnId(columns[0].id),
            BlockView.Table.ColumnId(columns[1].id),
            BlockView.Table.ColumnId(columns[3].id)
        )
        val expectedSimpleTableWidget = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            columnItems = listOf(
                SimpleTableWidgetItem.Column.ClearContents(ids),
                SimpleTableWidgetItem.Column.Color(ids),
                SimpleTableWidgetItem.Column.Style(ids),
                SimpleTableWidgetItem.Column.ResetStyle(ids)
            ),
            selectedCount = 3,
            tab = BlockView.Table.Tab.COLUMN
        )

        //ASSERT
        assertEquals(
            expected = expectedSelectedState,
            actual = vm.currentSelection().toList()
        )
        assertEquals(
            expected = expectedMode,
            actual = vm.mode as Editor.Mode.Table
        )
        assertEquals(
            expected = expectedSimpleTableWidget,
            actual = vm.controlPanelViewState.value?.simpleTableWidget
        )

        //TESTING Click Cell[15] - Click Cell[0] - Click Cell[2]
        vm.apply {
            onStart(root)

            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = mapToViewCell(
                        cell = cells[15],
                        row = rows[3],
                        rowIndex = 3,
                        column = columns[3],
                        columnIndex = 3
                    )
                )
            )

            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = mapToViewCell(
                        cell = cells[0],
                        row = rows[0],
                        rowIndex = 0,
                        column = columns[0],
                        columnIndex = 0
                    )
                )
            )

            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = mapToViewCell(
                        cell = cells[2],
                        row = rows[0],
                        rowIndex = 0,
                        column = columns[2],
                        columnIndex = 2
                    )
                )
            )
        }

        //EXPECTED
        val expectedMode1 = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cells[0].id, cells[13].id, cells[11].id),
            targets = setOf(
                cells[1].id,
                cells[5].id,
                cells[9].id,
                cells[13].id,
                cells[2].id,
                cells[6].id,
                cells[10].id,
                cells[14].id
            ),
            tab = BlockView.Table.Tab.COLUMN
        )
        val expectedSelectedState1 = listOf(
            cells[13].id,
            cells[1].id,
            cells[5].id,
            cells[9].id,
            cells[2].id,
            cells[6].id,
            cells[10].id,
            cells[14].id
        )

        ids = listOf(
            BlockView.Table.ColumnId(columns[1].id),
            BlockView.Table.ColumnId(columns[2].id)
        )

        val expectedSimpleTableWidget1 = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            columnItems = listOf(
                SimpleTableWidgetItem.Column.ClearContents(ids),
                SimpleTableWidgetItem.Column.Color(ids),
                SimpleTableWidgetItem.Column.Style(ids),
                SimpleTableWidgetItem.Column.ResetStyle(ids)
            ),
            selectedCount = 2,
            tab = BlockView.Table.Tab.COLUMN
        )

        //ASSERT
        assertEquals(
            expected = expectedSelectedState1,
            actual = vm.currentSelection().toList()
        )
        assertEquals(
            expected = expectedMode1,
            actual = vm.mode as Editor.Mode.Table
        )
        assertEquals(
            expected = expectedSimpleTableWidget1,
            actual = vm.controlPanelViewState.value?.simpleTableWidget
        )

        //TESTING Click Tab ROW
        vm.apply {
            onSimpleTableWidgetItemClicked(
                item = SimpleTableWidgetItem.Tab.Row
            )
        }

        //EXPECTED
        val expectedMode2 = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cells[0].id, cells[13].id, cells[11].id),
            targets = setOf(
                cells[0].id,
                cells[13].id,
                cells[11].id,
                cells[1].id,
                cells[2].id,
                cells[3].id,
                cells[12].id,
                cells[14].id,
                cells[15].id,
                cells[8].id,
                cells[9].id,
                cells[10].id
            ),
            tab = BlockView.Table.Tab.ROW
        )
        val expectedSelectedState2 = listOf(
            cells[0].id,
            cells[13].id,
            cells[11].id,
            cells[1].id,
            cells[2].id,
            cells[3].id,
            cells[12].id,
            cells[14].id,
            cells[15].id,
            cells[8].id,
            cells[9].id,
            cells[10].id
        )

        var idsRow = listOf(
            BlockView.Table.RowId(rows[0].id),
            BlockView.Table.RowId(rows[3].id),
            BlockView.Table.RowId(rows[2].id)
        )

        val expectedSimpleTableWidget2 = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            rowItems = listOf(
                SimpleTableWidgetItem.Row.ClearContents(idsRow),
                SimpleTableWidgetItem.Row.Color(idsRow),
                SimpleTableWidgetItem.Row.Style(idsRow),
                SimpleTableWidgetItem.Row.ResetStyle(idsRow)
            ),
            selectedCount = 3,
            tab = BlockView.Table.Tab.ROW
        )

        //ASSERT
        assertEquals(
            expected = expectedSelectedState2,
            actual = vm.currentSelection().toList()
        )
        assertEquals(
            expected = expectedMode2,
            actual = vm.mode as Editor.Mode.Table
        )
        assertEquals(
            expected = expectedSimpleTableWidget2,
            actual = vm.controlPanelViewState.value?.simpleTableWidget
        )

        //TESTING Click Cell[12] - Click Cell[10]
        vm.apply {
            onStart(root)

            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = mapToViewCell(
                        cell = cells[12],
                        row = rows[3],
                        rowIndex = 3,
                        column = columns[0],
                        columnIndex = 0
                    )
                )
            )

            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = mapToViewCell(
                        cell = cells[10],
                        row = rows[2],
                        rowIndex = 2,
                        column = columns[2],
                        columnIndex = 2
                    )
                )
            )
        }

        //EXPECTED
        val expectedMode3 = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cells[0].id, cells[13].id, cells[11].id),
            targets = setOf(
                cells[0].id,
                cells[1].id,
                cells[2].id,
                cells[3].id
            ),
            tab = BlockView.Table.Tab.ROW
        )
        val expectedSelectedState3 = listOf(
            cells[0].id,
            cells[1].id,
            cells[2].id,
            cells[3].id
        )

        idsRow = listOf(BlockView.Table.RowId(rows[0].id))

        val expectedSimpleTableWidget3 = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            rowItems = listOf(
                SimpleTableWidgetItem.Row.InsertAbove(BlockView.Table.RowId(rows[0].id)),
                SimpleTableWidgetItem.Row.InsertBelow(BlockView.Table.RowId(rows[0].id)),
                SimpleTableWidgetItem.Row.MoveDown(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rows[0].id),
                        index = BlockView.Table.RowIndex(0),
                        isHeader = false
                    )
                ),
                SimpleTableWidgetItem.Row.Delete(BlockView.Table.RowId(rows[0].id)),
                SimpleTableWidgetItem.Row.Duplicate(BlockView.Table.RowId(rows[0].id)),
                SimpleTableWidgetItem.Row.ClearContents(idsRow),
                SimpleTableWidgetItem.Row.Color(idsRow),
                SimpleTableWidgetItem.Row.Style(idsRow),
                SimpleTableWidgetItem.Row.ResetStyle(idsRow)
            ),
            selectedCount = 1,
            tab = BlockView.Table.Tab.ROW
        )

        //ASSERT
        assertEquals(
            expected = expectedSelectedState3,
            actual = vm.currentSelection().toList()
        )
        assertEquals(
            expected = expectedMode3,
            actual = vm.mode as Editor.Mode.Table
        )
        assertEquals(
            expected = expectedSimpleTableWidget3,
            actual = vm.controlPanelViewState.value?.simpleTableWidget
        )

        //TESTING Click Tab CELL
        vm.apply {
            onSimpleTableWidgetItemClicked(
                item = SimpleTableWidgetItem.Tab.Cell
            )
        }

        //EXPECTED
        val expectedMode4 = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cells[0].id, cells[13].id, cells[11].id),
            targets = setOf(
                cells[0].id,
                cells[13].id,
                cells[11].id
            ),
            tab = BlockView.Table.Tab.CELL
        )
        val expectedSelectedState4 = listOf(
            cells[0].id,
            cells[13].id,
            cells[11].id
        )
        val expectedSimpleTableWidget4 = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            cellItems = listOf(
                SimpleTableWidgetItem.Cell.ClearContents,
                SimpleTableWidgetItem.Cell.Color,
                SimpleTableWidgetItem.Cell.Style,
                SimpleTableWidgetItem.Cell.ResetStyle
            ),
            selectedCount = 3,
            tab = BlockView.Table.Tab.CELL
        )

        //ASSERT
        assertEquals(
            expected = expectedSelectedState4,
            actual = vm.currentSelection().toList()
        )
        assertEquals(
            expected = expectedMode4,
            actual = vm.mode as Editor.Mode.Table
        )
        assertEquals(
            expected = expectedSimpleTableWidget4,
            actual = vm.controlPanelViewState.value?.simpleTableWidget
        )
    }

    /**
     * Выделена 1 колонка, стоит на позиции 0
     */
    @Test
    fun `when selected one column and column index is zero - should have proper menu items`() {
        //SETUP
        val columns = StubTableColumns(size = 3)
        val rows = StubTableRows(size = 2)
        val cells = StubTableCells(columns = columns, rows = rows)
        val columnLayout = StubLayoutColumns(children = columns.map { it.id })
        val rowLayout = StubLayoutRows(children = rows.map { it.id })
        val table = StubTable(id = tableId, children = listOf(columnLayout.id, rowLayout.id))
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id, table.id))
        val document =
            listOf(page, header, title, table, columnLayout, rowLayout) + columns + rows + cells
        stubInterceptEvents()
        stubOpenDocument(document)

        //TESTING Focus Cell[0] - Enter Table Mode - Click Tab COLUMN
        val vm = buildViewModel()
        vm.apply {
            onStart(root)

            onBlockFocusChanged(
                id = cells[0].id,
                hasFocus = true
            )

            onBlockToolbarBlockActionsClicked()

            onBlockFocusChanged(
                id = cells[0].id,
                hasFocus = false
            )

            onSimpleTableWidgetItemClicked(
                item = SimpleTableWidgetItem.Tab.Column
            )
        }

        //EXPECTED
        var expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cells[0].id),
            targets = setOf(
                cells[0].id,
                cells[3].id
            ),
            tab = BlockView.Table.Tab.COLUMN
        )
        var expectedSelectedState = listOf(
            cells[0].id,
            cells[3].id
        )

        var id = BlockView.Table.ColumnId(columns[0].id)
        var ids = listOf(id)
        var column = BlockView.Table.Column(
            id = id,
            index = BlockView.Table.ColumnIndex(0)
        )
        var expectedItems = listOf(
            SimpleTableWidgetItem.Column.InsertLeft(id),
            SimpleTableWidgetItem.Column.InsertRight(id),
            SimpleTableWidgetItem.Column.MoveRight(column),
            SimpleTableWidgetItem.Column.Duplicate(id),
            SimpleTableWidgetItem.Column.Delete(id),
            SimpleTableWidgetItem.Column.ClearContents(ids),
            SimpleTableWidgetItem.Column.Color(ids),
            SimpleTableWidgetItem.Column.Style(ids),
            SimpleTableWidgetItem.Column.ResetStyle(ids)
        )
        var expectedSimpleTableWidget = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            columnItems = expectedItems,
            selectedCount = 1,
            tab = BlockView.Table.Tab.COLUMN
        )

        //ASSERT
        assertEquals(
            expected = expectedSelectedState,
            actual = vm.currentSelection().toList()
        )
        assertEquals(
            expected = expectedMode,
            actual = vm.mode as Editor.Mode.Table
        )
        assertEquals(
            expected = expectedSimpleTableWidget,
            actual = vm.controlPanelViewState.value?.simpleTableWidget
        )

        //TESTING Click Cell[1](select Column 1) - Click Cell[0](unselect Column 0)
        vm.apply {
            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = mapToViewCell(
                        cell = cells[1],
                        row = rows[0],
                        rowIndex = 0,
                        column = columns[1],
                        columnIndex = 1
                    )
                )
            )

            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = mapToViewCell(
                        cell = cells[0],
                        row = rows[0],
                        rowIndex = 0,
                        column = columns[0],
                        columnIndex = 0
                    )
                )
            )
        }

        //EXPECTED
        expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cells[0].id),
            targets = setOf(
                cells[1].id,
                cells[4].id
            ),
            tab = BlockView.Table.Tab.COLUMN
        )
        expectedSelectedState = listOf(
            cells[1].id,
            cells[4].id
        )

        id = BlockView.Table.ColumnId(columns[1].id)
        ids = listOf(id)
        column = BlockView.Table.Column(
            id = id,
            index = BlockView.Table.ColumnIndex(1)
        )
        expectedItems = listOf(
            SimpleTableWidgetItem.Column.InsertLeft(id),
            SimpleTableWidgetItem.Column.InsertRight(id),
            SimpleTableWidgetItem.Column.MoveLeft(column),
            SimpleTableWidgetItem.Column.MoveRight(column),
            SimpleTableWidgetItem.Column.Duplicate(id),
            SimpleTableWidgetItem.Column.Delete(id),
            SimpleTableWidgetItem.Column.ClearContents(ids),
            SimpleTableWidgetItem.Column.Color(ids),
            SimpleTableWidgetItem.Column.Style(ids),
            SimpleTableWidgetItem.Column.ResetStyle(ids)
        )
        expectedSimpleTableWidget = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            columnItems = expectedItems,
            selectedCount = 1,
            tab = BlockView.Table.Tab.COLUMN
        )

        //ASSERT
        assertEquals(
            expected = expectedSelectedState,
            actual = vm.currentSelection().toList()
        )
        assertEquals(
            expected = expectedMode,
            actual = vm.mode as Editor.Mode.Table
        )
        assertEquals(
            expected = expectedSimpleTableWidget,
            actual = vm.controlPanelViewState.value?.simpleTableWidget
        )

        //TESTING Click Cell[2](select Column 2) - Click Cell[1](unselect Column 1)
        vm.apply {
            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = mapToViewCell(
                        cell = cells[2],
                        row = rows[0],
                        rowIndex = 0,
                        column = columns[2],
                        columnIndex = 2
                    )
                )
            )

            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = mapToViewCell(
                        cell = cells[1],
                        row = rows[0],
                        rowIndex = 0,
                        column = columns[1],
                        columnIndex = 1
                    )
                )
            )
        }

        //EXPECTED
        expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cells[0].id),
            targets = setOf(
                cells[2].id,
                cells[5].id
            ),
            tab = BlockView.Table.Tab.COLUMN
        )
        expectedSelectedState = listOf(
            cells[2].id,
            cells[5].id
        )

        id = BlockView.Table.ColumnId(columns[2].id)
        ids = listOf(id)
        column = BlockView.Table.Column(
            id = id,
            index = BlockView.Table.ColumnIndex(2)
        )
        expectedItems = listOf(
            SimpleTableWidgetItem.Column.InsertLeft(id),
            SimpleTableWidgetItem.Column.InsertRight(id),
            SimpleTableWidgetItem.Column.MoveLeft(column),
            SimpleTableWidgetItem.Column.Duplicate(id),
            SimpleTableWidgetItem.Column.Delete(id),
            SimpleTableWidgetItem.Column.ClearContents(ids),
            SimpleTableWidgetItem.Column.Color(ids),
            SimpleTableWidgetItem.Column.Style(ids),
            SimpleTableWidgetItem.Column.ResetStyle(ids)
        )
        expectedSimpleTableWidget = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            columnItems = expectedItems,
            selectedCount = 1,
            tab = BlockView.Table.Tab.COLUMN
        )

        //ASSERT
        assertEquals(
            expected = expectedSelectedState,
            actual = vm.currentSelection().toList()
        )
        assertEquals(
            expected = expectedMode,
            actual = vm.mode as Editor.Mode.Table
        )
        assertEquals(
            expected = expectedSimpleTableWidget,
            actual = vm.controlPanelViewState.value?.simpleTableWidget
        )

        //TESTING Click Cell[0](select Column 0)
        vm.apply {
            onClickListener(
                clicked = ListenerType.TableTextCell(
                    cell = mapToViewCell(
                        cell = cells[0],
                        row = rows[0],
                        rowIndex = 0,
                        column = columns[0],
                        columnIndex = 0
                    )
                )
            )
        }

        //EXPECTED
        expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cells[0].id),
            targets = setOf(
                cells[2].id,
                cells[5].id,
                cells[0].id,
                cells[3].id
            ),
            tab = BlockView.Table.Tab.COLUMN
        )
        expectedSelectedState = listOf(
            cells[0].id,
            cells[3].id,
            cells[2].id,
            cells[5].id
        )

        ids =
            listOf(BlockView.Table.ColumnId(columns[0].id), BlockView.Table.ColumnId(columns[2].id))
        expectedItems = listOf(
            SimpleTableWidgetItem.Column.ClearContents(ids),
            SimpleTableWidgetItem.Column.Color(ids),
            SimpleTableWidgetItem.Column.Style(ids),
            SimpleTableWidgetItem.Column.ResetStyle(ids)
        )
        expectedSimpleTableWidget = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            columnItems = expectedItems,
            selectedCount = 2,
            tab = BlockView.Table.Tab.COLUMN
        )

        //ASSERT
        assertEquals(
            expected = expectedSelectedState,
            actual = vm.currentSelection().toList()
        )
        assertEquals(
            expected = expectedMode,
            actual = vm.mode as Editor.Mode.Table
        )
        assertEquals(
            expected = expectedSimpleTableWidget,
            actual = vm.controlPanelViewState.value?.simpleTableWidget
        )
    }

    private fun mapToViewCell(
        cell: Block,
        row: Block,
        rowIndex: Int,
        column: Block,
        columnIndex: Int
    ): BlockView.Table.Cell {
        return BlockView.Table.Cell(
            row = BlockView.Table.Row(
                id = BlockView.Table.RowId(row.id),
                index = BlockView.Table.RowIndex(rowIndex),
                isHeader = false
            ),
            column = BlockView.Table.Column(
                id = BlockView.Table.ColumnId(column.id),
                index = BlockView.Table.ColumnIndex(columnIndex)
            ),
            block = BlockView.Text.Paragraph(
                id = cell.id,
                text = cell.content.asText().text
            ),
            tableId = tableId
        )
    }
}