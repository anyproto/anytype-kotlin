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

        val cells = listOf(
            BlockView.Table.Cell.Empty(
                rowId = rowId1,
                columnId = columnId1
            ),
            BlockView.Table.Cell.Text(
                block = BlockView.Text.Paragraph(
                    id = row1Block1.id,
                    text = row1Block1.content.asText().text
                ),
                rowId = rowId1,
                columnId = columnId2
            ),
            BlockView.Table.Cell.Text(
                block = BlockView.Text.Paragraph(
                    id = row1Block2.id,
                    text = row1Block2.content.asText().text
                ),
                rowId = rowId1,
                columnId = columnId3

            ),
            BlockView.Table.Cell.Empty(
                rowId = rowId1,
                columnId = columnId4
            )
        )

        val cellsNew = listOf(
            BlockView.Table.Cell.Empty(
                rowId = rowId1,
                columnId = columnId1,
                settings = BlockView.Table.CellSettings(
                    width = 140
                )
            ),
            BlockView.Table.Cell.Text(
                block = BlockView.Text.Paragraph(
                    id = row1Block1.id,
                    text = row1Block1.content.asText().text
                ),
                settings = BlockView.Table.CellSettings(
                    width = 140
                ),
                rowId = rowId1,
                columnId = columnId2
            ),
            BlockView.Table.Cell.Text(
                block = BlockView.Text.Paragraph(
                    id = row1Block2.id,
                    text = newText
                ),
                settings = BlockView.Table.CellSettings(
                    width = 140
                ),
                rowId = rowId1,
                columnId = columnId3
            ),
            BlockView.Table.Cell.Empty(
                rowId = rowId1,
                columnId = columnId4,
                settings = BlockView.Table.CellSettings(
                    width = 140
                )
            )
        )

        val columns = listOf(
            BlockView.Table.Column(id = columnId1, backgroundColor = null),
            BlockView.Table.Column(id = columnId2, backgroundColor = null),
            BlockView.Table.Column(id = columnId3, backgroundColor = null),
            BlockView.Table.Column(id = columnId4, backgroundColor = null)
        )

        scenario.onFragment {
            it.view?.updateLayoutParams {
                width = 1200
            }
            val recycler = givenRecycler(it)

            val tableId = MockDataFactory.randomUuid()
            val views = listOf<BlockView>(
                BlockView.Table(
                    id = tableId,
                    cells = cells,
                    columns = columns,
                    rowCount = 1,
                    isSelected = false
                )
            )
            val adapter = givenAdapter(views)
            recycler.adapter = adapter

            com.anytypeio.anytype.test_utils.R.id.recycler.rVMatcher().apply {

                onItemView(0, R.id.recyclerTable).checkIsDisplayed()

                onItemView(0, R.id.recyclerTable).checkHasChildViewCount(4)

                onItemView(0, R.id.recyclerTable).checkHasChildViewWithText(
                    0,
                    "",
                    R.id.textContent
                ).checkIsDisplayed()

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
                    rowCount = 1,
                    isSelected = false
                )
            )

            adapter.updateWithDiffUtil(viewsUpdated)

            com.anytypeio.anytype.test_utils.R.id.recycler.rVMatcher().apply {
                checkIsRecyclerSize(1)
                onItemView(0, R.id.recyclerTable).checkIsDisplayed()

                onItemView(0, R.id.recyclerTable).checkHasChildViewCount(4)


                onItemView(0, R.id.recyclerTable).checkHasChildViewWithText(
                    0,
                    "",
                    R.id.textContent
                ).checkIsDisplayed()

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