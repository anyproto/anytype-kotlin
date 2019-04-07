package com.agileburo.anytype.feature_editor

import com.agileburo.anytype.feature_editor.domain.ContentParam
import com.agileburo.anytype.feature_editor.domain.ContentType
import com.agileburo.anytype.feature_editor.factory.BlockFactory
import com.agileburo.anytype.feature_editor.presentation.converter.BlockContentTypeConverterImpl
import junit.framework.Assert.assertEquals
import org.junit.Test

class NumberedListMechanicsTest {

    private val converter by lazy {
        BlockContentTypeConverterImpl()
    }

    @Test
    fun `when content type is the same, converter should return the same list`() {

        val blocks = listOf(
            BlockFactory.makeBlock(contentType = ContentType.P),
            BlockFactory.makeBlock(contentType = ContentType.P)
        )

        val targetType = ContentType.P

        val target = blocks.first()

        val result = converter.convert(
            blocks = blocks,
            target = target,
            targetType = targetType
        )

        assert(result == blocks)
    }

    @Test
    fun `if previous block is not an item of numbered list and we create numbered list item, then its number param = 1`() {

        val blocks = listOf(
            BlockFactory.makeBlock(contentType = ContentType.P),
            BlockFactory.makeBlock(contentType = ContentType.P)
        )

        val targetType = ContentType.NumberedList

        val target = blocks.last()

        val result = converter.convert(
            blocks = blocks,
            target = target,
            targetType = targetType
        )

        result.apply {
            assert(size == 2)
            assert(first() == blocks.first())
            assert(last().contentType == ContentType.NumberedList)
            assert(last().content.param.number == 1)
        }

    }

    @Test
    fun `if previous block is an item of numbered list, then the next numbered list item should have number incremented`() {

        val blocks = listOf(
            BlockFactory.makeBlock(
                contentType = ContentType.NumberedList,
                contentParam = ContentParam.numberedList(number = 1)
            ),
            BlockFactory.makeBlock(contentType = ContentType.P)
        )

        val targetType = ContentType.NumberedList

        val target = blocks.last()

        val result = converter.convert(
            blocks = blocks,
            target = target,
            targetType = targetType
        )

        result.apply {
            assert(size == 2)
            assert(first() == blocks.first())
            assert(last().contentType == ContentType.NumberedList)
            assert(last().content.param.number == 2)
        }

    }

    @Test
    fun `the first block and the third one are items of numbered list, if we convert second block to numbered list, we should have a correct number sequence`() {

        val blocks = listOf(
            BlockFactory.makeBlock(
                contentType = ContentType.NumberedList,
                contentParam = ContentParam.numberedList(number = 1)
            ),
            BlockFactory.makeBlock(contentType = ContentType.P),
            BlockFactory.makeBlock(
                contentType = ContentType.NumberedList,
                contentParam = ContentParam.numberedList(number = 1)
            )
        )

        val targetType = ContentType.NumberedList

        val target = blocks[1]

        val result = converter.convert(
            blocks = blocks,
            target = target,
            targetType = targetType
        )

        result.apply {
            assert(size == 3)
            assert(get(0).contentType == ContentType.NumberedList)
            assert(get(0).content.param.number == 1)
            assert(get(1).contentType == ContentType.NumberedList)
            assert(get(1).content.param.number == 2)
            assert(get(2).contentType == ContentType.NumberedList)
            assert(get(2).content.param.number == 3)
        }
    }

    @Test
    fun `when we have three items of numbered list, when we convert item in the middle, numbers should be also updated`() {

        val blocks = listOf(
            BlockFactory.makeBlock(
                contentParam = ContentParam.numberedList(1),
                contentType = ContentType.NumberedList
            ),
            BlockFactory.makeBlock(
                contentParam = ContentParam.numberedList(2),
                contentType = ContentType.NumberedList
            ),
            BlockFactory.makeBlock(
                contentParam = ContentParam.numberedList(3),
                contentType = ContentType.NumberedList
            )
        )

        val targetType = ContentType.P

        val target = blocks[1]

        val result = converter.convert(
            blocks = blocks,
            target = target,
            targetType = targetType
        )

        result.apply {
            assert(size == 3)
            assert(get(0).contentType == ContentType.NumberedList)
            assert(get(0).content.param.number == 1)
            assert(get(1).contentType == ContentType.P)
            assert(get(2).contentType == ContentType.NumberedList)
            assert(get(2).content.param.number == 1)
        }

    }

    @Test
    fun `should return correctly ordered numbered list`() {

        val blocks = listOf(
            BlockFactory.makeBlock(
                contentType = ContentType.P
            ),
            BlockFactory.makeBlock(
                contentParam = ContentParam.numberedList(1),
                contentType = ContentType.NumberedList
            ),
            BlockFactory.makeBlock(
                contentType = ContentType.P
            ),
            BlockFactory.makeBlock(
                contentParam = ContentParam.numberedList(1),
                contentType = ContentType.NumberedList
            )
        )

        val targetType = ContentType.NumberedList

        val target = blocks[2]

        val result = converter.convert(
            blocks = blocks,
            target = target,
            targetType = targetType
        )

        result.apply {
            assertEquals(size, 4)
            assertEquals(get(0).contentType, ContentType.P)
            assertEquals(get(0).content.param.number, 0)
            assertEquals(get(1).contentType, ContentType.NumberedList)
            assertEquals(get(1).content.param.number, 1)
            assertEquals(get(2).contentType, ContentType.NumberedList)
            assertEquals(get(2).content.param.number, 2)
            assertEquals(get(3).contentType, ContentType.NumberedList)
            assertEquals(get(3).content.param.number, 3)
        }

    }

}