package com.agileburo.anytype.feature_editor.document

import com.agileburo.anytype.feature_editor.domain.*
import com.agileburo.anytype.feature_editor.factory.DataFactory
import com.agileburo.anytype.feature_editor.presentation.model.BlockView
import org.junit.Test
import kotlin.test.assertEquals

class DocumentRenderingTests {

    @Test
    fun toggleBlockCollapsedByDefaultTest() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    param = ContentParam.empty(),
                    marks = emptyList()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.Toggle,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            param = ContentParam.empty(),
                            marks = emptyList()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf(
                            Block(
                                id = "3",
                                parentId = "2",
                                contentType = ContentType.P,
                                content = Content.Text(
                                    text = DataFactory.randomString(),
                                    param = ContentParam.empty(),
                                    marks = emptyList()
                                ),
                                blockType = BlockType.Editable
                            )
                        )
                    )
                )
            ),
            Block(
                id = "4",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    param = ContentParam.empty(),
                    marks = emptyList()
                ),
                blockType = BlockType.Editable
            )
        )

        val view = document.toView()

        assertEquals(
            expected = 2,
            actual = view.size
        )
    }

    @Test
    fun collapsedToggleBlockIndentationTest() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    param = ContentParam.empty(),
                    marks = emptyList()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.Toggle,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            param = ContentParam.empty(),
                            marks = emptyList()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf(
                            Block(
                                id = "3",
                                parentId = "2",
                                contentType = ContentType.P,
                                content = Content.Text(
                                    text = DataFactory.randomString(),
                                    param = ContentParam.empty(),
                                    marks = emptyList()
                                ),
                                blockType = BlockType.Editable
                            )
                        )
                    )
                )
            ),
            Block(
                id = "4",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    param = ContentParam.empty(),
                    marks = emptyList()
                ),
                blockType = BlockType.Editable
            )
        )

        val view = document.toView()

        assertEquals(
            expected = 0,
            actual = (view.first() as BlockView.ToggleView).indent
        )

        assertEquals(
            expected = 0,
            actual = (view.last() as BlockView.ParagraphView).indent
        )
    }

    @Test
    fun expandedToggleBlockIndentationTest() {

        val document : Document = mutableListOf(
            Block(
                state = Block.State.expanded(true),
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    param = ContentParam.empty(),
                    marks = emptyList()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        state = Block.State.expanded(true),
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.Toggle,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            param = ContentParam.empty(),
                            marks = emptyList()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf(
                            Block(
                                id = "3",
                                parentId = "2",
                                contentType = ContentType.P,
                                content = Content.Text(
                                    text = DataFactory.randomString(),
                                    param = ContentParam.empty(),
                                    marks = emptyList()
                                ),
                                blockType = BlockType.Editable
                            )
                        )
                    )
                )
            ),
            Block(
                id = "4",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    param = ContentParam.empty(),
                    marks = emptyList()
                ),
                blockType = BlockType.Editable
            )
        )

        val view = document.toView()

        assertEquals(
            expected = 4,
            actual = view.size
        )

        assertEquals(
            expected = 0,
            actual = (view[0] as BlockView.ToggleView).indent
        )

        assertEquals(
            expected = 1,
            actual = (view[1] as BlockView.ToggleView).indent
        )

        assertEquals(
            expected = 2,
            actual = (view[2] as BlockView.ParagraphView).indent
        )

        assertEquals(
            expected = 0,
            actual = (view.last() as BlockView.ParagraphView).indent
        )
    }

    @Test
    fun partlyExpandedToggleBlockIndentationTest() {

        val document : Document = mutableListOf(
            Block(
                state = Block.State.expanded(true),
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    param = ContentParam.empty(),
                    marks = emptyList()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        state = Block.State.expanded(false),
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.Toggle,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            param = ContentParam.empty(),
                            marks = emptyList()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf(
                            Block(
                                id = "3",
                                parentId = "2",
                                contentType = ContentType.P,
                                content = Content.Text(
                                    text = DataFactory.randomString(),
                                    param = ContentParam.empty(),
                                    marks = emptyList()
                                ),
                                blockType = BlockType.Editable
                            )
                        )
                    )
                )
            ),
            Block(
                id = "4",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    param = ContentParam.empty(),
                    marks = emptyList()
                ),
                blockType = BlockType.Editable
            )
        )

        val view = document.toView()

        assertEquals(
            expected = 3,
            actual = view.size
        )

        assertEquals(
            expected = 0,
            actual = (view[0] as BlockView.ToggleView).indent
        )

        assertEquals(
            expected = 1,
            actual = (view[1] as BlockView.ToggleView).indent
        )

        assertEquals(
            expected = 0,
            actual = (view.last() as BlockView.ParagraphView).indent
        )
    }

    @Test
    fun `if checkbox block has children, they are indentable`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Check,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    param = ContentParam.empty(),
                    marks = emptyList()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            param = ContentParam.empty(),
                            marks = emptyList()
                        ),
                        blockType = BlockType.Editable
                    ),
                    Block(
                        id = "3",
                        parentId = "1",
                        contentType = ContentType.H1,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            param = ContentParam.empty(),
                            marks = emptyList()
                        ),
                        blockType = BlockType.Editable
                    ),
                    Block(
                        id = "4",
                        parentId = "1",
                        contentType = ContentType.Quote,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            param = ContentParam.empty(),
                            marks = emptyList()
                        ),
                        blockType = BlockType.Editable
                    )
                )
            )
        )

        assertEquals(1, document.size)

        val view = document.toView()

        assertEquals(
            expected = 4,
            actual = view.size
        )

        assertEquals(
            expected = 0,
            actual = (view[0] as BlockView.CheckboxView).indent
        )

        assertEquals(
            expected = 1,
            actual = (view[1] as BlockView.ParagraphView).indent
        )

        assertEquals(
            expected = 1,
            actual = (view[2] as BlockView.HeaderView).indent
        )

        assertEquals(
            expected = 1,
            actual = (view[3] as BlockView.QuoteView).indent
        )
    }

    @Test
    fun `if bullet list block has children, they are indentable`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.UL,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    param = ContentParam.empty(),
                    marks = emptyList()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            param = ContentParam.empty(),
                            marks = emptyList()
                        ),
                        blockType = BlockType.Editable
                    ),
                    Block(
                        id = "3",
                        parentId = "1",
                        contentType = ContentType.H1,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            param = ContentParam.empty(),
                            marks = emptyList()
                        ),
                        blockType = BlockType.Editable
                    ),
                    Block(
                        id = "4",
                        parentId = "1",
                        contentType = ContentType.Quote,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            param = ContentParam.empty(),
                            marks = emptyList()
                        ),
                        blockType = BlockType.Editable
                    )
                )
            )
        )

        assertEquals(1, document.size)

        val view = document.toView()

        assertEquals(
            expected = 4,
            actual = view.size
        )

        assertEquals(
            expected = 0,
            actual = (view[0] as BlockView.BulletView).indent
        )

        assertEquals(
            expected = 1,
            actual = (view[1] as BlockView.ParagraphView).indent
        )

        assertEquals(
            expected = 1,
            actual = (view[2] as BlockView.HeaderView).indent
        )

        assertEquals(
            expected = 1,
            actual = (view[3] as BlockView.QuoteView).indent
        )
    }

    @Test
    fun `if numbered list block has children, they are indentable`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.NumberedList,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    param = ContentParam.empty(),
                    marks = emptyList()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            param = ContentParam.empty(),
                            marks = emptyList()
                        ),
                        blockType = BlockType.Editable
                    ),
                    Block(
                        id = "3",
                        parentId = "1",
                        contentType = ContentType.H1,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            param = ContentParam.empty(),
                            marks = emptyList()
                        ),
                        blockType = BlockType.Editable
                    ),
                    Block(
                        id = "4",
                        parentId = "1",
                        contentType = ContentType.Quote,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            param = ContentParam.empty(),
                            marks = emptyList()
                        ),
                        blockType = BlockType.Editable
                    )
                )
            )
        )

        assertEquals(1, document.size)

        val view = document.toView()

        assertEquals(
            expected = 4,
            actual = view.size
        )

        assertEquals(
            expected = 0,
            actual = (view[0] as BlockView.NumberListItemView).indent
        )

        assertEquals(
            expected = 1,
            actual = (view[1] as BlockView.ParagraphView).indent
        )

        assertEquals(
            expected = 1,
            actual = (view[2] as BlockView.HeaderView).indent
        )

        assertEquals(
            expected = 1,
            actual = (view[3] as BlockView.QuoteView).indent
        )
    }
}