package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RelationExtensionsTest {

    @Test
    fun `should filter recommended by object relations`() = runTest {
        // Given
        val mockFactory = MockTypicalDocumentFactory

        // relations in StoreOfRelations
        val relations =
            List(2) { mockFactory.relationObject(name = "relation-$it") } +
                    List(2) { mockFactory.relationObject(name = "recommendedRelation-$it") }

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
                key = relations[3].key,
                format = RelationFormat.CHECKBOX
            ),
        )

        val recommendedRelations = listOf(relations[2].id, relations[3].id)

        val storeOfRelations = DefaultStoreOfRelations().apply { merge(relations) }
        advanceUntilIdle()

        // When
        val result = getNotIncludedRecommendedRelations(
            relationLinks = relationLinks,
            recommendedRelations = recommendedRelations,
            storeOfRelations = storeOfRelations
        )

        // Then
        val expected = listOf(relations[2])
        kotlin.test.assertEquals(expected, result)
    }
}