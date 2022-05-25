package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.presentation.relations.model.RelationView
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
                relationKey = ObjectSetConfig.TYPE_KEY,
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
                relationKey = ObjectSetConfig.TYPE_KEY,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = Block.Content.DataView.Filter.Condition.IN,
                value = listOf("_image", "_video", "_file")
            ),
            DVFilter(
                relationKey = Relations.IS_HIDDEN,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = Block.Content.DataView.Filter.Condition.NOT_EQUAL,
                value = true
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return filtered relation views`() {

        val relation = Relation(
            key = "type",
            name = "Object type",
            format = Relation.Format.OBJECT,
            source = Relation.Source.DERIVED,
            isHidden = true,
            isReadOnly = true,
            isMulti = false,
            objectTypes = listOf(ObjectType.OBJECT_TYPE_URL)
        )

        val relation2 = Relation(
            key = "events",
            name = "Events",
            format = Relation.Format.OBJECT,
            source = Relation.Source.DERIVED
        )

        val relation3 = Relation(
            key = "70L0KG2q",
            name = "Custom relation",
            format = Relation.Format.NUMBER,
            source = Relation.Source.ACCOUNT
        )

        val relations = listOf(relation, relation2, relation3)

        val result = relations.toNotHiddenRelationViews()

        val expected = listOf(
            RelationView.Existing(
                id = relation2.key,
                name = relation2.name,
                format = relation2.format
            ),
            RelationView.Existing(
                id = relation3.key,
                name = relation3.name,
                format = relation3.format
            )
        )

        assertEquals(expected, result)
    }
}