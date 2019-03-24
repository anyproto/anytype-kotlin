package com.agileburo.anytype.feature_editor.presentation

import com.agileburo.anytype.feature_editor.domain.ContentType
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class BlockContentTypeConverterTest {

    lateinit var converter: BlockContentTypeConverter

    private val CONTENT_TYPE = ContentType.Toggle
    private val CONTENT_TYPE_OTHER = ContentType.Quote
    private val CONTENT_TYPE_SET_SIZE = ContentType::class::nestedClasses.get().size

    @Before
    fun setup() {
        converter = BlockContentTypeConverterImpl()
    }

    @Test
    fun `should return set of size ContentType minus 1`() {
        Assert.assertEquals(
            CONTENT_TYPE_SET_SIZE - 1,
            converter.getPermittedTypes(CONTENT_TYPE).size
        )
    }

    @Test
    fun `should return set without initial content type`() {
        val result = converter.getPermittedTypes(CONTENT_TYPE)
        Assert.assertNotEquals(CONTENT_TYPE, CONTENT_TYPE_OTHER)
        Assert.assertEquals(false, result.contains(CONTENT_TYPE))
        Assert.assertEquals(true, result.contains(CONTENT_TYPE_OTHER))
    }
}
