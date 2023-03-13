package com.anytypeio.anytype.presentation.search

import com.anytypeio.anytype.presentation.MockObjectTypes
import com.anytypeio.anytype.presentation.mapper.toObjectTypeView
import com.anytypeio.anytype.presentation.objects.getObjectTypeViewsForSBPage
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObjectSearchExtensionsTest {

    @Test
    fun `should return sorted views with set and collection, without bookmark and page`() {

        val searchObjectsResponse = MockObjectTypes.objectTypeList
        val excludeTypes = listOf(MockObjectTypes.objectTypePage.id)
        val selectedTypes = listOf(MockObjectTypes.objectTypeNote.id)

        val expected = listOf(
            MockObjectTypes.objectTypeNote,
            MockObjectTypes.objectTypeCollection,
            MockObjectTypes.objectTypeTask,
            MockObjectTypes.objectTypeCustom,
            MockObjectTypes.objectTypeHuman,
            MockObjectTypes.objectTypeSet
        )
            .map { it.toObjectTypeView(selectedSources = selectedTypes) }

        val actual = searchObjectsResponse.getObjectTypeViewsForSBPage(
            isWithCollection = true,
            isWithBookmark = false,
            excludeTypes = excludeTypes,
            selectedTypes = selectedTypes
        )

        assertEquals(
            expected = expected,
            actual = actual
        )

        actual.forEach {
            if (it.id == MockObjectTypes.objectTypeNote.id) {
                assertTrue { it.isSelected }
            } else {
                assertFalse { it.isSelected }
            }
        }
    }

    @Test
    fun `should return sorted views with bookmark, without set and note`() {

        val searchObjectsResponse = MockObjectTypes.objectTypeList
        val excludeTypes = listOf(MockObjectTypes.objectTypeNote.id)
        val selectedTypes = listOf(MockObjectTypes.objectTypePage.id)

        val expected = listOf(
            MockObjectTypes.objectTypePage,
            MockObjectTypes.objectTypeTask,
            MockObjectTypes.objectTypeBookmark,
            MockObjectTypes.objectTypeCustom,
            MockObjectTypes.objectTypeHuman,
        )
            .map { it.toObjectTypeView(selectedSources = selectedTypes) }

        val actual = searchObjectsResponse.getObjectTypeViewsForSBPage(
            isWithCollection = false,
            isWithBookmark = true,
            excludeTypes = excludeTypes,
            selectedTypes = selectedTypes
        )

        assertEquals(
            expected = expected,
            actual = actual
        )

        actual.forEach {
            if (it.id == MockObjectTypes.objectTypePage.id) {
                assertTrue { it.isSelected }
            } else {
                assertFalse { it.isSelected }
            }
        }
    }
}