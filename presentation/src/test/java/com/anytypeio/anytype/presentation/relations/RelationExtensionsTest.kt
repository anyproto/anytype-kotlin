package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class RelationExtensionsTest {

    @Test
    fun `should return empty list of filters`() {

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.OBJECT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = true,
            objectTypes = listOf(),
            defaultValue = ""
        )

        val result = relation.searchObjectsFilter()

        assertEquals(0, result.size)
    }

    @Test
    fun `should return list of one filter with three values`() {

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.OBJECT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = true,
            objectTypes = listOf("_image", "_video", "_file"),
            defaultValue = ""
        )

        val result = relation.searchObjectsFilter()

        val expected = listOf(
            DVFilter(
                relation = ObjectSetConfig.TYPE_KEY,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = Block.Content.DataView.Filter.Condition.IN,
                value = listOf("_image", "_video", "_file")
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should add is hidden filter`() {

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.OBJECT,
            source = Relation.Source.ACCOUNT,
            isHidden = false,
            isReadOnly = false,
            isMulti = true,
            objectTypes = listOf("_image", "_video", "_file"),
            defaultValue = ""
        )

        val result = relation
            .searchObjectsFilter()
            .addIsHiddenFilter()

        val expected = listOf(
            DVFilter(
                relation = ObjectSetConfig.TYPE_KEY,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = Block.Content.DataView.Filter.Condition.IN,
                value = listOf("_image", "_video", "_file")
            ),
            DVFilter(
                relation = Relations.IS_HIDDEN,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = Block.Content.DataView.Filter.Condition.NOT_EQUAL,
                value = true
            )
        )

        assertEquals(expected, result)
    }
}