package com.agileburo.anytype.feature_editor

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import androidx.test.runner.AndroidJUnit4
import com.agileburo.anytype.feature_editor.domain.*
import com.agileburo.anytype.feature_editor.factory.AndroidDataFactory
import com.agileburo.anytype.feature_editor.presentation.mapper.BlockModelMapper
import com.agileburo.anytype.feature_editor.presentation.model.BlockView
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-05-15.
 */
class BlockModelMapperTest {

    lateinit var blockMapper: BlockModelMapper

    val BOLD = "BOLD"
    val ITALIC = "ITALIC"
    val STRIKE = "STRIKE_THROUGH"

    val spannableText = SpannableString("This can be simply solved by using Spannable String").apply {
        setSpan(StyleSpan(Typeface.BOLD), 0, 3, 0)
        setSpan(StyleSpan(Typeface.BOLD), 19, 24, 0)
        setSpan(StyleSpan(Typeface.ITALIC), 12, 17, 0)
        setSpan(StrikethroughSpan(), 27, 33, 0)
    }

    val blockView = BlockView.ParagraphView(
        id = "2321",
        text = spannableText
    )

    @Before
    fun init() {
        blockMapper = BlockModelMapper()
    }

    @Test
    fun testShouldBeTheSameMarks() {
        val block = blockMapper.mapToModel(blockView)
        val content = block.content as Content.Text

        assertEquals(4, content.marks.size)
        assertEquals(3, content.marks[0].end)
        assertEquals(BOLD, content.marks[0].type.name)
        assertEquals(24, content.marks[1].end)
        assertEquals(BOLD, content.marks[1].type.name)
        assertEquals(12, content.marks[2].start)
        assertEquals(ITALIC, content.marks[2].type.name)
        assertEquals(33, content.marks[3].end)
        assertEquals(STRIKE, content.marks[3].type.name)
    }

    @Test
    fun paragraphViewConvertedCorrectly() {

        val view = BlockView.ParagraphView(
            id = AndroidDataFactory.randomString(),
            text = SpannableString(AndroidDataFactory.randomString())
        )

        val block = blockMapper.mapToModel(view)
        val content = block.content as Content.Text

        assertEquals(view.text.toString(), content.text)
        assertEquals(view.id, view.id)
        assertEquals(block.blockType, BlockType.Editable)
        assertEquals(block.contentType, ContentType.P)
    }

    @Test
    fun bulletViewConvertedCorrectly() {

        val view = BlockView.BulletView(
            id = AndroidDataFactory.randomString(),
            text = SpannableString(AndroidDataFactory.randomString())
        )

        val block = blockMapper.mapToModel(view)
        val content = block.content as Content.Text

        assertEquals(view.text.toString(), content.text)
        assertEquals(view.id, view.id)
        assertEquals(block.blockType, BlockType.Editable)
        assertEquals(block.contentType, ContentType.UL)
    }

    @Test
    fun quoteViewConvertedCorrectly() {

        val view = BlockView.QuoteView(
            id = AndroidDataFactory.randomString(),
            text = SpannableString(AndroidDataFactory.randomString())
        )

        val block = blockMapper.mapToModel(view)
        val content = block.content as Content.Text

        assertEquals(view.text.toString(), content.text)
        assertEquals(view.id, view.id)
        assertEquals(block.blockType, BlockType.Editable)
        assertEquals(block.contentType, ContentType.Quote)
    }

    @Test
    fun checkboxViewConvertedCorrectly() {

        val view = BlockView.CheckboxView(
            id = AndroidDataFactory.randomString(),
            text = SpannableString(AndroidDataFactory.randomString()),
            isChecked = AndroidDataFactory.randomBoolean()
        )

        val block = blockMapper.mapToModel(view)
        val content = block.content as Content.Text

        assertEquals(view.text.toString(), content.text)
        assertEquals(view.id, view.id)
        assertEquals(block.blockType, BlockType.Editable)
        assertEquals(block.contentType, ContentType.Check)
        assertEquals(content.param.checked, view.isChecked)
    }

