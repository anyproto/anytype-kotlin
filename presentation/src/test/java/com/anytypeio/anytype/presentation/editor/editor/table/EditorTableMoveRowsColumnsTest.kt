package com.anytypeio.anytype.presentation.editor.editor.table

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubLayoutColumns
import com.anytypeio.anytype.core_models.StubLayoutRows
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.StubTable
import com.anytypeio.anytype.core_models.StubTableColumns
import com.anytypeio.anytype.core_models.StubTableRows
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.table.FillTableColumn
import com.anytypeio.anytype.domain.table.FillTableRow
import com.anytypeio.anytype.domain.table.MoveTableColumn
import com.anytypeio.anytype.domain.table.MoveTableRow
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.EditorPresentationTestSetup
import com.anytypeio.anytype.presentation.editor.editor.ViewState
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import kotlin.test.assertEquals

class EditorTableMoveRowsColumnsTest : EditorPresentationTestSetup() {

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
    fun `when clicking on moveUp and moveDown - table should proper render`() = runTest {
        //SETUP
        val columns = StubTableColumns(size = 4)
        val rows = StubTableRows(size = 4)

        val columnLayout = StubLayoutColumns(children = columns.map { it.id })
        val rowLayout = StubLayoutRows(children = rows.map { it.id })
        val table = StubTable(id = tableId, children = listOf(columnLayout.id, rowLayout.id))
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id, table.id))
        val document =
            listOf(page, header, title, table, columnLayout, rowLayout) + columns + rows
        stubInterceptEvents()
        stubOpenDocument(document)

        val rowToMoveId = rows[3].id

        //row to move
        val cellR3C0 = "${rows[3].id}-${columns[0].id}"
        val cellR3C1 = "${rows[3].id}-${columns[1].id}"
        val cellR3C2 = "${rows[3].id}-${columns[2].id}"
        val cellR3C3 = "${rows[3].id}-${columns[3].id}"

        //TESTING Focus Cell[r3_c2] - Enter Table Mode - Click Tab ROW - Click Move Up
        val vm = buildViewModel()

        vm.apply {
            onStart(root)

            fillTableRow.stub {
                onBlocking {
                    invoke(
                        params = FillTableRow.Params(
                            ctx = root,
                            targetIds = listOf(rowToMoveId)
                        )
                    )
                } doReturn Either.Right(
                    b = Payload(
                        context = context,
                        events = listOf(
                            Event.Command.UpdateStructure(
                                context = root,
                                id = rowToMoveId,
                                children = listOf(cellR3C0, cellR3C1, cellR3C2, cellR3C3)
                            ),
                            Event.Command.AddBlock(
                                context = root,
                                blocks = listOf(
                                    StubParagraph(id = cellR3C0, text = ""),
                                    StubParagraph(id = cellR3C1, text = ""),
                                    StubParagraph(id = cellR3C2, text = ""),
                                    StubParagraph(id = cellR3C3, text = "")
                                )
                            )
                        )
                    )
                )
            }

            onClickListener(
                clicked = ListenerType.TableEmptyCell(
                    cell = BlockView.Table.Cell(
                        tableId = tableId,
                        row = BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[3].id),
                            index = BlockView.Table.RowIndex(3),
                            isHeader = false
                        ),
                        block = null,
                        column = BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[2].id),
                            index = BlockView.Table.ColumnIndex(2)
                        )
                    )
                )
            )

            verify(fillTableRow, times(1)).invoke(
                params = FillTableRow.Params(
                    ctx = root,
                    targetIds = listOf(rowToMoveId)
                )
            )

            onBlockFocusChanged(
                id = cellR3C2,
                hasFocus = true
            )

            onBlockToolbarBlockActionsClicked()

            onBlockFocusChanged(
                id = cellR3C2,
                hasFocus = false
            )

            onSimpleTableWidgetItemClicked(
                item = SimpleTableWidgetItem.Tab.Row
            )

            fillTableRow.stub {
                onBlocking {
                    invoke(
                        params = FillTableRow.Params(
                            ctx = root,
                            targetIds = listOf(rowToMoveId)
                        )
                    )
                } doReturn Either.Right(
                    b = Payload(
                        context = context,
                        events = listOf()
                    )
                )
            }

            moveTableRow.stub {
                onBlocking {
                    run(
                        params = MoveTableRow.Params(
                            context = root,
                            rowContext = root,
                            row = rowToMoveId,
                            targetDrop = rows[2].id,
                            position = Position.TOP
                        )
                    )
                } doReturn Either.Right(
                    Payload(
                        context = root, events = listOf(
                            Event.Command.UpdateStructure(
                                context = root,
                                id = rowLayout.id,
                                children = listOf(rows[0].id, rows[1].id, rows[3].id, rows[2].id)
                            )
                        )
                    )
                )
            }

            onSimpleTableWidgetItemClicked(
                item = SimpleTableWidgetItem.Row.MoveUp(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(value = rowToMoveId),
                        index = BlockView.Table.RowIndex(3),
                        isHeader = false
                    )
                )
            )

            verify(fillTableRow, times(2)).invoke(
                params = FillTableRow.Params(
                    ctx = root,
                    targetIds = listOf(rowToMoveId)
                )
            )

            verify(moveTableRow, times(1)).run(
                params = MoveTableRow.Params(
                    context = root,
                    rowContext = root,
                    row = rowToMoveId,
                    targetDrop = rows[2].id,
                    position = Position.TOP
                )
            )
        }

        //EXPECTED

        var expectedCells = listOf(
            mapToViewCell(
                column = columns[0],
                row = rows[0],
                columnIndex = 0,
                rowIndex = 0
            ),
            mapToViewCell(
                column = columns[0],
                row = rows[1],
                columnIndex = 0,
                rowIndex = 1
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C0, text = ""),
                column = columns[0],
                row = rows[3],
                columnIndex = 0,
                rowIndex = 2,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[0],
                row = rows[2],
                columnIndex = 0,
                rowIndex = 3
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[0],
                columnIndex = 1,
                rowIndex = 0
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[1],
                columnIndex = 1,
                rowIndex = 1
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C1, text = ""),
                column = columns[1],
                row = rows[3],
                columnIndex = 1,
                rowIndex = 2,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[2],
                columnIndex = 1,
                rowIndex = 3
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[0],
                columnIndex = 2,
                rowIndex = 0
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[1],
                columnIndex = 2,
                rowIndex = 1
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C2, text = ""),
                column = columns[2],
                row = rows[3],
                columnIndex = 2,
                rowIndex = 2,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[2],
                columnIndex = 2,
                rowIndex = 3
            ),
            mapToViewCell(
                column = columns[3],
                row = rows[0],
                columnIndex = 3,
                rowIndex = 0
            ),
            mapToViewCell(
                column = columns[3],
                row = rows[1],
                columnIndex = 3,
                rowIndex = 1
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C3, text = ""),
                column = columns[3],
                row = rows[3],
                columnIndex = 3,
                rowIndex = 2,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[3],
                row = rows[2],
                columnIndex = 3,
                rowIndex = 3
            )
        )

        var expectedViewState = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.READ,
                ),
                BlockView.Table(
                    id = tableId,
                    columns = listOf(
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[0].id),
                            index = BlockView.Table.ColumnIndex(0)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[1].id),
                            index = BlockView.Table.ColumnIndex(1)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[2].id),
                            index = BlockView.Table.ColumnIndex(2)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[3].id),
                            index = BlockView.Table.ColumnIndex(3)
                        )
                    ),
                    rows = listOf(
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[0].id),
                            index = BlockView.Table.RowIndex(0),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[1].id),
                            index = BlockView.Table.RowIndex(1),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rowToMoveId),
                            index = BlockView.Table.RowIndex(2),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[2].id),
                            index = BlockView.Table.RowIndex(3),
                            isHeader = false
                        )
                    ),
                    cells = expectedCells,
                    isSelected = false,
                    selectedCellsIds = listOf(cellR3C2, cellR3C0, cellR3C1, cellR3C3),
                    tab = BlockView.Table.Tab.ROW
                )
            )
        )

        //ASSERT VIEW STATE
        val actualViewState = vm.state.test().value()
        assertEquals(
            expected = expectedViewState,
            actual = actualViewState
        )

        var expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cellR3C2),
            targets = setOf(cellR3C2, cellR3C0, cellR3C1, cellR3C3),
            tab = BlockView.Table.Tab.ROW
        )
        var expectedSelectedState = listOf(cellR3C2, cellR3C0, cellR3C1, cellR3C3)

        var selectedRow = BlockView.Table.RowId(rowToMoveId)
        var ids = listOf(selectedRow)
        var expectedSimpleTableWidget = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            items = listOf(
                SimpleTableWidgetItem.Row.InsertAbove(selectedRow),
                SimpleTableWidgetItem.Row.InsertBelow(selectedRow),
                SimpleTableWidgetItem.Row.MoveUp(
                    row = BlockView.Table.Row(
                        id = selectedRow,
                        index = BlockView.Table.RowIndex(2),
                        isHeader = false
                    )
                ),
                SimpleTableWidgetItem.Row.MoveDown(
                    row = BlockView.Table.Row(
                        id = selectedRow,
                        index = BlockView.Table.RowIndex(2),
                        isHeader = false
                    )
                ),
                SimpleTableWidgetItem.Row.Delete(selectedRow),
                SimpleTableWidgetItem.Row.Duplicate(selectedRow),
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

        //TESTING Click MoveUp
        vm.apply {

            fillTableRow.stub {
                onBlocking {
                    invoke(
                        params = FillTableRow.Params(
                            ctx = root,
                            targetIds = listOf(rowToMoveId)
                        )
                    )
                } doReturn Either.Right(
                    b = Payload(
                        context = context,
                        events = listOf()
                    )
                )
            }

            moveTableRow.stub {
                onBlocking {
                    run(
                        params = MoveTableRow.Params(
                            context = root,
                            rowContext = root,
                            row = rowToMoveId,
                            targetDrop = rows[1].id,
                            position = Position.TOP
                        )
                    )
                } doReturn Either.Right(
                    Payload(
                        context = root, events = listOf(
                            Event.Command.UpdateStructure(
                                context = root,
                                id = rowLayout.id,
                                children = listOf(rows[0].id, rows[3].id, rows[1].id, rows[2].id)
                            )
                        )
                    )
                )
            }

            onSimpleTableWidgetItemClicked(
                item = SimpleTableWidgetItem.Row.MoveUp(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(value = rowToMoveId),
                        index = BlockView.Table.RowIndex(2),
                        isHeader = false
                    )
                )
            )

            verify(fillTableRow, times(3)).invoke(
                params = FillTableRow.Params(
                    ctx = root,
                    targetIds = listOf(rowToMoveId)
                )
            )

            verify(moveTableRow, times(1)).run(
                params = MoveTableRow.Params(
                    context = root,
                    rowContext = root,
                    row = rowToMoveId,
                    targetDrop = rows[1].id,
                    position = Position.TOP
                )
            )
        }


        //EXPECTED

        expectedCells = listOf(
            mapToViewCell(
                column = columns[0],
                row = rows[0],
                columnIndex = 0,
                rowIndex = 0
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C0, text = ""),
                column = columns[0],
                row = rows[3],
                columnIndex = 0,
                rowIndex = 1,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[0],
                row = rows[1],
                columnIndex = 0,
                rowIndex = 2
            ),
            mapToViewCell(
                column = columns[0],
                row = rows[2],
                columnIndex = 0,
                rowIndex = 3
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[0],
                columnIndex = 1,
                rowIndex = 0
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C1, text = ""),
                column = columns[1],
                row = rows[3],
                columnIndex = 1,
                rowIndex = 1,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[1],
                columnIndex = 1,
                rowIndex = 2
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[2],
                columnIndex = 1,
                rowIndex = 3
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[0],
                columnIndex = 2,
                rowIndex = 0
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C2, text = ""),
                column = columns[2],
                row = rows[3],
                columnIndex = 2,
                rowIndex = 1,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[1],
                columnIndex = 2,
                rowIndex = 2
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[2],
                columnIndex = 2,
                rowIndex = 3
            ),
            mapToViewCell(
                column = columns[3],
                row = rows[0],
                columnIndex = 3,
                rowIndex = 0
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C3, text = ""),
                column = columns[3],
                row = rows[3],
                columnIndex = 3,
                rowIndex = 1,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[3],
                row = rows[1],
                columnIndex = 3,
                rowIndex = 2
            ),
            mapToViewCell(
                column = columns[3],
                row = rows[2],
                columnIndex = 3,
                rowIndex = 3
            )
        )

        expectedViewState = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.READ,
                ),
                BlockView.Table(
                    id = tableId,
                    columns = listOf(
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[0].id),
                            index = BlockView.Table.ColumnIndex(0)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[1].id),
                            index = BlockView.Table.ColumnIndex(1)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[2].id),
                            index = BlockView.Table.ColumnIndex(2)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[3].id),
                            index = BlockView.Table.ColumnIndex(3)
                        )
                    ),
                    rows = listOf(
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[0].id),
                            index = BlockView.Table.RowIndex(0),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rowToMoveId),
                            index = BlockView.Table.RowIndex(1),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[1].id),
                            index = BlockView.Table.RowIndex(2),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[2].id),
                            index = BlockView.Table.RowIndex(3),
                            isHeader = false
                        )
                    ),
                    cells = expectedCells,
                    isSelected = false,
                    selectedCellsIds = listOf(cellR3C2, cellR3C0, cellR3C1, cellR3C3),
                    tab = BlockView.Table.Tab.ROW
                )
            )
        )

        //ASSERT VIEW STATE
        assertEquals(
            expected = expectedViewState,
            actual = vm.state.test().value()
        )

        expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cellR3C2),
            targets = setOf(cellR3C2, cellR3C0, cellR3C1, cellR3C3),
            tab = BlockView.Table.Tab.ROW
        )
        expectedSelectedState = listOf(cellR3C2, cellR3C0, cellR3C1, cellR3C3)

        selectedRow = BlockView.Table.RowId(rowToMoveId)
        ids = listOf(selectedRow)
        expectedSimpleTableWidget = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            items = listOf(
                SimpleTableWidgetItem.Row.InsertAbove(selectedRow),
                SimpleTableWidgetItem.Row.InsertBelow(selectedRow),
                SimpleTableWidgetItem.Row.MoveUp(
                    row = BlockView.Table.Row(
                        id = selectedRow,
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    )
                ),
                SimpleTableWidgetItem.Row.MoveDown(
                    row = BlockView.Table.Row(
                        id = selectedRow,
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    )
                ),
                SimpleTableWidgetItem.Row.Delete(selectedRow),
                SimpleTableWidgetItem.Row.Duplicate(selectedRow),
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

        //TESTING Click MoveUp
        vm.apply {

            fillTableRow.stub {
                onBlocking {
                    invoke(
                        params = FillTableRow.Params(
                            ctx = root,
                            targetIds = listOf(rowToMoveId)
                        )
                    )
                } doReturn Either.Right(
                    b = Payload(
                        context = context,
                        events = listOf()
                    )
                )
            }

            moveTableRow.stub {
                onBlocking {
                    run(
                        params = MoveTableRow.Params(
                            context = root,
                            rowContext = root,
                            row = rowToMoveId,
                            targetDrop = rows[0].id,
                            position = Position.TOP
                        )
                    )
                } doReturn Either.Right(
                    Payload(
                        context = root, events = listOf(
                            Event.Command.UpdateStructure(
                                context = root,
                                id = rowLayout.id,
                                children = listOf(rows[3].id, rows[0].id, rows[1].id, rows[2].id)
                            )
                        )
                    )
                )
            }

            onSimpleTableWidgetItemClicked(
                item = SimpleTableWidgetItem.Row.MoveUp(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(value = rowToMoveId),
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    )
                )
            )

            verify(fillTableRow, times(4)).invoke(
                params = FillTableRow.Params(
                    ctx = root,
                    targetIds = listOf(rowToMoveId)
                )
            )

            verify(moveTableRow, times(1)).run(
                params = MoveTableRow.Params(
                    context = root,
                    rowContext = root,
                    row = rowToMoveId,
                    targetDrop = rows[0].id,
                    position = Position.TOP
                )
            )
        }


        //EXPECTED

        expectedCells = listOf(
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C0, text = ""),
                column = columns[0],
                row = rows[3],
                columnIndex = 0,
                rowIndex = 0,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[0],
                row = rows[0],
                columnIndex = 0,
                rowIndex = 1
            ),
            mapToViewCell(
                column = columns[0],
                row = rows[1],
                columnIndex = 0,
                rowIndex = 2
            ),
            mapToViewCell(
                column = columns[0],
                row = rows[2],
                columnIndex = 0,
                rowIndex = 3
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C1, text = ""),
                column = columns[1],
                row = rows[3],
                columnIndex = 1,
                rowIndex = 0,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[0],
                columnIndex = 1,
                rowIndex = 1
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[1],
                columnIndex = 1,
                rowIndex = 2
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[2],
                columnIndex = 1,
                rowIndex = 3
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C2, text = ""),
                column = columns[2],
                row = rows[3],
                columnIndex = 2,
                rowIndex = 0,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[0],
                columnIndex = 2,
                rowIndex = 1
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[1],
                columnIndex = 2,
                rowIndex = 2
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[2],
                columnIndex = 2,
                rowIndex = 3
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C3, text = ""),
                column = columns[3],
                row = rows[3],
                columnIndex = 3,
                rowIndex = 0,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[3],
                row = rows[0],
                columnIndex = 3,
                rowIndex = 1
            ),
            mapToViewCell(
                column = columns[3],
                row = rows[1],
                columnIndex = 3,
                rowIndex = 2
            ),
            mapToViewCell(
                column = columns[3],
                row = rows[2],
                columnIndex = 3,
                rowIndex = 3
            )
        )

        expectedViewState = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.READ,
                ),
                BlockView.Table(
                    id = tableId,
                    columns = listOf(
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[0].id),
                            index = BlockView.Table.ColumnIndex(0)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[1].id),
                            index = BlockView.Table.ColumnIndex(1)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[2].id),
                            index = BlockView.Table.ColumnIndex(2)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[3].id),
                            index = BlockView.Table.ColumnIndex(3)
                        )
                    ),
                    rows = listOf(
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rowToMoveId),
                            index = BlockView.Table.RowIndex(0),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[0].id),
                            index = BlockView.Table.RowIndex(1),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[1].id),
                            index = BlockView.Table.RowIndex(2),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[2].id),
                            index = BlockView.Table.RowIndex(3),
                            isHeader = false
                        )
                    ),
                    cells = expectedCells,
                    isSelected = false,
                    selectedCellsIds = listOf(cellR3C2, cellR3C0, cellR3C1, cellR3C3),
                    tab = BlockView.Table.Tab.ROW
                )
            )
        )

        //ASSERT VIEW STATE
        assertEquals(
            expected = expectedViewState,
            actual = vm.state.test().value()
        )

        expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cellR3C2),
            targets = setOf(cellR3C2, cellR3C0, cellR3C1, cellR3C3),
            tab = BlockView.Table.Tab.ROW
        )
        expectedSelectedState = listOf(cellR3C2, cellR3C0, cellR3C1, cellR3C3)

        selectedRow = BlockView.Table.RowId(rowToMoveId)
        ids = listOf(selectedRow)
        expectedSimpleTableWidget = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            items = listOf(
                SimpleTableWidgetItem.Row.InsertAbove(selectedRow),
                SimpleTableWidgetItem.Row.InsertBelow(selectedRow),
                SimpleTableWidgetItem.Row.MoveDown(
                    row = BlockView.Table.Row(
                        id = selectedRow,
                        index = BlockView.Table.RowIndex(0),
                        isHeader = false
                    )
                ),
                SimpleTableWidgetItem.Row.Delete(selectedRow),
                SimpleTableWidgetItem.Row.Duplicate(selectedRow),
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

        //TESTING Click MoveDown
        vm.apply {

            fillTableRow.stub {
                onBlocking {
                    invoke(
                        params = FillTableRow.Params(
                            ctx = root,
                            targetIds = listOf(rowToMoveId)
                        )
                    )
                } doReturn Either.Right(
                    b = Payload(
                        context = context,
                        events = listOf()
                    )
                )
            }

            moveTableRow.stub {
                onBlocking {
                    run(
                        params = MoveTableRow.Params(
                            context = root,
                            rowContext = root,
                            row = rowToMoveId,
                            targetDrop = rows[0].id,
                            position = Position.BOTTOM
                        )
                    )
                } doReturn Either.Right(
                    Payload(
                        context = root, events = listOf(
                            Event.Command.UpdateStructure(
                                context = root,
                                id = rowLayout.id,
                                children = listOf(rows[0].id, rows[3].id, rows[1].id, rows[2].id)
                            )
                        )
                    )
                )
            }

            onSimpleTableWidgetItemClicked(
                item = SimpleTableWidgetItem.Row.MoveDown(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(value = rowToMoveId),
                        index = BlockView.Table.RowIndex(0),
                        isHeader = false
                    )
                )
            )

            verify(fillTableRow, times(5)).invoke(
                params = FillTableRow.Params(
                    ctx = root,
                    targetIds = listOf(rowToMoveId)
                )
            )

            verify(moveTableRow, times(1)).run(
                params = MoveTableRow.Params(
                    context = root,
                    rowContext = root,
                    row = rowToMoveId,
                    targetDrop = rows[0].id,
                    position = Position.BOTTOM
                )
            )
        }


        //EXPECTED

        expectedCells = listOf(
            mapToViewCell(
                column = columns[0],
                row = rows[0],
                columnIndex = 0,
                rowIndex = 0
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C0, text = ""),
                column = columns[0],
                row = rows[3],
                columnIndex = 0,
                rowIndex = 1,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[0],
                row = rows[1],
                columnIndex = 0,
                rowIndex = 2
            ),
            mapToViewCell(
                column = columns[0],
                row = rows[2],
                columnIndex = 0,
                rowIndex = 3
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[0],
                columnIndex = 1,
                rowIndex = 0
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C1, text = ""),
                column = columns[1],
                row = rows[3],
                columnIndex = 1,
                rowIndex = 1,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[1],
                columnIndex = 1,
                rowIndex = 2
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[2],
                columnIndex = 1,
                rowIndex = 3
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[0],
                columnIndex = 2,
                rowIndex = 0
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C2, text = ""),
                column = columns[2],
                row = rows[3],
                columnIndex = 2,
                rowIndex = 1,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[1],
                columnIndex = 2,
                rowIndex = 2
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[2],
                columnIndex = 2,
                rowIndex = 3
            ),
            mapToViewCell(
                column = columns[3],
                row = rows[0],
                columnIndex = 3,
                rowIndex = 0
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR3C3, text = ""),
                column = columns[3],
                row = rows[3],
                columnIndex = 3,
                rowIndex = 1,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[3],
                row = rows[1],
                columnIndex = 3,
                rowIndex = 2
            ),
            mapToViewCell(
                column = columns[3],
                row = rows[2],
                columnIndex = 3,
                rowIndex = 3
            )
        )

        expectedViewState = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.READ,
                ),
                BlockView.Table(
                    id = tableId,
                    columns = listOf(
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[0].id),
                            index = BlockView.Table.ColumnIndex(0)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[1].id),
                            index = BlockView.Table.ColumnIndex(1)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[2].id),
                            index = BlockView.Table.ColumnIndex(2)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[3].id),
                            index = BlockView.Table.ColumnIndex(3)
                        )
                    ),
                    rows = listOf(
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[0].id),
                            index = BlockView.Table.RowIndex(0),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rowToMoveId),
                            index = BlockView.Table.RowIndex(1),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[1].id),
                            index = BlockView.Table.RowIndex(2),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[2].id),
                            index = BlockView.Table.RowIndex(3),
                            isHeader = false
                        )
                    ),
                    cells = expectedCells,
                    isSelected = false,
                    selectedCellsIds = listOf(cellR3C2, cellR3C0, cellR3C1, cellR3C3),
                    tab = BlockView.Table.Tab.ROW
                )
            )
        )

        //ASSERT VIEW STATE
        assertEquals(
            expected = expectedViewState,
            actual = vm.state.test().value()
        )

        expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cellR3C2),
            targets = setOf(cellR3C2, cellR3C0, cellR3C1, cellR3C3),
            tab = BlockView.Table.Tab.ROW
        )
        expectedSelectedState = listOf(cellR3C2, cellR3C0, cellR3C1, cellR3C3)

        selectedRow = BlockView.Table.RowId(rowToMoveId)
        ids = listOf(selectedRow)
        expectedSimpleTableWidget = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            items = listOf(
                SimpleTableWidgetItem.Row.InsertAbove(selectedRow),
                SimpleTableWidgetItem.Row.InsertBelow(selectedRow),
                SimpleTableWidgetItem.Row.MoveUp(
                    row = BlockView.Table.Row(
                        id = selectedRow,
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    )
                ),
                SimpleTableWidgetItem.Row.MoveDown(
                    row = BlockView.Table.Row(
                        id = selectedRow,
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    )
                ),
                SimpleTableWidgetItem.Row.Delete(selectedRow),
                SimpleTableWidgetItem.Row.Duplicate(selectedRow),
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


        coroutineTestRule.advanceTime(300L)
    }

    @Test
    fun `when clicking on moveRight and moveLeft - table should proper render`() = runTest {
        //SETUP
        val columns = StubTableColumns(size = 3)
        val rows = StubTableRows(size = 3)

        val columnLayout = StubLayoutColumns(children = columns.map { it.id })
        val rowLayout = StubLayoutRows(children = rows.map { it.id })
        val table = StubTable(id = tableId, children = listOf(columnLayout.id, rowLayout.id))
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id, table.id))
        val document =
            listOf(page, header, title, table, columnLayout, rowLayout) + columns + rows
        stubInterceptEvents()
        stubOpenDocument(document)

        val row0 = rows[0].id
        val columnToMoveId = columns[0].id

        //row to move
        val cellR0C0 = "${rows[0].id}-${columns[0].id}"
        val cellR1C0 = "${rows[1].id}-${columns[0].id}"
        val cellR2C0 = "${rows[2].id}-${columns[0].id}"

        val cellR0C1 = "${rows[0].id}-${columns[1].id}"
        val cellR0C2 = "${rows[0].id}-${columns[2].id}"

        //TESTING Focus Cell[r0_c0] - Enter Table Mode - Click Tab COLUMN - Click Move Right
        val vm = buildViewModel()

        vm.apply {
            onStart(root)

            fillTableRow.stub {
                onBlocking {
                    invoke(
                        params = FillTableRow.Params(
                            ctx = root,
                            targetIds = listOf(rows[0].id)
                        )
                    )
                } doReturn Either.Right(
                    b = Payload(
                        context = context,
                        events = listOf(
                            Event.Command.UpdateStructure(
                                context = root,
                                id = row0,
                                children = listOf(cellR0C0, cellR0C1, cellR0C2)
                            ),
                            Event.Command.AddBlock(
                                context = root,
                                blocks = listOf(
                                    StubParagraph(id = cellR0C0, text = ""),
                                    StubParagraph(id = cellR0C1, text = ""),
                                    StubParagraph(id = cellR0C2, text = "")
                                )
                            )
                        )
                    )
                )
            }

            onClickListener(
                clicked = ListenerType.TableEmptyCell(
                    cell = BlockView.Table.Cell(
                        tableId = tableId,
                        row = BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[0].id),
                            index = BlockView.Table.RowIndex(0),
                            isHeader = false
                        ),
                        block = null,
                        column = BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[0].id),
                            index = BlockView.Table.ColumnIndex(0)
                        )
                    )
                )
            )

            verify(fillTableRow, times(1)).invoke(
                params = FillTableRow.Params(
                    ctx = root,
                    targetIds = listOf(row0)
                )
            )

            onBlockFocusChanged(
                id = cellR0C0,
                hasFocus = true
            )

            onBlockToolbarBlockActionsClicked()

            onBlockFocusChanged(
                id = cellR0C0,
                hasFocus = false
            )

            onSimpleTableWidgetItemClicked(
                item = SimpleTableWidgetItem.Tab.Column
            )

            fillTableColumn.stub {
                onBlocking {
                    invoke(
                        params = FillTableColumn.Params(
                            ctx = root,
                            targetIds = listOf(columnToMoveId)
                        )
                    )
                } doReturn Either.Right(
                    b = Payload(
                        context = context,
                        events = listOf(
                            Event.Command.UpdateStructure(
                                context = root,
                                id = rows[1].id,
                                children = listOf(cellR1C0)
                            ),
                            Event.Command.AddBlock(
                                context = root,
                                blocks = listOf(StubParagraph(id = cellR1C0, text = ""))
                            ),
                            Event.Command.UpdateStructure(
                                context = root,
                                id = rows[2].id,
                                children = listOf(cellR2C0)
                            ),
                            Event.Command.AddBlock(
                                context = root,
                                blocks = listOf(StubParagraph(id = cellR2C0, text = ""))
                            )
                        )
                    )
                )
            }

            moveTableColumn.stub {
                onBlocking {
                    run(
                        params = MoveTableColumn.Params(
                            ctx = root,
                            column = columnToMoveId,
                            targetDrop = columns[1].id,
                            position = Position.RIGHT
                        )
                    )
                } doReturn Either.Right(
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.UpdateStructure(
                                context = root,
                                id = columnLayout.id,
                                children = listOf(columns[1].id, columns[0].id, columns[2].id)
                            ),
                            Event.Command.UpdateStructure(
                                context = root,
                                id = rows[0].id,
                                children = listOf(cellR0C1, cellR0C0, cellR0C2)
                            )
                        )
                    )
                )
            }

            onSimpleTableWidgetItemClicked(
                item = SimpleTableWidgetItem.Column.MoveRight(
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(value = columnToMoveId),
                        index = BlockView.Table.ColumnIndex(0)
                    )
                )
            )

            verifyNoMoreInteractions(fillTableRow)

            verify(fillTableColumn, times(1)).invoke(
                params = FillTableColumn.Params(
                    ctx = root,
                    targetIds = listOf(columnToMoveId)
                )
            )

            verify(moveTableColumn, times(1)).run(
                params = MoveTableColumn.Params(
                    ctx = root,
                    column = columnToMoveId,
                    targetDrop = columns[1].id,
                    position = Position.RIGHT
                )
            )
        }

        //EXPECTED

        var expectedCells = listOf(
            mapToViewCell(
                textBlock = StubParagraph(id = cellR0C1, text = ""),
                column = columns[1],
                row = rows[0],
                columnIndex = 0,
                rowIndex = 0,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[1],
                columnIndex = 0,
                rowIndex = 1
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[2],
                columnIndex = 0,
                rowIndex = 2
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR0C0, text = ""),
                column = columns[0],
                row = rows[0],
                columnIndex = 1,
                rowIndex = 0,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR1C0, text = ""),
                column = columns[0],
                row = rows[1],
                columnIndex = 1,
                rowIndex = 1,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR2C0, text = ""),
                column = columns[0],
                row = rows[2],
                columnIndex = 1,
                rowIndex = 2,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR0C2, text = ""),
                column = columns[2],
                row = rows[0],
                columnIndex = 2,
                rowIndex = 0,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[1],
                columnIndex = 2,
                rowIndex = 1
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[2],
                columnIndex = 2,
                rowIndex = 2
            )
        )

        var expectedViewState = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.READ,
                ),
                BlockView.Table(
                    id = tableId,
                    columns = listOf(
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[1].id),
                            index = BlockView.Table.ColumnIndex(0)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[0].id),
                            index = BlockView.Table.ColumnIndex(1)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[2].id),
                            index = BlockView.Table.ColumnIndex(2)
                        )
                    ),
                    rows = listOf(
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[0].id),
                            index = BlockView.Table.RowIndex(0),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[1].id),
                            index = BlockView.Table.RowIndex(1),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[2].id),
                            index = BlockView.Table.RowIndex(2),
                            isHeader = false
                        )
                    ),
                    cells = expectedCells,
                    isSelected = false,
                    selectedCellsIds = listOf(cellR0C0, cellR1C0, cellR2C0),
                    tab = BlockView.Table.Tab.COLUMN
                )
            )
        )

        var expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cellR0C0),
            targets = setOf(cellR0C0, cellR1C0, cellR2C0),
            tab = BlockView.Table.Tab.COLUMN
        )
        var expectedSelectedState = listOf(cellR0C0, cellR1C0, cellR2C0)

        var selectedColumn = BlockView.Table.ColumnId(columnToMoveId)
        var ids = listOf(selectedColumn)
        var expectedSimpleTableWidget = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            selectedCount = 1,
            tab = BlockView.Table.Tab.COLUMN,
            items = listOf(
                SimpleTableWidgetItem.Column.InsertLeft(selectedColumn),
                SimpleTableWidgetItem.Column.InsertRight(selectedColumn),
                SimpleTableWidgetItem.Column.MoveLeft(
                    column = BlockView.Table.Column(
                        id = selectedColumn,
                        index = BlockView.Table.ColumnIndex(1)
                    )
                ),
                SimpleTableWidgetItem.Column.MoveRight(
                    column = BlockView.Table.Column(
                        id = selectedColumn,
                        index = BlockView.Table.ColumnIndex(1)
                    )
                ),
                SimpleTableWidgetItem.Column.Duplicate(column = selectedColumn),
                SimpleTableWidgetItem.Column.Delete(column = selectedColumn),
                SimpleTableWidgetItem.Column.ClearContents(ids),
                SimpleTableWidgetItem.Column.Color(ids),
                SimpleTableWidgetItem.Column.Style(ids),
                SimpleTableWidgetItem.Column.ResetStyle(ids)
            )
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

        assertEquals(
            expected = expectedViewState,
            actual = vm.state.test().value()
        )

        //TESTING Click Move Right

        vm.apply {

            fillTableColumn.stub {
                onBlocking {
                    invoke(
                        params = FillTableColumn.Params(
                            ctx = root,
                            targetIds = listOf(columnToMoveId)
                        )
                    )
                } doReturn Either.Right(
                    b = Payload(
                        context = context,
                        events = listOf()
                    )
                )
            }

            moveTableColumn.stub {
                onBlocking {
                    run(
                        params = MoveTableColumn.Params(
                            ctx = root,
                            column = columnToMoveId,
                            targetDrop = columns[2].id,
                            position = Position.RIGHT
                        )
                    )
                } doReturn Either.Right(
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.UpdateStructure(
                                context = root,
                                id = columnLayout.id,
                                children = listOf(columns[1].id, columns[2].id, columns[0].id)
                            ),
                            Event.Command.UpdateStructure(
                                context = root,
                                id = rows[0].id,
                                children = listOf(cellR0C1, cellR0C2, cellR0C0)
                            )
                        )
                    )
                )
            }

            onSimpleTableWidgetItemClicked(
                item = SimpleTableWidgetItem.Column.MoveRight(
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(value = columnToMoveId),
                        index = BlockView.Table.ColumnIndex(1)
                    )
                )
            )

            verifyNoMoreInteractions(fillTableRow)

            verify(fillTableColumn, times(2)).invoke(
                params = FillTableColumn.Params(
                    ctx = root,
                    targetIds = listOf(columnToMoveId)
                )
            )

            verify(moveTableColumn, times(1)).run(
                params = MoveTableColumn.Params(
                    ctx = root,
                    column = columnToMoveId,
                    targetDrop = columns[2].id,
                    position = Position.RIGHT
                )
            )
        }

        //EXPECTED

        expectedCells = listOf(
            mapToViewCell(
                textBlock = StubParagraph(id = cellR0C1, text = ""),
                column = columns[1],
                row = rows[0],
                columnIndex = 0,
                rowIndex = 0,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[1],
                columnIndex = 0,
                rowIndex = 1
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[2],
                columnIndex = 0,
                rowIndex = 2
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR0C2, text = ""),
                column = columns[2],
                row = rows[0],
                columnIndex = 1,
                rowIndex = 0,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[1],
                columnIndex = 1,
                rowIndex = 1,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[2],
                columnIndex = 1,
                rowIndex = 2,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR0C0, text = ""),
                column = columns[0],
                row = rows[0],
                columnIndex = 2,
                rowIndex = 0,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR1C0, text = ""),
                column = columns[0],
                row = rows[1],
                columnIndex = 2,
                rowIndex = 1,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR2C0, text = ""),
                column = columns[0],
                row = rows[2],
                columnIndex = 2,
                rowIndex = 2,
                mode = BlockView.Mode.READ
            )
        )

        expectedViewState = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.READ,
                ),
                BlockView.Table(
                    id = tableId,
                    columns = listOf(
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[1].id),
                            index = BlockView.Table.ColumnIndex(0)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[2].id),
                            index = BlockView.Table.ColumnIndex(1)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[0].id),
                            index = BlockView.Table.ColumnIndex(2)
                        )
                    ),
                    rows = listOf(
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[0].id),
                            index = BlockView.Table.RowIndex(0),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[1].id),
                            index = BlockView.Table.RowIndex(1),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[2].id),
                            index = BlockView.Table.RowIndex(2),
                            isHeader = false
                        )
                    ),
                    cells = expectedCells,
                    isSelected = false,
                    selectedCellsIds = listOf(cellR0C0, cellR1C0, cellR2C0),
                    tab = BlockView.Table.Tab.COLUMN
                )
            )
        )

        expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cellR0C0),
            targets = setOf(cellR0C0, cellR1C0, cellR2C0),
            tab = BlockView.Table.Tab.COLUMN
        )
        expectedSelectedState = listOf(cellR0C0, cellR1C0, cellR2C0)

        selectedColumn = BlockView.Table.ColumnId(columnToMoveId)
        ids = listOf(selectedColumn)
        expectedSimpleTableWidget = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            selectedCount = 1,
            tab = BlockView.Table.Tab.COLUMN,
            items = listOf(
                SimpleTableWidgetItem.Column.InsertLeft(selectedColumn),
                SimpleTableWidgetItem.Column.InsertRight(selectedColumn),
                SimpleTableWidgetItem.Column.MoveLeft(
                    column = BlockView.Table.Column(
                        id = selectedColumn,
                        index = BlockView.Table.ColumnIndex(2)
                    )
                ),
                SimpleTableWidgetItem.Column.Duplicate(column = selectedColumn),
                SimpleTableWidgetItem.Column.Delete(column = selectedColumn),
                SimpleTableWidgetItem.Column.ClearContents(ids),
                SimpleTableWidgetItem.Column.Color(ids),
                SimpleTableWidgetItem.Column.Style(ids),
                SimpleTableWidgetItem.Column.ResetStyle(ids)
            )
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

        assertEquals(
            expected = expectedViewState,
            actual = vm.state.test().value()
        )

        //TESTING Click Move Left

        vm.apply {

            fillTableColumn.stub {
                onBlocking {
                    invoke(
                        params = FillTableColumn.Params(
                            ctx = root,
                            targetIds = listOf(columnToMoveId)
                        )
                    )
                } doReturn Either.Right(
                    b = Payload(
                        context = context,
                        events = listOf()
                    )
                )
            }

            moveTableColumn.stub {
                onBlocking {
                    run(
                        params = MoveTableColumn.Params(
                            ctx = root,
                            column = columnToMoveId,
                            targetDrop = columns[2].id,
                            position = Position.LEFT
                        )
                    )
                } doReturn Either.Right(
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.UpdateStructure(
                                context = root,
                                id = columnLayout.id,
                                children = listOf(columns[1].id, columns[0].id, columns[2].id)
                            ),
                            Event.Command.UpdateStructure(
                                context = root,
                                id = rows[0].id,
                                children = listOf(cellR0C1, cellR0C0, cellR0C2)
                            )
                        )
                    )
                )
            }

            onSimpleTableWidgetItemClicked(
                item = SimpleTableWidgetItem.Column.MoveLeft(
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(value = columnToMoveId),
                        index = BlockView.Table.ColumnIndex(2)
                    )
                )
            )

            verifyNoMoreInteractions(fillTableRow)

            verify(fillTableColumn, times(3)).invoke(
                params = FillTableColumn.Params(
                    ctx = root,
                    targetIds = listOf(columnToMoveId)
                )
            )

            verify(moveTableColumn, times(1)).run(
                params = MoveTableColumn.Params(
                    ctx = root,
                    column = columnToMoveId,
                    targetDrop = columns[2].id,
                    position = Position.LEFT
                )
            )
        }

        //EXPECTED

        expectedCells = listOf(
            mapToViewCell(
                textBlock = StubParagraph(id = cellR0C1, text = ""),
                column = columns[1],
                row = rows[0],
                columnIndex = 0,
                rowIndex = 0,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[1],
                columnIndex = 0,
                rowIndex = 1
            ),
            mapToViewCell(
                column = columns[1],
                row = rows[2],
                columnIndex = 0,
                rowIndex = 2
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR0C0, text = ""),
                column = columns[0],
                row = rows[0],
                columnIndex = 1,
                rowIndex = 0,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR1C0, text = ""),
                column = columns[0],
                row = rows[1],
                columnIndex = 1,
                rowIndex = 1,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR2C0, text = ""),
                column = columns[0],
                row = rows[2],
                columnIndex = 1,
                rowIndex = 2,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                textBlock = StubParagraph(id = cellR0C2, text = ""),
                column = columns[2],
                row = rows[0],
                columnIndex = 2,
                rowIndex = 0,
                mode = BlockView.Mode.READ
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[1],
                columnIndex = 2,
                rowIndex = 1
            ),
            mapToViewCell(
                column = columns[2],
                row = rows[2],
                columnIndex = 2,
                rowIndex = 2
            )
        )

        expectedViewState = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.READ,
                ),
                BlockView.Table(
                    id = tableId,
                    columns = listOf(
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[1].id),
                            index = BlockView.Table.ColumnIndex(0)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[0].id),
                            index = BlockView.Table.ColumnIndex(1)
                        ),
                        BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(columns[2].id),
                            index = BlockView.Table.ColumnIndex(2)
                        )
                    ),
                    rows = listOf(
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[0].id),
                            index = BlockView.Table.RowIndex(0),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[1].id),
                            index = BlockView.Table.RowIndex(1),
                            isHeader = false
                        ),
                        BlockView.Table.Row(
                            id = BlockView.Table.RowId(rows[2].id),
                            index = BlockView.Table.RowIndex(2),
                            isHeader = false
                        )
                    ),
                    cells = expectedCells,
                    isSelected = false,
                    selectedCellsIds = listOf(cellR0C0, cellR1C0, cellR2C0),
                    tab = BlockView.Table.Tab.COLUMN
                )
            )
        )

        expectedMode = Editor.Mode.Table(
            tableId = table.id,
            initialTargets = setOf(cellR0C0),
            targets = setOf(cellR0C0, cellR1C0, cellR2C0),
            tab = BlockView.Table.Tab.COLUMN
        )
        expectedSelectedState = listOf(cellR0C0, cellR1C0, cellR2C0)

        selectedColumn = BlockView.Table.ColumnId(columnToMoveId)
        ids = listOf(selectedColumn)
        expectedSimpleTableWidget = ControlPanelState.Toolbar.SimpleTableWidget(
            isVisible = true,
            tableId = tableId,
            selectedCount = 1,
            tab = BlockView.Table.Tab.COLUMN,
            items = listOf(
                SimpleTableWidgetItem.Column.InsertLeft(selectedColumn),
                SimpleTableWidgetItem.Column.InsertRight(selectedColumn),
                SimpleTableWidgetItem.Column.MoveLeft(
                    column = BlockView.Table.Column(
                        id = selectedColumn,
                        index = BlockView.Table.ColumnIndex(1)
                    )
                ),
                SimpleTableWidgetItem.Column.MoveRight(
                    column = BlockView.Table.Column(
                        id = selectedColumn,
                        index = BlockView.Table.ColumnIndex(1)
                    )
                ),
                SimpleTableWidgetItem.Column.Duplicate(column = selectedColumn),
                SimpleTableWidgetItem.Column.Delete(column = selectedColumn),
                SimpleTableWidgetItem.Column.ClearContents(ids),
                SimpleTableWidgetItem.Column.Color(ids),
                SimpleTableWidgetItem.Column.Style(ids),
                SimpleTableWidgetItem.Column.ResetStyle(ids)
            )
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

        assertEquals(
            expected = expectedViewState,
            actual = vm.state.test().value()
        )


        coroutineTestRule.advanceTime(300L)
    }

    private fun mapToViewCell(
        textBlock: Block? = null,
        row: Block,
        rowIndex: Int,
        column: Block,
        columnIndex: Int,
        mode: BlockView.Mode = BlockView.Mode.EDIT
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
            block = if (textBlock != null) {
                BlockView.Text.Paragraph(
                    id = textBlock.id,
                    text = textBlock.content.asText().text,
                    mode = mode
                )
            } else {
                null
            },
            tableId = tableId
        )
    }
}