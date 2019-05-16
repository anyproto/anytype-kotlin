package com.agileburo.anytype.feature_editor

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import com.agileburo.anytype.feature_editor.domain.*
import com.agileburo.anytype.feature_editor.presentation.mapper.BlockModelMapper
import com.agileburo.anytype.feature_editor.presentation.model.BlockView
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test

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

    val blockView = BlockView(
        id = "2321",
        needClearFocus = false,
        contentType = ContentType.H1,
        content = BlockView.Content.Text(
            text = spannableText,
            param = BlockView.ContentParam(mutableMapOf())
        )
    )

    @Before
    fun init() {
        blockMapper = BlockModelMapper()
    }

    @Test
    fun testShouldBeTheSameMarks() {
        val block = blockMapper.mapToModel(blockView)

        assertEquals(4, block.content.marks.size)
        assertEquals(3, block.content.marks[0].end)
        assertEquals(BOLD, block.content.marks[0].type.name)
        assertEquals(24, block.content.marks[1].end)
        assertEquals(BOLD, block.content.marks[1].type.name)
        assertEquals(12, block.content.marks[2].start)
        assertEquals(ITALIC, block.content.marks[2].type.name)
        assertEquals(33, block.content.marks[3].end)
        assertEquals(STRIKE, block.content.marks[3].type.name)
    }
}