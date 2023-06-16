package com.anytypeio.anytype.presentation.editor.editor.table

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubLayoutColumns
import com.anytypeio.anytype.core_models.StubLayoutRows
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.StubTable
import com.anytypeio.anytype.core_models.StubTableCells
import com.anytypeio.anytype.core_models.StubTableColumns
import com.anytypeio.anytype.core_models.StubTableRows
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.EditorPresentationTestSetup
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorTableRowsColumnsDelete : EditorPresentationTestSetup() {

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
    fun `when table contains only one row table widget shouldn't has delete button`() {
        //SETUP
        val columns = StubTableColumns(size = 4)
        val rows = StubTableRows(size = 1)
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

        //TESTING Focus Cell[0] - Enter Table Mode - Click Tab ROW
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
                item = SimpleTableWidgetItem.Tab.Row
            )
        }

        //EXPECTED
        val expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cells[0].id),
            targets = setOf(
                cells[0].id,
                cells[1].id,
                cells[2].id,
                cells[3].id
            ),
            tab = BlockView.Table.Tab.ROW
        )
        val expectedSelectedState = listOf(
            cells[0].id,
            cells[1].id,
            cells[2].id,
            cells[3].id
        )

        val row1 = BlockView.Table.RowId(rows[0].id)
        val ids = listOf(row1)

        val expectedSimpleTableWidget = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            items = listOf(
                SimpleTableWidgetItem.Row.InsertAbove(row1),
                SimpleTableWidgetItem.Row.InsertBelow(row1),
                SimpleTableWidgetItem.Row.Duplicate(row1),
                SimpleTableWidgetItem.Row.ClearContents(ids),
                SimpleTableWidgetItem.Row.Color(ids),
                SimpleTableWidgetItem.Row.Style(ids),
                SimpleTableWidgetItem.Row.ResetStyle(ids)
            ),
            selectedCount = 1,
            tab = BlockView.Table.Tab.ROW
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

    @Test
    fun `when table contains only one column table widget shouldn't has delete button`() {
        //SETUP
        val columns = StubTableColumns(size = 1)
        val rows = StubTableRows(size = 3)
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
        val expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cells[0].id),
            targets = setOf(
                cells[0].id,
                cells[1].id,
                cells[2].id
            ),
            tab = BlockView.Table.Tab.COLUMN
        )
        val expectedSelectedState = listOf(
            cells[0].id,
            cells[1].id,
            cells[2].id
        )

        val column1 = BlockView.Table.ColumnId(columns[0].id)
        val ids = listOf(column1)

        val expectedSimpleTableWidget = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            items = listOf(
                SimpleTableWidgetItem.Column.InsertLeft(column1),
                SimpleTableWidgetItem.Column.InsertRight(column1),
                SimpleTableWidgetItem.Column.Duplicate(column1),
                SimpleTableWidgetItem.Column.ClearContents(ids),
                SimpleTableWidgetItem.Column.Color(ids),
                SimpleTableWidgetItem.Column.Style(ids),
                SimpleTableWidgetItem.Column.ResetStyle(ids)
            ),
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
    }
}