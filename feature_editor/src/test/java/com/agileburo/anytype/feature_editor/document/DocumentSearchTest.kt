package com.agileburo.anytype.feature_editor.document

import com.agileburo.anytype.feature_editor.domain.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull

class DocumentSearchTest {

    @Test
    fun searchFindsResultAsCopyInstance() {
        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Empty,
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Empty,
                        blockType = BlockType.Editable,
                        children = mutableListOf(
                            Block(
                                id = "3",
                                parentId = "2",
                                contentType = ContentType.P,
                                content = Content.Empty,
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
                content = Content.Empty,
                blockType = BlockType.Editable
            )
        )

        val result = document.search(id = "2")

        assertNotNull(result)

        assertEquals(
            expected = document.first().children.first(),
            actual = result
        )

        assertNotSame(
            illegal = document.first().children.first(),
            actual = result
        )
    }

    @Test
    fun searchFindsResultAsCopyInstanceAtRootLevel() {
        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Empty,
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Empty,
                        blockType = BlockType.Editable,
                        children = mutableListOf(
                            Block(
                                id = "3",
                                parentId = "2",
                                contentType = ContentType.P,
                                content = Content.Empty,
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
                content = Content.Empty,
                blockType = BlockType.Editable
            )
        )

        val result = document.search(id = "4")

        assertNotNull(result)

        assertEquals(
            expected = document.last(),
            actual = result
        )

        assertNotSame(
            illegal = document.last(),
            actual = result
        )
    }

    @Test
    fun searchFindsResultAsCopyInstanceAtChildrenLevel() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Empty,
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Empty,
                        blockType = BlockType.Editable,
                        children = mutableListOf(
                            Block(
                                id = "3",
                                parentId = "2",
                                contentType = ContentType.P,
                                content = Content.Empty,
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
                content = Content.Empty,
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "5",
                        parentId = "4",
                        contentType = ContentType.P,
                        content = Content.Empty,
                        blockType = BlockType.Editable,
                        children = mutableListOf(
                            Block(
                                id = "6",
                                parentId = "5",
                                contentType = ContentType.P,
                                content = Content.Empty,
                                blockType = BlockType.Editable
                            )
                        )
                    )
                )
            )
        )

        val result = document.search(id = "6")

        assertNotNull(result)

        assertEquals(
            expected = document.last().children.first().children.first(),
            actual = result
        )

        assertNotSame(
            illegal = document.last().children.first().children.first(),
            actual = result
        )
    }

    @Test
    fun searchFindsNoResultTest() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Empty,
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Empty,
                        blockType = BlockType.Editable,
                        children = mutableListOf(
                            Block(
                                id = "3",
                                parentId = "2",
                                contentType = ContentType.P,
                                content = Content.Empty,
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
                content = Content.Empty,
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "5",
                        parentId = "4",
                        contentType = ContentType.P,
                        content = Content.Empty,
                        blockType = BlockType.Editable,
                        children = mutableListOf(
                            Block(
                                id = "6",
                                parentId = "5",
                                contentType = ContentType.P,
                                content = Content.Empty,
                                blockType = BlockType.Editable
                            )
                        )
                    )
                )
            )
        )

        val result = document.search(id = "7")

        assertNull(result)
    }
}