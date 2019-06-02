package com.agileburo.anytype.feature_editor

import com.agileburo.anytype.feature_editor.data.converter.toContentType
import com.agileburo.anytype.feature_editor.data.converter.toNumericalCode
import com.agileburo.anytype.feature_editor.domain.ContentType
import com.agileburo.anytype.feature_editor.factory.ContentTypeFactory
import org.junit.Test

class ContentTypeConverterTest {

    @Test
    fun `we get the same result when we convert a content type to a numerical code and vice versa`() {

        val source = ContentTypeFactory.values()

        assert(source.size == ContentType::class.nestedClasses.size)

        val numerical = source.map { type -> type.toNumericalCode() }

        val typed = numerical.map { num -> num.toContentType() }

        assert(typed == source)
    }

}
