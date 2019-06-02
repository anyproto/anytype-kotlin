package com.agileburo.anytype.feature_editor

import com.agileburo.anytype.feature_editor.data.converter.toBlockType
import com.agileburo.anytype.feature_editor.data.converter.toNumericalCode
import com.agileburo.anytype.feature_editor.domain.BlockType
import com.agileburo.anytype.feature_editor.factory.BlockTypeFactory
import org.junit.Test


class BlockTypeConverterTest {

    @Test
    fun `we get the same result when we convert a block type to a numerical code and vice versa`() {

        val source = BlockTypeFactory.values()

        assert(source.size == BlockType::class.nestedClasses.size)

        val numerical = source.map { type -> type.toNumericalCode() }

        val typed = numerical.map { num -> num.toBlockType() }

        assert(typed == source)
    }
}
