package com.anytypeio.anytype.core_ui.features.editor.table

import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.table.holders.TableBlockHolder
import com.anytypeio.anytype.core_ui.uitests.givenAdapter
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PARAGRAPH
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_TABLE
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.TestFragment
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

/**
 * Tests for TableBlockHolder cleanup behavior when recycled.
 *
 * These tests verify the fix for the crash:
 * "IllegalArgumentException: called detach on an already detached child TableBlockHolder"
 *
 * The fix adds proper cleanup when TableBlockHolder is recycled via BlockAdapter.onViewRecycled(),
 * safely removing decorations and clearing state to prevent crashes when the nested RecyclerView
 * is in an inconsistent state.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.P],
    instrumentedPackages = ["androidx.loader.content"]
)
class TableBlockHolderCleanupTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: FragmentScenario<TestFragment>

    @Before
    fun setup() {
        context.setTheme(R.style.Theme_MaterialComponents)
        scenario = launchFragmentInContainer()
    }

    @Test
    fun `should call cleanup on table block holder when recycled`() {
        scenario.onFragment { fragment ->
            // Given
            val recycler = givenRecycler(fragment)
            val tableBlock = givenTableBlockWithSelection()
            val adapter = givenAdapter(views = listOf(tableBlock))
            recycler.adapter = adapter

            val holder = adapter.onCreateViewHolder(recycler, HOLDER_TABLE)
            adapter.onBindViewHolder(holder, 0)
            assertTrue(holder is TableBlockHolder)

            // When - should not throw
            adapter.onViewRecycled(holder)
        }
    }

    @Test
    fun `should safely recycle table block holder with no selection`() {
        scenario.onFragment { fragment ->
            // Given
            val recycler = givenRecycler(fragment)
            val tableBlock = givenTableBlockWithoutSelection()
            val adapter = givenAdapter(views = listOf(tableBlock))
            recycler.adapter = adapter

            val holder = adapter.onCreateViewHolder(recycler, HOLDER_TABLE)
            adapter.onBindViewHolder(holder, 0)
            assertTrue(holder is TableBlockHolder)

            // When - should not throw
            adapter.onViewRecycled(holder)
        }
    }

    @Test
    fun `should not affect paragraph holder when recycled`() {
        scenario.onFragment { fragment ->
            // Given
            val recycler = givenRecycler(fragment)
            val paragraph = BlockView.Text.Paragraph(
                id = MockDataFactory.randomUuid(),
                text = "Test paragraph"
            )
            val adapter = givenAdapter(views = listOf(paragraph))
            recycler.adapter = adapter

            val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)
            adapter.onBindViewHolder(holder, 0)

            // When - should not throw
            adapter.onViewRecycled(holder)
        }
    }

    @Test
    fun `should handle multiple recycle cycles on same table holder`() {
        scenario.onFragment { fragment ->
            // Given
            val recycler = givenRecycler(fragment)
            val tableBlock = givenTableBlockWithSelection()
            val adapter = givenAdapter(views = listOf(tableBlock))
            recycler.adapter = adapter

            val holder = adapter.onCreateViewHolder(recycler, HOLDER_TABLE)
            assertTrue(holder is TableBlockHolder)

            // When - bind, recycle, rebind, recycle multiple times
            adapter.onBindViewHolder(holder, 0)
            adapter.onViewRecycled(holder)

            adapter.onBindViewHolder(holder, 0)
            adapter.onViewRecycled(holder)

            adapter.onBindViewHolder(holder, 0)
            adapter.onViewRecycled(holder)

            // Should not throw on any cycle
        }
    }

    private fun givenRecycler(fragment: Fragment): RecyclerView =
        fragment.view!!.findViewById<RecyclerView>(com.anytypeio.anytype.test_utils.R.id.recycler).apply {
            layoutManager = LinearLayoutManager(context)
        }

    private fun givenTableBlockWithSelection(): BlockView.Table {
        val tableId = MockDataFactory.randomUuid()
        val rowId = "row1"
        val columnId = "col1"

        return BlockView.Table(
            id = tableId,
            cells = listOf(
                BlockView.Table.Cell(
                    tableId = tableId,
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId),
                        index = BlockView.Table.RowIndex(0),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId),
                        index = BlockView.Table.ColumnIndex(0)
                    ),
                    block = null
                )
            ),
            columns = listOf(
                BlockView.Table.Column(
                    id = BlockView.Table.ColumnId(columnId),
                    index = BlockView.Table.ColumnIndex(0)
                )
            ),
            rows = listOf(
                BlockView.Table.Row(
                    id = BlockView.Table.RowId(rowId),
                    index = BlockView.Table.RowIndex(0),
                    isHeader = false
                )
            ),
            selectedCellsIds = listOf("$rowId-$columnId"),
            decorations = emptyList(),
            isSelected = false,
            tab = null
        )
    }

    private fun givenTableBlockWithoutSelection(): BlockView.Table {
        val tableId = MockDataFactory.randomUuid()
        val rowId = "row1"
        val columnId = "col1"

        return BlockView.Table(
            id = tableId,
            cells = listOf(
                BlockView.Table.Cell(
                    tableId = tableId,
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId),
                        index = BlockView.Table.RowIndex(0),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId),
                        index = BlockView.Table.ColumnIndex(0)
                    ),
                    block = null
                )
            ),
            columns = listOf(
                BlockView.Table.Column(
                    id = BlockView.Table.ColumnId(columnId),
                    index = BlockView.Table.ColumnIndex(0)
                )
            ),
            rows = listOf(
                BlockView.Table.Row(
                    id = BlockView.Table.RowId(rowId),
                    index = BlockView.Table.RowIndex(0),
                    isHeader = false
                )
            ),
            selectedCellsIds = emptyList(),
            decorations = emptyList(),
            isSelected = false,
            tab = null
        )
    }
}
