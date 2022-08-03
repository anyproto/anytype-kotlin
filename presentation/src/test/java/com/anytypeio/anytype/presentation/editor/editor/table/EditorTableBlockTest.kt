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
import com.anytypeio.anytype.domain.table.FillTableRow
import com.anytypeio.anytype.presentation.editor.editor.EditorPresentationTestSetup
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
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

    @Test
    fun `should amend second empty cell click`() {

        val columns = StubTableColumns(size = 3)
        val rows = StubTableRows(size = 2)
        val cells = StubTableCells(columns = listOf(), rows = listOf())
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

        val cell1Id = "${rows[0].id}-${columns[1].id}"
        val cell2Id = "${rows[1].id}-${columns[0].id}"

        vm.onStart(root)

        vm.apply {
            onClickListener(
                ListenerType.TableEmptyCell(
                    cellId = cell1Id,
                    rowId = rows[0].id,
                    tableId = table.id
                )
            )
            onClickListener(
                ListenerType.TableEmptyCell(
                    cellId = cell2Id,
                    rowId = rows[1].id,
                    tableId = table.id
                )
            )
        }


        val selectedState = vm.currentSelection()

        assertEquals(1, selectedState.size)
        assertEquals(cell1Id, selectedState.first())

        runBlocking {
            verify(fillTableRow, times(1)).invoke(
                params = FillTableRow.Params(ctx = root, targetIds = listOf(rows[0].id))
            )
        }
    }

    @Test
    fun `should amend second text cell click`() {

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

        val cell1Id = "${rows[0].id}-${columns[1].id}"
        val cell2Id = "${rows[1].id}-${columns[0].id}"

        vm.onStart(root)

        vm.apply {
            onClickListener(
                ListenerType.TableTextCell(
                    cellId = cell1Id,
                    tableId = table.id
                )
            )
            onClickListener(
                ListenerType.TableTextCell(
                    cellId = cell2Id,
                    tableId = table.id
                )
            )
        }

        val selectedState = vm.currentSelection()
        runBlocking {
            assertEquals(1, selectedState.size)
            assertEquals(cell1Id, selectedState.first())
            verifyZeroInteractions(fillTableRow)
        }
    }
}