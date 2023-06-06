package com.anytypeio.anytype.presentation.editor.editor.table

import android.util.Log
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.StubBulleted
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubLayoutColumns
import com.anytypeio.anytype.core_models.StubLayoutRows
import com.anytypeio.anytype.core_models.StubNumbered
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.StubTable
import com.anytypeio.anytype.core_models.StubTableColumn
import com.anytypeio.anytype.core_models.StubTableRow
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.ext.asMap
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.render.BlockViewRenderer
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.editor.render.parseThemeBackgroundColor
import com.anytypeio.anytype.presentation.editor.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.util.TXT
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class TableBlockRendererTest {

    class BlockViewRenderWrapper(
        private val blocks: Map<Id, List<Block>>,
        private val renderer: BlockViewRenderer,
        private val restrictions: List<ObjectRestriction> = emptyList(),
        private val selections: Set<Id> = emptySet()
    ) : BlockViewRenderer by renderer {
        suspend fun render(
            root: Block,
            anchor: Id,
            focus: Editor.Focus,
            indent: Int,
            details: Block.Details
        ): List<BlockView> = blocks.render(
            root = root,
            anchor = anchor,
            focus = focus,
            indent = indent,
            details = details,
            relationLinks = emptyList(),
            restrictions = restrictions,
            selection = selections
        )
    }

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(true)
        .showTimestamp(false)
        .onlyLogWhenTestFails(true)
        .build()

    @Mock
    lateinit var toggleStateHolder: ToggleStateHolder

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var coverImageHashProvider: CoverImageHashProvider

    private lateinit var renderer: DefaultBlockViewRenderer

    private lateinit var wrapper: BlockViewRenderWrapper

    private var storeOfRelations = DefaultStoreOfRelations()

    private val storeOfObjectTypes = DefaultStoreOfObjectTypes()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        renderer = DefaultBlockViewRenderer(
            urlBuilder = UrlBuilder(gateway),
            toggleStateHolder = toggleStateHolder,
            coverImageHashProvider = coverImageHashProvider,
            storeOfRelations = storeOfRelations,
            storeOfObjectTypes = storeOfObjectTypes
        )
    }

    @Test
    fun `should return table block with columns, rows and text cells`() {

        val blocksUpper = mutableListOf<Block>()
        val blocksDown = mutableListOf<Block>()

        for (i in 1..5) {
            val blockU = StubBulleted()
            blocksUpper.add(blockU)
            val blockD = StubNumbered()
            blocksDown.add(blockD)
        }

        val rowsSize = 6
        val columnsSize = 4

        val mapRows = mutableMapOf<String, List<Block>>()
        val rows = mutableListOf<Block>()
        val columns = mutableListOf<Block>()

        for (i in 1..rowsSize) {
            val rowId = "rowId$i"
            val cells = mutableListOf<Block>()
            for (j in 1..columnsSize) {
                val columnId = "columnId$j"
                val cellId = "$rowId-$columnId"
                val p = StubParagraph(id = cellId)
                cells.add(p)
            }
            val row = StubTableRow(id = rowId, children = cells.map { it.id })
            rows.add(row)
            mapRows[row.id] = cells
        }
        val layoutRow = StubLayoutRows(children = rows.map { it.id })

        for (j in 1..columnsSize) {
            columns.add(StubTableColumn(id = "columnId$j"))
        }
        val layoutColumn = StubLayoutColumns(children = columns.map { it.id })

        val table = StubTable(children = listOf(layoutColumn.id, layoutRow.id))

        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))

        val page = Block(
            id = "page",
            children = listOf(header.id) + blocksUpper.map { it.id } + listOf(table.id) + blocksDown.map { it.id },
            fields = Block.Fields.empty(),
            content = Block.Content.Smart
        )

        val l = mutableListOf<Block>()
        columns.forEach { l.add(it) }
        l.add(layoutRow)
        layoutRow.children.forEach { child ->
            val row = rows.first { it.id == child }
            l.add(row)
            val cells = mapRows[row.id]
            cells?.forEach { l.add(it) }
        }

        val blocks =
            listOf(page, header) + blocksUpper + listOf(table, title, layoutColumn) + l + blocksDown

        /**
         * Should be 25 blocks + 6 rows + 4 columns + layoutRow + layoutColumn + table + title + header + 10 text blocks + page
         */
        assertEquals(50, blocks.size)

        val details = mapOf(page.id to Block.Fields.empty())

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.empty(),
                indent = 0,
                details = Block.Details(details)
            )
        }

        val cells = mutableListOf<BlockView.Table.Cell>()
        columns.forEachIndexed { columnIndex, column ->
            rows.forEachIndexed { rowIndex, row ->
                val cell = mapRows[row.id]?.get(columnIndex)!!
                val p = BlockView.Text.Paragraph(
                    id = cell.id,
                    text = cell.content.asText().text
                )
                cells.add(
                    BlockView.Table.Cell(
                        block = p,
                        row = BlockView.Table.Row(
                            id = BlockView.Table.RowId(row.id),
                            index = BlockView.Table.RowIndex(rowIndex),
                            isHeader = false
                        ),
                        column = BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(column.id),
                            index = BlockView.Table.ColumnIndex(columnIndex)
                        ),
                        tableId = table.id
                    )
                )
            }
        }

        val columnViews = mutableListOf<BlockView.Table.Column>()
        columns.forEachIndexed { index, column ->
            columnViews.add(
                BlockView.Table.Column(
                    id = BlockView.Table.ColumnId(column.id),
                    index = BlockView.Table.ColumnIndex(index)
                )
            )
        }

        val rowViews = mutableListOf<BlockView.Table.Row>()
        rows.forEachIndexed { index, row ->
            rowViews.add(
                BlockView.Table.Row(
                    id = BlockView.Table.RowId(value = row.id),
                    index = BlockView.Table.RowIndex(value = index),
                    isHeader = false
                )
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null
            )
        ) + blocksUpper.map { block: Block ->
            BlockView.Text.Bulleted(
                id = block.id,
                text = block.content<TXT>().text,
                decorations = listOf(
                    BlockView.Decoration(
                        background = block.parseThemeBackgroundColor()
                    )
                )
            )
        } + listOf(
            BlockView.Table(
                id = table.id,
                cells = cells,
                columns = columnViews,
                rows = rowViews,
                isSelected = false,
                selectedCellsIds = emptyList(),
                tab = null,
                decorations = listOf(BlockView.Decoration())
            )
        ) + blocksDown.mapIndexed { idx, block ->
            BlockView.Text.Numbered(
                id = block.id,
                text = block.content<TXT>().text,
                number = idx.inc(),
                decorations = listOf(
                    BlockView.Decoration(
                        background = block.parseThemeBackgroundColor()
                    )
                )
            )
        }

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should return table block with columns, rows and empty cells`() {

        val blocksUpper = mutableListOf<Block>()
        val blocksDown = mutableListOf<Block>()

        for (i in 1..5) {
            val blockU = StubBulleted()
            blocksUpper.add(blockU)
            val blockD = StubNumbered()
            blocksDown.add(blockD)
        }

        val rowsSize = 6
        val columnsSize = 4

        val rows = mutableListOf<Block>()
        val columns = mutableListOf<Block>()

        for (i in 1..rowsSize) {
            val rowId = "rowId$i"
            rows.add(StubTableRow(id = rowId))
        }
        val layoutRow = StubLayoutRows(children = rows.map { it.id })

        for (j in 1..columnsSize) {
            columns.add(StubTableColumn(id = "columnId$j"))
        }
        val layoutColumn = StubLayoutColumns(children = columns.map { it.id })

        val table = StubTable(children = listOf(layoutColumn.id, layoutRow.id))

        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))

        val page = Block(
            id = "page",
            children = listOf(header.id) + blocksUpper.map { it.id } + listOf(table.id) + blocksDown.map { it.id },
            fields = Block.Fields.empty(),
            content = Block.Content.Smart
        )

        val l = mutableListOf<Block>()
        columns.forEach { l.add(it) }
        l.add(layoutRow)
        layoutRow.children.forEach { child ->
            val row = rows.first { it.id == child }
            l.add(row)
        }

        val blocks =
            listOf(page, header) + blocksUpper + listOf(table, title, layoutColumn) + l + blocksDown

        /**
         * 6 rows + 4 columns + layoutRow + layoutColumn + table + title + header + 10 text blocks + page
         */
        assertEquals(26, blocks.size)

        val details = mapOf(page.id to Block.Fields.empty())

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.empty(),
                indent = 0,
                details = Block.Details(details)
            )
        }

        val cells = mutableListOf<BlockView.Table.Cell>()
        columns.forEachIndexed { columnIndex, column ->
            rows.forEachIndexed { rowIndex, row ->
                cells.add(
                    BlockView.Table.Cell(
                        row = BlockView.Table.Row(
                            id = BlockView.Table.RowId(row.id),
                            index = BlockView.Table.RowIndex(rowIndex),
                            isHeader = false
                        ),
                        column = BlockView.Table.Column(
                            id = BlockView.Table.ColumnId(column.id),
                            index = BlockView.Table.ColumnIndex(columnIndex)
                        ),
                        block = null,
                        tableId = table.id
                    )
                )
            }
        }

        val columnViews = mutableListOf<BlockView.Table.Column>()
        columns.forEachIndexed { index, column ->
            columnViews.add(
                BlockView.Table.Column(
                    id = BlockView.Table.ColumnId(column.id),
                    index = BlockView.Table.ColumnIndex(index)
                )
            )
        }

        val rowViews = mutableListOf<BlockView.Table.Row>()
        rows.forEachIndexed { index, row ->
            rowViews.add(
                BlockView.Table.Row(
                    id = BlockView.Table.RowId(value = row.id),
                    index = BlockView.Table.RowIndex(value = index),
                    isHeader = false
                )
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null
            )
        ) + blocksUpper.map { block: Block ->
            BlockView.Text.Bulleted(
                id = block.id,
                text = block.content<TXT>().text,
                decorations = listOf(
                    BlockView.Decoration(
                        background = ThemeColor.DEFAULT
                    )
                )
            )
        } + listOf(
            BlockView.Table(
                id = table.id,
                cells = cells,
                columns = columnViews,
                rows = rowViews,
                isSelected = false,
                selectedCellsIds = emptyList(),
                tab = null,
                decorations = listOf(BlockView.Decoration())
            )
        ) + blocksDown.mapIndexed { idx, block ->
            BlockView.Text.Numbered(
                id = block.id,
                text = block.content<TXT>().text,
                number = idx.inc(),
                decorations = listOf(
                    BlockView.Decoration(
                        background = ThemeColor.DEFAULT
                    )
                )
            )
        }

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should return table block with columns, rows and empty plus text cells`() {

        val blocksUpper = mutableListOf<Block>()
        val blocksDown = mutableListOf<Block>()

        for (i in 1..5) {
            val blockU = StubBulleted()
            blocksUpper.add(blockU)
            val blockD = StubNumbered()
            blocksDown.add(blockD)
        }

        val rowsSize = 3
        val columnsSize = 4

        val mapRows = mutableMapOf<String, List<Block>>()
        val rows = mutableListOf<Block>()
        val columns = mutableListOf<Block>()

        val rowId1 = "rowId1"
        val rowId2 = "rowId2"
        val rowId3 = "rowId3"
        val columnId1 = "columnId1"
        val columnId2 = "columnId2"
        val columnId3 = "columnId3"
        val columnId4 = "columnId4"

        val row1Block1 = StubParagraph(id = "$rowId1-$columnId2")
        val row1Block2 = StubParagraph(id = "$rowId1-$columnId4")
        val row2Block1 = StubParagraph(id = "$rowId2-$columnId1")
        val row2Block2 = StubParagraph(id = "$rowId2-$columnId2")
        val row2Block3 = StubParagraph(id = "$rowId2-$columnId4")

        rows.apply {
            add(StubTableRow(rowId1, listOf(row1Block1.id, row1Block2.id)))
            add(StubTableRow(rowId2, listOf(row2Block1.id, row2Block2.id, row2Block3.id)))
            add(StubTableRow(rowId3))
            mapRows[rowId1] = listOf(row1Block1, row1Block2)
            mapRows[rowId2] = listOf(row2Block1, row2Block2, row2Block3)
        }

        val layoutRow = StubLayoutRows(children = rows.map { it.id })

        for (j in 1..columnsSize) {
            columns.add(StubTableColumn(id = "columnId$j"))
        }
        val layoutColumn = StubLayoutColumns(children = columns.map { it.id })

        val table = StubTable(children = listOf(layoutColumn.id, layoutRow.id))

        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))

        val page = Block(
            id = "page",
            children = listOf(header.id) + blocksUpper.map { it.id } + listOf(table.id) + blocksDown.map { it.id },
            fields = Block.Fields.empty(),
            content = Block.Content.Smart
        )

        val l = mutableListOf<Block>()
        columns.forEach { l.add(it) }
        l.add(layoutRow)
        layoutRow.children.forEach { child ->
            val row = rows.first { it.id == child }
            l.add(row)
            val cells = mapRows[row.id]
            cells?.forEach { l.add(it) }
        }

        val blocks =
            listOf(page, header) + blocksUpper + listOf(table, title, layoutColumn) + l + blocksDown

        assertEquals(28, blocks.size)

        val details = mapOf(page.id to Block.Fields.empty())

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer,
            selections = setOf("$rowId2-$columnId1")
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.empty(),
                indent = 0,
                details = Block.Details(details)
            )
        }

        val cells =
            listOf(
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
                    tableId = table.id
                ),
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId2),
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId1),
                        index = BlockView.Table.ColumnIndex(0)
                    ),
                    block = BlockView.Text.Paragraph(
                        id = row2Block1.id,
                        text = row2Block1.content.asText().text
                    ),
                    tableId = table.id
                ),
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId3),
                        index = BlockView.Table.RowIndex(2),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId1),
                        index = BlockView.Table.ColumnIndex(0)
                    ),
                    block = null,
                    tableId = table.id
                ), //column1
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId1),
                        index = BlockView.Table.RowIndex(0),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId2),
                        index = BlockView.Table.ColumnIndex(1)
                    ),
                    block = BlockView.Text.Paragraph(
                        id = row1Block1.id,
                        text = row1Block1.content.asText().text
                    ),
                    tableId = table.id
                ),
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId2),
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId2),
                        index = BlockView.Table.ColumnIndex(1)
                    ),
                    block = BlockView.Text.Paragraph(
                        id = row2Block2.id,
                        text = row2Block2.content.asText().text
                    ),
                    tableId = table.id
                ),
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId3),
                        index = BlockView.Table.RowIndex(2),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId2),
                        index = BlockView.Table.ColumnIndex(1)
                    ),
                    block = null,
                    tableId = table.id
                ),//column2
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId1),
                        index = BlockView.Table.RowIndex(0),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId3),
                        index = BlockView.Table.ColumnIndex(2)
                    ),
                    block = null,
                    tableId = table.id
                ),
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId2),
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId3),
                        index = BlockView.Table.ColumnIndex(2)
                    ),
                    block = null,
                    tableId = table.id
                ),
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId3),
                        index = BlockView.Table.RowIndex(2),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId3),
                        index = BlockView.Table.ColumnIndex(2)
                    ),
                    block = null,
                    tableId = table.id
                ),//column3
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
                    block = BlockView.Text.Paragraph(
                        id = row1Block2.id,
                        text = row1Block2.content.asText().text
                    ),
                    tableId = table.id
                ),
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId2),
                        index = BlockView.Table.RowIndex(1),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId4),
                        index = BlockView.Table.ColumnIndex(3)
                    ),
                    block = BlockView.Text.Paragraph(
                        id = row2Block3.id,
                        text = row2Block3.content.asText().text
                    ),
                    tableId = table.id
                ),
                BlockView.Table.Cell(
                    row = BlockView.Table.Row(
                        id = BlockView.Table.RowId(rowId3),
                        index = BlockView.Table.RowIndex(2),
                        isHeader = false
                    ),
                    column = BlockView.Table.Column(
                        id = BlockView.Table.ColumnId(columnId4),
                        index = BlockView.Table.ColumnIndex(3)
                    ),
                    block = null,
                    tableId = table.id
                )
            )

        val columnViews = mutableListOf<BlockView.Table.Column>()
        columns.forEachIndexed { index, column ->
            columnViews.add(
                BlockView.Table.Column(
                    id = BlockView.Table.ColumnId(column.id),
                    index = BlockView.Table.ColumnIndex(index)
                )
            )
        }

        val rowViews = mutableListOf<BlockView.Table.Row>()
        rows.forEachIndexed { index, row ->
            rowViews.add(
                BlockView.Table.Row(
                    id = BlockView.Table.RowId(value = row.id),
                    index = BlockView.Table.RowIndex(value = index),
                    isHeader = false
                )
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null
            )
        ) + blocksUpper.map { block: Block ->
            BlockView.Text.Bulleted(
                id = block.id,
                text = block.content<TXT>().text,
                decorations = listOf(
                    BlockView.Decoration(
                        background = ThemeColor.DEFAULT
                    )
                )
            )
        } + listOf(
            BlockView.Table(
                id = table.id,
                cells = cells,
                columns = columnViews,
                rows = rowViews,
                isSelected = false,
                selectedCellsIds = listOf("$rowId2-$columnId1"),
                tab = null,
                decorations = listOf(BlockView.Decoration())
            )
        ) + blocksDown.mapIndexed { idx, block ->
            BlockView.Text.Numbered(
                id = block.id,
                text = block.content<TXT>().text,
                number = idx.inc(),
                decorations = listOf(
                    BlockView.Decoration(
                        background = ThemeColor.DEFAULT
                    )
                )
            )
        }

        assertEquals(expected = expected, actual = result)
    }
}