    @Test
    fun numberedListItemConvertedCorrectly() {

        val view = BlockView.NumberListItemView(
            id = AndroidDataFactory.randomString(),
            text = SpannableString(AndroidDataFactory.randomString()),
            number = AndroidDataFactory.randomInt()
        )

        val block = blockMapper.mapToModel(view)
        val content = block.content as Content.Text

        assertEquals(view.text.toString(), content.text)
        assertEquals(view.id, view.id)
        assertEquals(block.blockType, BlockType.Editable)
        assertEquals(block.contentType, ContentType.NumberedList)
        assertEquals(content.param.number, view.number)
    }

    @Test
    fun codeSnippetViewConvertedCorrectly() {

        val view = BlockView.CodeSnippetView(
            id = AndroidDataFactory.randomString(),
            text = SpannableString(AndroidDataFactory.randomString())
        )

        val block = blockMapper.mapToModel(view)
        val content = block.content as Content.Text

        assertEquals(view.text.toString(), content.text)
        assertEquals(view.id, view.id)
        assertEquals(block.blockType, BlockType.Editable)
        assertEquals(block.contentType, ContentType.Code)
    }

    @Test
    fun headerOneViewConvertedCorrectly() {

        val headerOneView = BlockView.HeaderView(
            id = AndroidDataFactory.randomString(),
            text = SpannableString(AndroidDataFactory.randomString()),
            type = BlockView.HeaderView.HeaderType.ONE
        )


        val headerOneBlock = blockMapper.mapToModel(headerOneView)
        val content = headerOneBlock.content as Content.Text


        assertEquals(headerOneView.text.toString(), content.text)
        assertEquals(headerOneView.id, headerOneView.id)
        assertEquals(headerOneBlock.blockType, BlockType.Editable)
        assertEquals(headerOneBlock.contentType, ContentType.H1)

    }

    @Test
    fun headerTwoViewConvertedCorrectly() {

        val headerTwoView = BlockView.HeaderView(
            id = AndroidDataFactory.randomString(),
            text = SpannableString(AndroidDataFactory.randomString()),
            type = BlockView.HeaderView.HeaderType.TWO
        )

        val headerTwoBlock = blockMapper.mapToModel(headerTwoView)
        val content = headerTwoBlock.content as Content.Text


        assertEquals(headerTwoView.text.toString(), content.text)
        assertEquals(headerTwoView.id, headerTwoView.id)
        assertEquals(headerTwoBlock.blockType, BlockType.Editable)
        assertEquals(headerTwoBlock.contentType, ContentType.H2)

    }

    @Test
    fun headerThreeViewConvertedCorrectly() {

        val headerThreeView = BlockView.HeaderView(
            id = AndroidDataFactory.randomString(),
            text = SpannableString(AndroidDataFactory.randomString()),
            type = BlockView.HeaderView.HeaderType.THREE
        )

        val headerThreeBlock = blockMapper.mapToModel(headerThreeView)
        val content = headerThreeBlock.content as Content.Text


        assertEquals(headerThreeView.text.toString(), content.text)
        assertEquals(headerThreeView.id, headerThreeView.id)
        assertEquals(headerThreeBlock.blockType, BlockType.Editable)
        assertEquals(headerThreeBlock.contentType, ContentType.H3)

    }

    @Test
    fun headerFourViewConvertedCorrectly() {

        val headerFourView = BlockView.HeaderView(
            id = AndroidDataFactory.randomString(),
            text = SpannableString(AndroidDataFactory.randomString()),
            type = BlockView.HeaderView.HeaderType.FOUR
        )

        val headerFourBlock = blockMapper.mapToModel(headerFourView)
        val content = headerFourBlock.content as Content.Text


        assertEquals(headerFourView.text.toString(), content.text)
        assertEquals(headerFourView.id, headerFourView.id)
        assertEquals(headerFourBlock.blockType, BlockType.Editable)
        assertEquals(headerFourBlock.contentType, ContentType.H4)

    }


}