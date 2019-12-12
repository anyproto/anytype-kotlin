package com.agileburo.anytype.domain.ext

import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.common.MockDataFactory
import org.junit.Test
import kotlin.test.assertEquals

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
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
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
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
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
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
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
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
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
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
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
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
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
}