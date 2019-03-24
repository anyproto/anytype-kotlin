package com.agileburo.anytype.feature_editor.presentation

import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.ContentType
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class BlockContentTypeConverterTest {

    lateinit var converter: BlockContentTypeConverter

    val BLOCK = Block(
        id = "65325656", content = "test content",
        contentType = ContentType.H1, parentId = ""
    )

    @Before
    fun setup() {
        converter = BlockContentTypeConverterImpl()
    }

    @Test
    fun `should return set with length 11`() {
        Assert.assertEquals(11, converter.getPermittedTypes(BLOCK.contentType).size)
    }
}
