package com.agileburo.anytype.feature_editor.document

import com.agileburo.anytype.feature_editor.domain.*
import com.agileburo.anytype.feature_editor.factory.BlockFactory
import com.agileburo.anytype.feature_editor.factory.DataFactory
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DocumentModificationTests {

    @Test
    fun flatteningDocumentTest() {

        val blocks = listOf(
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

        val unwrapped = blocks.toMutableList().flat()

        assertEquals(4, unwrapped.size)
        assertEquals(unwrapped.first(), blocks.first().copy(children = mutableListOf()))
        assertEquals(unwrapped[1], blocks.first().children.first().copy(children = mutableListOf()))
        assertEquals(unwrapped[2], blocks.first().children.first().children.first().copy(children = mutableListOf()))
        assertEquals(unwrapped.last(), blocks.last().copy(children = mutableListOf()))
    }

    @Test
    fun flatThenConvertToGraphTest() {

        val blocks = listOf(
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

        val unwrapped = blocks.toMutableList().flat()

        val graph = unwrapped.toMutableList().graph()

        assertEquals(blocks, graph)
    }

    @Test
    fun testChangeContentTypeAtRootLevel() {

        val blocks = mutableListOf(
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
            ),
            Block(
                id = "5",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Empty,
                blockType = BlockType.Editable
            )
        )

        blocks.changeContentType(targetId = "4", targetType = ContentType.H1)
        blocks.changeContentType(targetId = "5", targetType = ContentType.H2)

        assertEquals(ContentType.H1, blocks[1].contentType)
        assertEquals(ContentType.H2, blocks[2].contentType)
    }

    @Test
    fun changeContentTypeAtChildrenLevel() {

        val blocks = mutableListOf(
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
            ),
            Block(
                id = "5",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Empty,
                blockType = BlockType.Editable
            )
        )

        blocks.changeContentType(targetId = "2", targetType = ContentType.H1)
        blocks.changeContentType(targetId = "3", targetType = ContentType.H2)


        assertEquals(ContentType.H1, blocks.first().children.first().contentType)
        assertEquals(ContentType.H2, blocks.first().children.first().children.first().contentType)
    }

    @Test
    fun fixNumberTestAtRootLevel() {

        val blocks = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.NumberedList,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.numberedList(0)
                ),
                blockType = BlockType.Editable
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.NumberedList,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.numberedList(0)
                ),
                blockType = BlockType.Editable
            ),
            Block(
                id = "3",
                parentId = "",
                contentType = ContentType.NumberedList,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.numberedList(0)
                ),
                blockType = BlockType.Editable
            )
        )

        blocks.fixNumberOrder()

        assertEquals(
            expected = 1,
            actual = (blocks.first().content as Content.Text).param.number
        )

        assertEquals(
            expected = 2,
            actual = (blocks[1].content as Content.Text).param.number
        )

        assertEquals(
            expected = 3,
            actual = (blocks.last().content as Content.Text).param.number
        )
    }

    @Test
    fun fixNumberAtChildrenLevel() {

        val blocks = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "4",
                        parentId = "1",
                        blockType = BlockType.Editable,
                        contentType = ContentType.NumberedList,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.numberedList(0)
                        )
                    ),
                    Block(
                        id = "5",
                        parentId = "1",
                        blockType = BlockType.Editable,
                        contentType = ContentType.NumberedList,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.numberedList(0)
                        )
                    ),
                    Block(
                        id = "6",
                        parentId = "1",
                        blockType = BlockType.Editable,
                        contentType = ContentType.NumberedList,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.numberedList(0)
                        )
                    )
                )
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "7",
                        parentId = "2",
                        blockType = BlockType.Editable,
                        contentType = ContentType.NumberedList,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.numberedList(0)
                        )
                    ),
                    Block(
                        id = "8",
                        parentId = "2",
                        blockType = BlockType.Editable,
                        contentType = ContentType.NumberedList,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.numberedList(0)
                        )
                    ),
                    Block(
                        id = "9",
                        parentId = "2",
                        blockType = BlockType.Editable,
                        contentType = ContentType.NumberedList,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.numberedList(0)
                        )
                    )
                )
            )
        )

        blocks.fixNumberOrder()

        assertEquals(
            expected = 1,
            actual = (blocks.first().children.first().content as Content.Text).param.number
        )

        assertEquals(
            expected = 2,
            actual = (blocks.first().children[1].content as Content.Text).param.number
        )

        assertEquals(
            expected = 3,
            actual = (blocks.first().children.last().content as Content.Text).param.number
        )

        assertEquals(
            expected = 1,
            actual = (blocks.last().children.first().content as Content.Text).param.number
        )

        assertEquals(
            expected = 2,
            actual = (blocks.last().children[1].content as Content.Text).param.number
        )

        assertEquals(
            expected = 3,
            actual = (blocks.last().children.last().content as Content.Text).param.number
        )
    }

    @Test
    fun updateBlockTakesDataFromContent() {

        val block = BlockFactory.makeBlock(
            parentId = ""
        )

        val blocks = mutableListOf(block)

        val update = (block.content as Content.Text).copy(
            text = DataFactory.randomString()
        )

        blocks.updateContent(
            targetId = block.id,
            targetContentUpdate = update
        )

        assertEquals(
            expected = update,
            actual = blocks.first().content
        )
    }

    @Test
    fun updateToggleTitleTest() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.Toggle,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf(
                            Block(
                                id = "3",
                                parentId = "2",
                                contentType = ContentType.P,
                                content = Content.Text(
                                    text = DataFactory.randomString(),
                                    marks = emptyList(),
                                    param = ContentParam.empty()
                                ),
                                blockType = BlockType.Editable
                            )
                        )
                    )
                )
            )
        )

        val update = (document.first().content as Content.Text).copy(
            text = DataFactory.randomString()
        )

        document.updateContent(
            targetId = document.first().id,
            targetContentUpdate = update
        )

        assertEquals(
            expected = update,
            actual = document.first().content
        )
    }

    @Test
    fun changeToggleContentTypeAtRootLevelTest() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf()
                    )
                )
            )
        )

        assertEquals(1, document.size)

        document.changeContentType(
            targetId = document.first().id,
            targetType = ContentType.P
        )

        assertEquals(ContentType.P, document.first().contentType)
        assertEquals(true, document.first().children.isEmpty())
        assertEquals(2, document.size)
        assertEquals("2", document.last().id)
        assertTrue { document.last().parentId.isEmpty() }
        assertEquals(ContentType.P, document.last().contentType)
    }

    @Test
    fun changeContentTypeAndInsertInBetweenAtRootLevelTest() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf()
                    )
                )
            ),
            Block(
                id = "3",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            )
        )

        assertEquals(2, document.size)

        document.changeContentType(
            targetId = document.first().id,
            targetType = ContentType.P
        )


        assertEquals(3, document.size)
        assertEquals("1", document.first().id)
        assertEquals("2", document[1].id)
        assertEquals("3", document.last().id)

        assertEquals(ContentType.P, document.first().contentType)
        assertEquals(true, document.first().children.isEmpty())
        assertTrue { document[1].parentId.isEmpty() }

        assertEquals(ContentType.P, document[1].contentType)
    }

    @Test
    fun changeContentTypeByToggleWithSeveralChildrenAndInsertInBetweenAtRootLevelTest() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf()
                    ),
                    Block(
                        id = "3",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf()
                    )
                )
            ),
            Block(
                id = "4",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            )
        )

        assertEquals(2, document.size)

        document.changeContentType(
            targetId = document.first().id,
            targetType = ContentType.P
        )


        assertEquals(4, document.size)
        assertEquals("1", document.first().id)
        assertEquals("2", document[1].id)
        assertEquals("3", document[2].id)
        assertEquals("4", document.last().id)

        document.forEach { block ->
            assertTrue { block.parentId.isEmpty() }
        }

        assertEquals(ContentType.P, document.first().contentType)
        assertEquals(true, document.first().children.isEmpty())

        assertEquals(ContentType.P, document[1].contentType)
        assertEquals(ContentType.P, document[2].contentType)
    }

    @Test
    fun changeContentTypeDoesNotAffectGrandChildren() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.Toggle,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf(
                            Block(
                                id = "3",
                                parentId = "2",
                                contentType = ContentType.P,
                                content = Content.Text(
                                    text = DataFactory.randomString(),
                                    marks = emptyList(),
                                    param = ContentParam.empty()
                                ),
                                blockType = BlockType.Editable,
                                children = mutableListOf(

                                )
                            )
                        )
                    )
                )
            )
        )

        assertEquals(1, document.size)

        document.changeContentType(
            targetId = document.first().id,
            targetType = ContentType.P
        )

        assertEquals(2, document.size)
        assertEquals("1", document.first().id)
        assertEquals("2", document.last().id)

        assertTrue { document.first().children.isEmpty() }
        assertTrue { document.last().children.isNotEmpty() }
        assertTrue { document.last().parentId.isEmpty() }

        assertEquals(ContentType.P, document.first().contentType)
        assertEquals(ContentType.Toggle, document.last().contentType)
    }

    @Test
    fun changeContentTypeByToggleBlockAtLastRootLevelPosition() {

        val document : Document = mutableListOf(
            Block(
                id = "3",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            ),
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf()
                    )
                )
            )
        )

        assertEquals(2, document.size)

        document.changeContentType(
            targetId = document.last().id,
            targetType = ContentType.P
        )

        assertEquals(3, document.size)
        assertEquals("3", document.first().id)
        assertEquals("1", document[1].id)
        assertEquals("2", document.last().id)

        document.forEach { block ->
            assertTrue { block.parentId.isEmpty() }
        }

        assertEquals(ContentType.P, document[1].contentType)
        assertEquals(true, document[1].children.isEmpty())

        assertEquals(ContentType.P, document.last().contentType)
    }

    @Test
    fun changeContentTypeByToggleBlockAtMiddleRootLevelPosition() {

        val document : Document = mutableListOf(
            Block(
                id = "3",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            ),
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf()
                    )
                )
            ),
            Block(
                id = "4",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            )
        )

        assertEquals(3, document.size)

        document.changeContentType(
            targetId = document[1].id,
            targetType = ContentType.P
        )

        assertEquals(4, document.size)
        assertEquals("3", document.first().id)
        assertEquals("1", document[1].id)
        assertEquals("2", document[2].id)
        assertEquals("4", document.last().id)

        document.forEach { block ->
            assertTrue { block.parentId.isEmpty() }
        }

        assertEquals(ContentType.P, document[1].contentType)
        assertEquals(true, document[1].children.isEmpty())

        assertEquals(ContentType.P, document[2].contentType)
    }

    @Test
    fun testChangeToggleBlockContentTypeAtChildLevel() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.Toggle,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf(
                            Block(
                                id = "3",
                                parentId = "2",
                                contentType = ContentType.P,
                                content = Content.Text(
                                    text = DataFactory.randomString(),
                                    marks = emptyList(),
                                    param = ContentParam.empty()
                                ),
                                blockType = BlockType.Editable,
                                children = mutableListOf(

                                )
                            )
                        )
                    )
                )
            )
        )

        assertEquals(1, document.size)

        document.changeContentType(
            targetId = document.first().children.first().id,
            targetType = ContentType.P
        )

        assertEquals(1, document.size)
        assertEquals(2, document.first().children.size)

        document.first().let { parent ->
            parent.children.forEach { child ->
                assertTrue { child.parentId == parent.id }
            }
        }
    }

    @Test
    fun basicDeleteAtRootLevelTest() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            )
        )

        assertEquals(1, document.size)

        document.delete(document.first().id)

        assertTrue { document.isEmpty() }
    }

    @Test
    fun deleteFromRootLevelTest() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            )
        )

        assertEquals(2, document.size)

        document.delete(document.first().id)

        assertTrue { document.size == 1 }
        assertFalse { document.any { block -> block.id == "1" } }
        assertEquals("2", document.first().id)
    }

    @Test
    fun basicChildDeletionTest() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf()
                    )
                )
            )
        )

        assertEquals(1, document.size)

        document.delete(document.first().children.first().id)

        assertTrue { document.size == 1 }
        assertTrue { document.first().children.isEmpty() }
        assertEquals("1", document.first().id)
    }

    @Test
    fun shouldDeleteChildInTheMiddle() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf()
                    ),
                    Block(
                        id = "3",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf()
                    ),
                    Block(
                        id = "4",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf()
                    )
                )
            )
        )

        assertEquals(1, document.size)
        assertEquals(3, document.first().children.size)

        document.delete(targetId = "3")

        assertTrue { document.size == 1 }
        assertEquals("1", document.first().id)
        assertEquals(2, document.first().children.size)
        assertEquals("2", document.first().children.first().id)
        assertEquals("4", document.first().children.last().id)
        assertFalse { document.first().children.any { block -> block.id == "3" } }
    }

    @Test(expected = IllegalStateException::class)
    fun `if consumer is missing, we get an exception`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf()
                    )
                )
            )
        )

        document.consume(
            consumerId = "3",
            consumableId = "1"
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `if consumable is not found, we get an exception`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf()
                    )
                )
            )
        )

        document.consume(
            consumerId = "1",
            consumableId = "3"
        )
    }

    @Test
    fun `toggle block can consume a block even if it is not empty`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable,
                        children = mutableListOf()
                    )
                )
            ),
            Block(
                id = "3",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            )
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.consume(
            consumerId = document.first().id,
            consumableId = document.last().id
        )

        assertEquals(
            expected = 1,
            actual = document.size
        )

        document.first().apply {
            assertEquals(expected = "1", actual = id)
            assertTrue { parentId.isEmpty() }

            assertEquals(expected = 2, actual = children.size)

            children.first().apply {
                assertEquals(expected = "2", actual = id)
                assertEquals(expected = "1", actual = parentId)
            }

            children.last().apply {
                assertEquals(expected = "3", actual = id)
                assertEquals(expected = "1", actual = parentId)
            }
        }
    }

    @Test
    fun `toggle block can consume a block even it is empty`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            )
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.consume(
            consumerId = document.first().id,
            consumableId = document.last().id
        )

        assertEquals(
            expected = 1,
            actual = document.size
        )

        document.first().apply {
            assertEquals(expected = "1", actual = id)
            assertTrue { parentId.isEmpty() }

            assertEquals(expected = 1, actual = children.size)

            children.first().apply {
                assertEquals(expected = "2", actual = id)
                assertEquals(expected = "1", actual = parentId)
            }
        }
    }

    @Test
    fun `if consumer is a numbered list, it can consume given consumable`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.NumberedList,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            )
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.consume(
            consumerId = document.first().id,
            consumableId = document.last().id
        )

        assertEquals(
            expected = 1,
            actual = document.size
        )

        document.first().apply {
            assertEquals(expected = "1", actual = id)
            assertTrue { parentId.isEmpty() }

            assertEquals(expected = 1, actual = children.size)

            children.first().apply {
                assertEquals(expected = "2", actual = id)
                assertEquals(expected = "1", actual = parentId)
            }
        }
    }

    @Test
    fun `if consumer is a bullet list, it can consume given consumable`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.UL,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            )
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.consume(
            consumerId = document.first().id,
            consumableId = document.last().id
        )

        assertEquals(
            expected = 1,
            actual = document.size
        )

        document.first().apply {
            assertEquals(expected = "1", actual = id)
            assertTrue { parentId.isEmpty() }

            assertEquals(expected = 1, actual = children.size)

            children.first().apply {
                assertEquals(expected = "2", actual = id)
                assertEquals(expected = "1", actual = parentId)
            }
        }
    }

    @Test
    fun `if consumer is a checkbox block, it can consume given consumable`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Check,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            )
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.consume(
            consumerId = document.first().id,
            consumableId = document.last().id
        )

        assertEquals(
            expected = 1,
            actual = document.size
        )

        document.first().apply {
            assertEquals(expected = "1", actual = id)
            assertTrue { parentId.isEmpty() }

            assertEquals(expected = 1, actual = children.size)

            children.first().apply {
                assertEquals(expected = "2", actual = id)
                assertEquals(expected = "1", actual = parentId)
            }
        }
    }

    @Test
    fun `document structure does not change when consumer is a paragraph block`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            )
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.consume(
            consumerId = document.first().id,
            consumableId = document.last().id
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.first().apply {
            assertEquals(expected = "1", actual = id)
            assertTrue { parentId.isEmpty() }
            assertTrue { children.isEmpty() }
        }

        document.last().apply {
            assertEquals(expected = "2", actual = id)
            assertTrue { parentId.isEmpty() }
            assertTrue { children.isEmpty() }
        }
    }

    @Test
    fun `document structure does not change, when consumer is a header-one block`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.H1,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            )
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.consume(
            consumerId = document.first().id,
            consumableId = document.last().id
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.first().apply {
            assertEquals(expected = "1", actual = id)
            assertTrue { parentId.isEmpty() }
            assertTrue { children.isEmpty() }
        }

        document.last().apply {
            assertEquals(expected = "2", actual = id)
            assertTrue { parentId.isEmpty() }
            assertTrue { children.isEmpty() }
        }
    }

    @Test
    fun `document structure does not change, when consumer is a header-two block`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.H2,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            )
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.consume(
            consumerId = document.first().id,
            consumableId = document.last().id
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.first().apply {
            assertEquals(expected = "1", actual = id)
            assertTrue { parentId.isEmpty() }
            assertTrue { children.isEmpty() }
        }

        document.last().apply {
            assertEquals(expected = "2", actual = id)
            assertTrue { parentId.isEmpty() }
            assertTrue { children.isEmpty() }
        }
    }

    @Test
    fun `document structure does not change, when consumer is a header-three block`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.H3,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            )
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.consume(
            consumerId = document.first().id,
            consumableId = document.last().id
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.first().apply {
            assertEquals(expected = "1", actual = id)
            assertTrue { parentId.isEmpty() }
            assertTrue { children.isEmpty() }
        }

        document.last().apply {
            assertEquals(expected = "2", actual = id)
            assertTrue { parentId.isEmpty() }
            assertTrue { children.isEmpty() }
        }
    }

    @Test
    fun `document structure does not change, when consumer is a header-four block`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.H4,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            )
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.consume(
            consumerId = document.first().id,
            consumableId = document.last().id
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.first().apply {
            assertEquals(expected = "1", actual = id)
            assertTrue { parentId.isEmpty() }
            assertTrue { children.isEmpty() }
        }

        document.last().apply {
            assertEquals(expected = "2", actual = id)
            assertTrue { parentId.isEmpty() }
            assertTrue { children.isEmpty() }
        }
    }

    @Test
    fun `document structure does not change, when consumer is a code snippet block`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Code,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            )
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.consume(
            consumerId = document.first().id,
            consumableId = document.last().id
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.first().apply {
            assertEquals(expected = "1", actual = id)
            assertTrue { parentId.isEmpty() }
            assertTrue { children.isEmpty() }
        }

        document.last().apply {
            assertEquals(expected = "2", actual = id)
            assertTrue { parentId.isEmpty() }
            assertTrue { children.isEmpty() }
        }
    }

    @Test
    fun `document structure does not change, when consumer is a quote block`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Quote,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            )
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.consume(
            consumerId = document.first().id,
            consumableId = document.last().id
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.first().apply {
            assertEquals(expected = "1", actual = id)
            assertTrue { parentId.isEmpty() }
            assertTrue { children.isEmpty() }
        }

        document.last().apply {
            assertEquals(expected = "2", actual = id)
            assertTrue { parentId.isEmpty() }
            assertTrue { children.isEmpty() }
        }
    }

    @Test
    fun `toggle block should consume a child of other toggle block`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "3",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable
                    )
                )
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "4",
                        parentId = "2",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable
                    )
                )
            )
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        document.consume(
            consumerId = document.first().id,
            consumableId = document.last().children.first().id
        )

        assertEquals(
            expected = 2,
            actual = document.size
        )

        assertEquals(
            expected = 0,
            actual = document.last().children.size
        )

        assertEquals(
            expected = 2,
            actual = document.first().children.size
        )

        assertEquals(
            expected = "3",
            actual = document.first().children.first().id
        )

        assertEquals(
            expected = "4",
            actual = document.first().children.last().id
        )

        assertEquals(
            expected = "1",
            actual = document.first().children.first().parentId
        )

        assertEquals(
            expected = "1",
            actual = document.first().children.last().parentId
        )
    }

    @Test
    fun `should move block at third position at root level to second position at root level`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf()
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            ),
            Block(
                id = "3",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            )
        )

        document.moveAfter(
            previousId = "1",
            targetId = "3"
        )

        assertEquals(3, document.size)

        document.first().apply {
            assertEquals("1", id)
            assertTrue { parentId.isEmpty() }
        }

        document[1].apply {
            assertEquals("3", id)
            assertTrue { parentId.isEmpty() }
        }

        document.last().apply {
            assertEquals("2", id)
            assertTrue { parentId.isEmpty() }
        }
    }

    @Test
    fun `should move toggle child after another child`() {

        val document : Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "2",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable
                    ),
                    Block(
                        id = "3",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable
                    ),
                    Block(
                        id = "4",
                        parentId = "1",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable
                    )
                )
            )
        )

        assertEquals(1, document.size)

        document.moveAfter(
            previousId = "2",
            targetId = "4"
        )

        assertEquals(1, document.size)

        assertEquals(3, document.first().children.size)
        assertEquals("2", document.first().children[0].id)
        assertEquals("4", document.first().children[1].id)
        assertEquals("3", document.first().children[2].id)
        assertTrue { document.first().children[0].parentId == "1" }
        assertTrue { document.first().children[1].parentId == "1" }
        assertTrue { document.first().children[2].parentId == "1" }
    }

    @Test
    fun `should move block at children level to root level`() {

        val document: Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "3",
                        parentId = "2",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable
                    )
                )
            )
        )

        assertEquals(2, document.size)
        assertTrue { document.last().children.first().parentId == "2" }

        document.moveAfter(
            previousId = "1",
            targetId = "3"
        )

        assertEquals(3, document.size)

        assertEquals("1", document[0].id)
        assertEquals("3", document[1].id)
        assertEquals("2", document[2].id)
        assertTrue { document[2].children.isEmpty() }

        assertTrue { document[0].parentId.isEmpty() }
        assertTrue { document[1].parentId.isEmpty() }
        assertTrue { document[2].parentId.isEmpty() }
    }

    @Test
    fun `should move block from root level inside toggle`() {

        val document: Document = mutableListOf(
            Block(
                id = "1",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            ),
            Block(
                id = "2",
                parentId = "",
                contentType = ContentType.Toggle,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                children = mutableListOf(
                    Block(
                        id = "3",
                        parentId = "2",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable
                    ),
                    Block(
                        id = "4",
                        parentId = "2",
                        contentType = ContentType.P,
                        content = Content.Text(
                            text = DataFactory.randomString(),
                            marks = emptyList(),
                            param = ContentParam.empty()
                        ),
                        blockType = BlockType.Editable
                    )
                )
            ),
            Block(
                id = "5",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            ),
            Block(
                id = "6",
                parentId = "",
                contentType = ContentType.P,
                content = Content.Text(
                    text = DataFactory.randomString(),
                    marks = emptyList(),
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable
            )
        )

        assertEquals(4, document.size)

        document.moveAfter(
            previousId = "3",
            targetId = "6"
        )

        assertEquals(3, document.size)

        assertEquals("1", document[0].id)
        assertEquals("2", document[1].id)
        assertEquals("5", document[2].id)

        assertEquals(3, document[1].children.size)

        assertEquals("3", document[1].children[0].id)
        assertEquals("6", document[1].children[1].id)
        assertEquals("4", document[1].children[2].id)

        assertEquals("2", document[1].children[0].parentId)
        assertEquals("2", document[1].children[1].parentId)
        assertEquals("2", document[1].children[2].parentId)
    }
}