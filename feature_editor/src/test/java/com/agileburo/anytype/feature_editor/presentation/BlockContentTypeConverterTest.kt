package com.agileburo.anytype.feature_editor.presentation

import com.agileburo.anytype.feature_editor.domain.ContentType
import com.agileburo.anytype.feature_editor.factory.BlockFactory
import com.agileburo.anytype.feature_editor.presentation.converter.BlockContentTypeConverter
import com.agileburo.anytype.feature_editor.presentation.converter.BlockContentTypeConverterImpl
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class BlockContentTypeConverterTest {

    lateinit var converter: BlockContentTypeConverter

    private val CONTENT_TYPE = ContentType.Toggle
    private val CONTENT_TYPE_SET_SIZE = ContentType::class::nestedClasses.get().size

    @Before
    fun setup() {
        converter = BlockContentTypeConverterImpl()
    }

    @Test
    fun `should return set of size ContentType`() {
        Assert.assertEquals(
            CONTENT_TYPE_SET_SIZE,
            converter.getPermittedTypes(CONTENT_TYPE).size
        )
    }
}
