package com.anytypeio.anytype.domain.ext

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.StubFile
import com.anytypeio.anytype.core_models.ext.asMap
import com.anytypeio.anytype.core_models.ext.asRender
import com.anytypeio.anytype.core_models.ext.getChildrenIdsList
import com.anytypeio.anytype.core_models.ext.getFirstLinkOrObjectMarkupParam
import com.anytypeio.anytype.core_models.ext.getSubstring
import com.anytypeio.anytype.core_models.ext.isAllTextAndNoneCodeBlocks
import com.anytypeio.anytype.core_models.ext.numbers
import com.anytypeio.anytype.core_models.ext.rangeIntersection
import com.anytypeio.anytype.core_models.ext.title
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Test

class BlockExtensionTest {

    @Test
    fun `should create a data structure that maps block ids to its children`() {

        val child = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                marks = emptyList(),
                style = Block.Content.Text.Style.P,
                text = MockDataFactory.randomString()
            ),
            children = emptyList()
        )

        val root = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart,
            children = listOf(child.id)
        )

        val blocks = listOf(root, child)

        val map = blocks.asMap()

        assertEquals(
            expected = listOf(child),
            actual = map.getValue(root.id)
        )

        assertEquals(
            expected = emptyList(),
            actual = map.getValue(child.id)
        )
    }

    @Test
    fun `should generate a map with correct children order`() {

        val children = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            )
        )

        val root = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart,
            children = listOf(children[1].id, children[0].id, children[2].id)
        )

        val blocks = listOf(root, children[0], children[1], children[2])

        val map = blocks.asMap()

        assertEquals(
            expected = listOf(children[1], children[0], children[2]),
            actual = map.getValue(root.id)
        )
    }

    @Test
    fun `should create a map containing root, parent and its children`() {

        val children = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            )
        )

        val parent = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.COLUMN
            ),
            children = children.map { it.id }
        )

        val root = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart,
            children = listOf(parent.id)
        )

        val blocks = listOf(root, parent, children[0], children[1])

        val map = blocks.asMap()

        assertEquals(
            expected = listOf(parent),
            actual = map.getValue(root.id)
        )

        assertEquals(
            expected = children,
            actual = map.getValue(parent.id)
        )
    }

    @Test
    fun `should return list for rendering containing two children`() {

        val children = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            )
        )

        val parent = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.COLUMN
            ),
            children = children.map { it.id }
        )

        val root = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart,
            children = listOf(parent.id)
        )

        val blocks = listOf(root, parent, children[0], children[1])

        val rendering = blocks.asMap().asRender(anchor = root.id)

        assertEquals(
            expected = children.size,
            actual = rendering.size
        )

        assertEquals(
            expected = children[0],
            actual = rendering[0]
        )

        assertEquals(
            expected = children[1],
            actual = rendering[1]
        )
    }

    @Test
    fun `should return list for rendering containing four blocks`() {

        val grandchildren = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            )
        )

        val children = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Layout(
                    type = Block.Content.Layout.Type.COLUMN
                ),
                children = grandchildren.map { it.id }
            )
        )

        val parent = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.COLUMN
            ),
            children = children.map { it.id }
        )

        val root = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart,
            children = listOf(parent.id)
        )

        val blocks = listOf(root, parent) + children + grandchildren

        val rendering = blocks.asMap().asRender(anchor = root.id)

        assertEquals(
            expected = 3,
            actual = rendering.size
        )

        assertEquals(
            expected = children[0],
            actual = rendering[0]
        )

        assertEquals(
            expected = grandchildren[0],
            actual = rendering[1]
        )

        assertEquals(
            expected = grandchildren[1],
            actual = rendering[2]
        )
    }

    @Test
    fun `should render one column after another then the last block`() {

        val firstColumnChildren = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            )
        )

        val secondColumnChildren = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            )
        )

        val firstColumn = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.COLUMN
            ),
            children = firstColumnChildren.map { it.id }
        )

        val secondColumn = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.COLUMN
            ),
            children = secondColumnChildren.map { it.id }
        )

        val lastBlock = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                marks = emptyList(),
                style = Block.Content.Text.Style.P,
                text = MockDataFactory.randomString()
            ),
            children = emptyList()
        )

        val row = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Layout(
                Block.Content.Layout.Type.ROW
            ),
            children = listOf(firstColumn.id, secondColumn.id)
        )

        val root = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart,
            children = listOf(row.id, lastBlock.id)
        )

        val blocks = listOf(
            root,
            row
        ) + lastBlock + firstColumn + secondColumn + firstColumnChildren + secondColumnChildren

        val rendering = blocks.shuffled().asMap().asRender(anchor = root.id)

        assertEquals(
            expected = firstColumnChildren[0],
            actual = rendering[0]
        )

        assertEquals(
            expected = firstColumnChildren[1],
            actual = rendering[1]
        )

        assertEquals(
            expected = secondColumnChildren[0],
            actual = rendering[2]
        )

        assertEquals(
            expected = secondColumnChildren[1],
            actual = rendering[3]
        )

        assertEquals(
            expected = lastBlock,
            actual = rendering[4]
        )
    }

    @Test
    fun `should return substring of block text by range`() {
        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                marks = emptyList(),
                style = Block.Content.Text.Style.P,
                text = "Test block 123"
            ),
            children = emptyList()
        )

        val range = IntRange(5, 12)

        val result = block.getSubstring(range)

        assertEquals("block 12", result)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun `should return error when range is out of bounds`() {
        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                marks = emptyList(),
                style = Block.Content.Text.Style.P,
                text = "Test"
            ),
            children = emptyList()
        )
        val range = IntRange(0, 4)

        block.getSubstring(range)
    }

    @Test(expected = ClassCastException::class)
    fun `should return error when block not text`() {
        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart,
            children = emptyList()
        )
        val range = IntRange(0, 44)

        block.getSubstring(range)
    }

    @Test
    fun `should return not empty range intersection`() {
        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 5,
                            endInclusive = 8
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    )
                ),
                style = Block.Content.Text.Style.P,
                text = "Test Bold text"
            ),
            children = emptyList()
        )
        val range = IntRange(7, 144)

        val result = block.content.asText().marks[0].rangeIntersection(range)

        assertEquals(2, result)
    }

    @Test
    fun `should return empty range intersection`() {
        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 5,
                            endInclusive = 8
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    )
                ),
                style = Block.Content.Text.Style.P,
                text = "Test Bold text"
            ),
            children = emptyList()
        )
        val range = IntRange(0, 4)

        val result = block.content.asText().marks[0].rangeIntersection(range)

        assertEquals(0, result)
    }

    @Test
    fun `should return link markup`() {
        val link = Block.Content.Text.Mark(
            range = IntRange(
                start = 10,
                endInclusive = 13
            ),
            type = Block.Content.Text.Mark.Type.LINK,
            param = "www.anytype.io/test"
        )
        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 5,
                            endInclusive = 8
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 10,
                            endInclusive = 13
                        ),
                        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                    ),
                    link
                ),
                style = Block.Content.Text.Style.P,
                text = "Test Bold text"
            ),
            children = emptyList()
        )
        val range = IntRange(10, 13)

        val result = block.getFirstLinkOrObjectMarkupParam(range)

        assertEquals(link.param, result)
    }

    @Test
    fun `should return nullable markup`() {
        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 5,
                            endInclusive = 8
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 10,
                            endInclusive = 13
                        ),
                        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                    )
                ),
                style = Block.Content.Text.Style.P,
                text = "Test Bold text"
            ),
            children = emptyList()
        )
        val range = IntRange(10, 13)

        val result = block.getFirstLinkOrObjectMarkupParam(range)

        assertNull(result)
    }

    @Test
    fun `should return first link markup`() {
        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 0,
                            endInclusive = 8
                        ),
                        type = Block.Content.Text.Mark.Type.LINK,
                        param = "https://kotlinlang.ru"
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 0,
                            endInclusive = 8
                        ),
                        type = Block.Content.Text.Mark.Type.LINK,
                        param = "https://ya.ru/"
                    )
                ),
                style = Block.Content.Text.Style.P,
                text = "Test Bold text"
            ),
            children = emptyList()
        )
        val range = IntRange(0, 8)

        val result = block.getFirstLinkOrObjectMarkupParam(range)

        assertEquals("https://kotlinlang.ru", result)
    }

    @Test
    fun `should return nullable markup when no marks in block`() {
        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                marks = emptyList(),
                style = Block.Content.Text.Style.P,
                text = "Test Bold text"
            ),
            children = emptyList()
        )
        val range = IntRange(10, 13)

        val result = block.getFirstLinkOrObjectMarkupParam(range)

        assertNull(result)
    }

    @Test(expected = ClassCastException::class)
    fun `should throw exception when block is not text`() {

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Divider(style = Block.Content.Divider.Style.LINE),
            children = emptyList()
        )
        val range = IntRange(10, 13)

        val result = block.getFirstLinkOrObjectMarkupParam(range)

        assertNull(result)
    }

    @Test
    fun `should return map with numbered list data`() {

        val blocks = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.TITLE,
                    marks = emptyList()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.NUMBERED,
                    marks = emptyList()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.NUMBERED,
                    marks = emptyList()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.NUMBERED,
                    marks = emptyList()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.TITLE,
                    marks = emptyList()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.NUMBERED,
                    marks = emptyList()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.NUMBERED,
                    marks = emptyList()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.NUMBERED,
                    marks = emptyList()
                ),
                children = emptyList()
            )
        )

        val numbers = blocks.numbers()

        val expected = mapOf(
            blocks[1].id to 1,
            blocks[2].id to 2,
            blocks[3].id to 3,
            blocks[5].id to 1,
            blocks[6].id to 2,
            blocks[7].id to 3
        )

        assertEquals(
            expected = expected,
            actual = numbers
        )
    }

    @Test
    fun `should return list of ids of root block childs`() {

        val children = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            )
        )

        val parent = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.COLUMN
            ),
            children = listOf(children[1].id, children[0].id)
        )

        val blocks = listOf(parent, children[0], children[1])

        val childrenIdsList = blocks.getChildrenIdsList(parent.id)

        val expected = listOf(children[1].id, children[0].id)

        assertEquals(
            expected = expected,
            actual = childrenIdsList
        )
    }

    @Test
    fun `should return empty list of ids of root block childs when parent id is wrong`() {

        val children = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P,
                    text = MockDataFactory.randomString()
                ),
                children = emptyList()
            )
        )

        val parent = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.COLUMN
            ),
            children = listOf(children[1].id, children[0].id)
        )

        val blocks = listOf(parent, children[0], children[1])

        val childrenIdsList = blocks.getChildrenIdsList("errorid")

        val expected = emptyList<String>()

        assertEquals(
            expected = expected,
            actual = childrenIdsList
        )
    }

    @Test
    fun `should return title block for document`() {

        val root = MockDataFactory.randomUuid()

        val title = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, a.id)
        )

        val document = listOf(page, header, title, a)

        val result = document.title()

        val expected = title

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should return null when title block is not present in header childs`() {

        val root = MockDataFactory.randomUuid()

        val header = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, a.id)
        )

        val document = listOf(page, header, a)

        val result = document.title()

        assertNull(result)
    }

    @Test
    fun `should return null when header is not present`() {

        val root = MockDataFactory.randomUuid()

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(a.id)
        )

        val document = listOf(page, a)

        val result = document.title()

        assertNull(result)
    }

    @Test
    fun `should return true on all non code and texted blocks`() {

        val root = MockDataFactory.randomUuid()

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = (Block.Content.Text.Style.P)
            )
        )

        val document = listOf(a, b)

        val result = document.isAllTextAndNoneCodeBlocks()

        assertTrue(result)
    }

    @Test
    fun `should return false on texted blocks and code block`() {

        val root = MockDataFactory.randomUuid()

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = (Block.Content.Text.Style.P)
            )
        )

        val c = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = (Block.Content.Text.Style.CODE_SNIPPET)
            )
        )

        val document = listOf(a, b, c)

        val result = document.isAllTextAndNoneCodeBlocks()

        assertFalse(result)
    }

    @Test
    fun `should return false on media blocks`() {

        val root = MockDataFactory.randomUuid()

        val a = StubFile()

        val b = StubFile()

        val document = listOf(a, b)

        val result = document.isAllTextAndNoneCodeBlocks()

        assertFalse(result)
    }
}