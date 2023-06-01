package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObjectWrapperExtensionTest {

    @Test
    fun `should filter by system relations and remove duplicates`() = runTest {
        // Given
        val mockFactory = MockTypicalDocumentFactory

        // relations in StoreOfRelations
        val relations = List(2) { mockFactory.relationObject(name = "sysRelation-$it") } +
                List(4) { mockFactory.relationObject(name = "relation-$it") } +
                List(3) { mockFactory.relationObject(name = "recommendedRelation-$it") }

        // Object has 6 relationLinks( 2 system + 4 other)
        val relationLinks = listOf(
            RelationLink(
                key = relations[0].key,
                format = RelationFormat.OBJECT
            ),
            RelationLink(
                key = relations[1].key,
                format = RelationFormat.NUMBER
            ),
            RelationLink(
                key = relations[2].key,
                format = RelationFormat.LONG_TEXT
            ),
            RelationLink(
                key = relations[3].key,
                format = RelationFormat.CHECKBOX
            ),
            RelationLink(
                key = relations[4].key,
                format = RelationFormat.FILE
            ),
            RelationLink(
                key = relations[5].key,
                format = RelationFormat.SHORT_TEXT
            ),
            RelationLink(
                key = relations[6].key,
                format = RelationFormat.SHORT_TEXT
            )
        )
        val systemRelations = listOf(relations[0].key, relations[1].key)

        val recommendedRelations = listOf(relations[6].id, relations[7].id, relations[8].id)

        val storeOfRelations = DefaultStoreOfRelations().apply { merge(relations) }
        advanceUntilIdle()

        // When
        val objectRelations = getObjectIncludedAndRecommendedRelations(
            relationLinks = relationLinks,
            systemRelations = systemRelations,
            recommendedRelations = recommendedRelations,
            storeOfRelations = storeOfRelations
        )

        // Then
        val expected = listOf(
            relations[2],
            relations[3],
            relations[4],
            relations[5],
            relations[6],
            relations[7],
            relations[8]
        )
        assertEquals(expected, objectRelations)
    }
}