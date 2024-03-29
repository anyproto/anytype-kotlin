package com.anytypeio.anytype.core_ui.features.editor.table

import android.content.Context
import android.os.Build
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.uitests.givenAdapter
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.TestFragment
import com.anytypeio.anytype.test_utils.utils.checkHasChildViewCount
import com.anytypeio.anytype.test_utils.utils.checkHasChildViewWithText
import com.anytypeio.anytype.test_utils.utils.checkIsDisplayed
import com.anytypeio.anytype.test_utils.utils.checkIsRecyclerSize
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.P],
    instrumentedPackages = ["androidx.loader.content"]
)
class TableBlockTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: FragmentScenario<TestFragment>

    @Before
    fun setUp() {
        context.setTheme(R.style.Theme_MaterialComponents)
        scenario = launchFragmentInContainer()
    }

    @Test
    fun `should render table block and update text in cell`() {

        val rowId1 = "rowId1"
        val columnId1 = "columnId1"
        val columnId2 = "columnId2"
        val columnId3 = "columnId3"
        val columnId4 = "columnId4"

        val oldText = "oldText"
        val newText = "NewText"

        val row1Block1 =
            StubParagraph(id = "$rowId1-$columnId2", text = "a1")
        val row1Block2 = StubParagraph(id = "$rowId1-$columnId3", text = oldText)
        val tableId = MockDataFactory.randomUuid()

        val cells = listOf(
            BlockView.Table.Cell(
                row = BlockView.Table.Row(
                    id = BlockView.Table.RowId(rowId1),
                    index = BlockView.Table.RowIndex(0),
                    isHeader = false
                ),
                column = BlockView.Table.Column(
                    id = BlockView.Table.ColumnId(columnId1),
                    index = BlockView.Table.ColumnIndex(0)
                ),
                block = null,
                tableId = tableId
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block1.id,
                    text = row1Block1.content.asText().text
                ),
                row = BlockView.Table.Row(
                    id = BlockView.Table.RowId(rowId1),
                    index = BlockView.Table.RowIndex(0),
                    isHeader = false
                ),
                column = BlockView.Table.Column(
                    id = BlockView.Table.ColumnId(columnId2),
                    index = BlockView.Table.ColumnIndex(1)
                ),
                tableId = tableId
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block2.id,
                    text = row1Block2.content.asText().text
                ),
                row = BlockView.Table.Row(
                    id = BlockView.Table.RowId(rowId1),
                    index = BlockView.Table.RowIndex(0),
                    isHeader = false
                ),
                column = BlockView.Table.Column(
                    id = BlockView.Table.ColumnId(columnId3),
                    index = BlockView.Table.ColumnIndex(2)
                ),
                tableId = tableId
            ),
            BlockView.Table.Cell(
                row = BlockView.Table.Row(
                    id = BlockView.Table.RowId(rowId1),
                    index = BlockView.Table.RowIndex(0),
                    isHeader = false
                ),
                column = BlockView.Table.Column(
                    id = BlockView.Table.ColumnId(columnId4),
                    index = BlockView.Table.ColumnIndex(3)
                ),
                block = null,
                tableId = tableId
            )
        )

        val cellsNew = listOf(
            BlockView.Table.Cell(
                row = BlockView.Table.Row(
                    id = BlockView.Table.RowId(rowId1),
                    index = BlockView.Table.RowIndex(0),
                    isHeader = false
                ),
                column = BlockView.Table.Column(
                    id = BlockView.Table.ColumnId(columnId1),
                    index = BlockView.Table.ColumnIndex(0)
                ),
                block = null,
                tableId = tableId
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block1.id,
                    text = row1Block1.content.asText().text
                ),
                row = BlockView.Table.Row(
                    id = BlockView.Table.RowId(rowId1),
                    index = BlockView.Table.RowIndex(0),
                    isHeader = false
                ),
                column = BlockView.Table.Column(
                    id = BlockView.Table.ColumnId(columnId2),
                    index = BlockView.Table.ColumnIndex(1)
                ),
                tableId = tableId
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block2.id,
                    text = newText
                ),
                row = BlockView.Table.Row(
                    id = BlockView.Table.RowId(rowId1),
                    index = BlockView.Table.RowIndex(0),
                    isHeader = false
                ),
                column = BlockView.Table.Column(
                    id = BlockView.Table.ColumnId(columnId3),
                    index = BlockView.Table.ColumnIndex(2)
                ),
                tableId = tableId
            ),
            BlockView.Table.Cell(
                row = BlockView.Table.Row(
                    id = BlockView.Table.RowId(rowId1),
                    index = BlockView.Table.RowIndex(0),
                    isHeader = false
                ),
                column = BlockView.Table.Column(
                    id = BlockView.Table.ColumnId(columnId4),
                    index = BlockView.Table.ColumnIndex(3)
                ),
                block = null,
                tableId = tableId
            )
        )

        val columns = listOf(
            BlockView.Table.Column(
                id = BlockView.Table.ColumnId(columnId1),
                index = BlockView.Table.ColumnIndex(0)
            ),
            BlockView.Table.Column(
                id = BlockView.Table.ColumnId(columnId2),
                index = BlockView.Table.ColumnIndex(1)
            ),
            BlockView.Table.Column(
                id = BlockView.Table.ColumnId(columnId3),
                index = BlockView.Table.ColumnIndex(2)
            ),
            BlockView.Table.Column(
                id = BlockView.Table.ColumnId(columnId4),
                index = BlockView.Table.ColumnIndex(3)
            )
        )

        val rows = listOf(
            BlockView.Table.Row(
                id = BlockView.Table.RowId(rowId1),
                index = BlockView.Table.RowIndex(0),
                isHeader = false
            )
        )

        scenario.onFragment {
            it.view?.updateLayoutParams {
                width = 1200
            }
            val recycler = givenRecycler(it)

            val views = listOf<BlockView>(
                BlockView.Table(
                    id = tableId,
                    cells = cells,
                    columns = columns,
                    rows = rows,
                    isSelected = false,
                    selectedCellsIds = emptyList(),
                    tab = null,
                    decorations = listOf()
                )
            )
            val adapter = givenAdapter(views)
            recycler.adapter = adapter

            com.anytypeio.anytype.test_utils.R.id.recycler.rVMatcher().apply {

                onItemView(0, R.id.recyclerTable).checkIsDisplayed()

                onItemView(0, R.id.recyclerTable).checkHasChildViewCount(4)

                onItemView(0, R.id.recyclerTable).checkHasChildViewWithText(
                    1,
                    row1Block1.content.asText().text,
                    R.id.textContent
                ).checkIsDisplayed()

                onItemView(0, R.id.recyclerTable).checkHasChildViewWithText(
                    2,
                    row1Block2.content.asText().text,
                    R.id.textContent
                ).checkIsDisplayed()
            }

            val viewsUpdated = listOf<BlockView>(
                BlockView.Table(
                    id = tableId,
                    cells = cellsNew,
                    columns = columns,
                    rows = rows,
                    isSelected = false,
                    selectedCellsIds = emptyList(),
                    tab = null,
                    decorations = listOf()
                )
            )

            adapter.updateWithDiffUtil(viewsUpdated)

            com.anytypeio.anytype.test_utils.R.id.recycler.rVMatcher().apply {
                checkIsRecyclerSize(1)
                onItemView(0, R.id.recyclerTable).checkIsDisplayed()

                onItemView(0, R.id.recyclerTable).checkHasChildViewCount(4)

                onItemView(0, R.id.recyclerTable).checkHasChildViewWithText(
                    1,
                    row1Block1.content.asText().text,
                    R.id.textContent
                ).checkIsDisplayed()

                onItemView(0, R.id.recyclerTable).checkHasChildViewWithText(
                    2,
                    newText,
                    R.id.textContent
                ).checkIsDisplayed()
            }
        }
    }

    private fun givenRecycler(it: Fragment): RecyclerView =
        it.view!!.findViewById<RecyclerView>(com.anytypeio.anytype.test_utils.R.id.recycler).apply {
            layoutManager = LinearLayoutManager(context)
        }
}