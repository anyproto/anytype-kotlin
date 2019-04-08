package com.agileburo.anytype.feature_editor.data

import com.agileburo.anytype.feature_editor.domain.Mark
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 03.04.2019.
 */
class MarkConverterImplTest {

    val MODEL_ITALIC = MarkModel(type = "i", start = 24032, end = 35226)
    val MODEL_BOLD = MarkModel(type = "b", start = 2402121, end = 111356)
    val MODEL_HYPERTEXT = MarkModel(type = "a", start = 111, end = 2222, param = "ya1.ru")
    val MODEL_UNDEFINE = MarkModel(type = "nope", start = 111, end = 2222, param = "ya1.ru")


    lateinit var markConverter: MarkConverter

    @Before
    fun setUp() {
        markConverter = MarkConverterImpl()
    }

    @Test
    fun `should return domain model type italic`() {
        val result = markConverter.modelToDomain(MODEL_ITALIC)

        assertEquals(MODEL_ITALIC.start, result.start)
        assertEquals(MODEL_ITALIC.end, result.end)
        assertEquals(Mark.MarkType.ITALIC, result.type)
    }

    @Test
    fun `should return domain model type bold`() {
        val result = markConverter.modelToDomain(MODEL_BOLD)

        assertEquals(MODEL_BOLD.start, result.start)
        assertEquals(Mark.MarkType.BOLD, result.type)
    }

    @Test
    fun `should return domain model type hypertext`() {
        val result = markConverter.modelToDomain(MODEL_HYPERTEXT)

        assertEquals(MODEL_HYPERTEXT.start, result.start)
        assertEquals(MODEL_HYPERTEXT.end, result.end)
        assertEquals(Mark.MarkType.HYPERTEXT, result.type)
        assertEquals(MODEL_HYPERTEXT.param, result.param)
    }

    @Test
    fun `should return domain model type undefine`() {
        val resultItalic = markConverter.modelToDomain(MODEL_UNDEFINE)

        assertEquals(MODEL_UNDEFINE.start, resultItalic.start)
        assertEquals(Mark.MarkType.UNDEFINED, resultItalic.type)
    }
}