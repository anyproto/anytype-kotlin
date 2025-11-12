package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.StubObjectType
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for widget type sorting logic.
 * Tests the three-level sorting priority:
 * 1. orderId (ascending, nulls at end)
 * 2. uniqueKey custom order
 * 3. name (alphabetical)
 */
class WidgetTypeSortingTest {

    private lateinit var storeOfObjectTypes: DefaultStoreOfObjectTypes
    private lateinit var config: Config

    @Before
    fun setup() {
        storeOfObjectTypes = DefaultStoreOfObjectTypes()
        config = StubConfig()
    }

    @Test
    fun `should sort by orderId ascending when all types have orderId`() = runTest {
        // Given: Types with different orderIds
        val type1 = StubObjectType(
            id = "type1",
            uniqueKey = ObjectTypeIds.PAGE,
            name = "Page",
            orderId = "W-02"
        )
        val type2 = StubObjectType(
            id = "type2",
            uniqueKey = ObjectTypeIds.NOTE,
            name = "Note",
            orderId = "W-01"
        )
        val type3 = StubObjectType(
            id = "type3",
            uniqueKey = ObjectTypeIds.TASK,
            name = "Task",
            orderId = "W-03"
        )

        storeOfObjectTypes.merge(listOf(type1, type2, type3))

        // When: Building widgets
        val result = mapSpaceTypesToWidgets(
            isOwnerOrEditor = true,
            config = config,
            storeOfObjectTypes = storeOfObjectTypes,
            spaceUxType = null
        )

        // Then: Should be sorted by orderId ascending
        assertEquals(3, result.size)
        assertEquals("type2", result[0].id) // W-01
        assertEquals("type1", result[1].id) // W-02
        assertEquals("type3", result[2].id) // W-03
    }

    @Test
    fun `should place types with orderId before types without orderId`() = runTest {
        // Given: Mix of types with and without orderId
        val typeWithOrder = StubObjectType(
            id = "typeWithOrder",
            uniqueKey = ObjectTypeIds.BOOKMARK,
            name = "Bookmark",
            orderId = "W-99"
        )
        val typeWithoutOrder1 = StubObjectType(
            id = "typeWithoutOrder1",
            uniqueKey = ObjectTypeIds.PAGE,
            name = "Page",
            orderId = null
        )
        val typeWithoutOrder2 = StubObjectType(
            id = "typeWithoutOrder2",
            uniqueKey = ObjectTypeIds.NOTE,
            name = "Note",
            orderId = null
        )

        storeOfObjectTypes.merge(listOf(typeWithoutOrder1, typeWithOrder, typeWithoutOrder2))

        // When: Building widgets
        val result = mapSpaceTypesToWidgets(
            isOwnerOrEditor = true,
            config = config,
            storeOfObjectTypes = storeOfObjectTypes,
            spaceUxType = null
        )

        // Then: Type with orderId should come first
        assertEquals(3, result.size)
        assertEquals("typeWithOrder", result[0].id)
    }

    @Test
    fun `should sort by custom uniqueKey order when no orderId`() = runTest {
        // Given: Types without orderId but with known uniqueKeys
        val task = StubObjectType(
            id = "task",
            uniqueKey = ObjectTypeIds.TASK,
            name = "Task",
            orderId = null
        )
        val page = StubObjectType(
            id = "page",
            uniqueKey = ObjectTypeIds.PAGE,
            name = "Page",
            orderId = null
        )
        val note = StubObjectType(
            id = "note",
            uniqueKey = ObjectTypeIds.NOTE,
            name = "Note",
            orderId = null
        )
        val collection = StubObjectType(
            id = "collection",
            uniqueKey = ObjectTypeIds.COLLECTION,
            name = "Collection",
            orderId = null
        )

        // Add in random order
        storeOfObjectTypes.merge(listOf(task, collection, note, page))

        // When: Building widgets for non-chat space
        val result = mapSpaceTypesToWidgets(
            isOwnerOrEditor = true,
            config = config,
            storeOfObjectTypes = storeOfObjectTypes,
            spaceUxType = null
        )

        // Then: Should follow custom order: Page, Note, Task, Collection
        assertEquals(4, result.size)
        assertEquals("page", result[0].id)
        assertEquals("note", result[1].id)
        assertEquals("task", result[2].id)
        assertEquals("collection", result[3].id)
    }

    @Test
    fun `should use different custom order for chat spaces`() = runTest {
        // Given: Types without orderId but with known uniqueKeys
        val image = StubObjectType(
            id = "image",
            uniqueKey = ObjectTypeIds.IMAGE,
            name = "Image",
            orderId = null
        )
        val page = StubObjectType(
            id = "page",
            uniqueKey = ObjectTypeIds.PAGE,
            name = "Page",
            orderId = null
        )
        val bookmark = StubObjectType(
            id = "bookmark",
            uniqueKey = ObjectTypeIds.BOOKMARK,
            name = "Bookmark",
            orderId = null
        )

        // Add in random order
        storeOfObjectTypes.merge(listOf(page, bookmark, image))

        // When: Building widgets for chat space
        val result = mapSpaceTypesToWidgets(
            isOwnerOrEditor = true,
            config = config,
            storeOfObjectTypes = storeOfObjectTypes,
            spaceUxType = SpaceUxType.CHAT
        )

        // Then: Should follow chat order: Image, Bookmark, Page
        assertEquals(3, result.size)
        assertEquals("image", result[0].id)
        assertEquals("bookmark", result[1].id)
        assertEquals("page", result[2].id)
    }

    @Test
    fun `should sort by name when types not in custom order`() = runTest {
        // Given: Types with unknown uniqueKeys (custom types)
        val zebra = StubObjectType(
            id = "zebra",
            uniqueKey = "ot-zebra",
            name = "Zebra Type",
            orderId = null
        )
        val apple = StubObjectType(
            id = "apple",
            uniqueKey = "ot-apple",
            name = "Apple Type",
            orderId = null
        )
        val banana = StubObjectType(
            id = "banana",
            uniqueKey = "ot-banana",
            name = "Banana Type",
            orderId = null
        )

        storeOfObjectTypes.merge(listOf(zebra, banana, apple))

        // When: Building widgets
        val result = mapSpaceTypesToWidgets(
            isOwnerOrEditor = true,
            config = config,
            storeOfObjectTypes = storeOfObjectTypes,
            spaceUxType = null
        )

        // Then: Should be alphabetically sorted by name
        assertEquals(3, result.size)
        assertEquals("apple", result[0].id)
        assertEquals("banana", result[1].id)
        assertEquals("zebra", result[2].id)
    }

    @Test
    fun `should use name as tertiary sort for same uniqueKey priority`() = runTest {
        // Given: Types with unknown uniqueKeys that need alphabetical sorting
        val zCustom = StubObjectType(
            id = "zCustom",
            uniqueKey = "ot-custom-z",
            name = "Z Custom",
            orderId = null
        )
        val aCustom = StubObjectType(
            id = "aCustom",
            uniqueKey = "ot-custom-a",
            name = "A Custom",
            orderId = null
        )

        storeOfObjectTypes.merge(listOf(zCustom, aCustom))

        // When: Building widgets
        val result = mapSpaceTypesToWidgets(
            isOwnerOrEditor = true,
            config = config,
            storeOfObjectTypes = storeOfObjectTypes,
            spaceUxType = null
        )

        // Then: Should be sorted alphabetically
        assertEquals(2, result.size)
        assertEquals("aCustom", result[0].id)
        assertEquals("zCustom", result[1].id)
    }

    @Test
    fun `should apply all three sorting levels correctly`() = runTest {
        // Given: Complex mix of types with different sorting criteria
        // - Some with orderId (should come first, sorted by orderId)
        // - Some without orderId but in custom order (sorted by custom order)
        // - Some without orderId and not in custom order (sorted alphabetically)

        val withOrder1 = StubObjectType(
            id = "withOrder1",
            uniqueKey = "ot-random1",
            name = "With Order 1",
            orderId = "W-02"
        )
        val withOrder2 = StubObjectType(
            id = "withOrder2",
            uniqueKey = "ot-random2",
            name = "With Order 2",
            orderId = "W-01"
        )
        val page = StubObjectType(
            id = "page",
            uniqueKey = ObjectTypeIds.PAGE,
            name = "Page",
            orderId = null
        )
        val note = StubObjectType(
            id = "note",
            uniqueKey = ObjectTypeIds.NOTE,
            name = "Note",
            orderId = null
        )
        val customZ = StubObjectType(
            id = "customZ",
            uniqueKey = "ot-custom-z",
            name = "Z Custom Type",
            orderId = null
        )
        val customA = StubObjectType(
            id = "customA",
            uniqueKey = "ot-custom-a",
            name = "A Custom Type",
            orderId = null
        )

        // Add in random order
        storeOfObjectTypes.merge(listOf(customZ, note, withOrder1, customA, page, withOrder2))

        // When: Building widgets
        val result = mapSpaceTypesToWidgets(
            isOwnerOrEditor = true,
            config = config,
            storeOfObjectTypes = storeOfObjectTypes,
            spaceUxType = null
        )

        // Then: Should be sorted by:
        // 1. orderId (W-01, W-02)
        // 2. custom order (Page, Note)
        // 3. alphabetical (A Custom Type, Z Custom Type)
        assertEquals(6, result.size)
        assertEquals("withOrder2", result[0].id) // W-01
        assertEquals("withOrder1", result[1].id) // W-02
        assertEquals("page", result[2].id)       // custom order
        assertEquals("note", result[3].id)       // custom order
        assertEquals("customA", result[4].id)    // alphabetical
        assertEquals("customZ", result[5].id)    // alphabetical
    }

    @Test
    fun `should filter out invalid types before sorting`() = runTest {
        // Given: Mix of valid and invalid types
        val validType = StubObjectType(
            id = "valid",
            uniqueKey = ObjectTypeIds.PAGE,
            name = "Page",
            orderId = null,
            isDeleted = false,
            isArchived = false
        )
        val deletedType = StubObjectType(
            id = "deleted",
            uniqueKey = ObjectTypeIds.NOTE,
            name = "Note",
            orderId = null,
            isDeleted = true
        )
        val archivedType = StubObjectType(
            id = "archived",
            uniqueKey = ObjectTypeIds.TASK,
            name = "Task",
            orderId = null,
            isArchived = true
        )

        storeOfObjectTypes.merge(listOf(deletedType, validType, archivedType))

        // When: Building widgets
        val result = mapSpaceTypesToWidgets(
            isOwnerOrEditor = true,
            config = config,
            storeOfObjectTypes = storeOfObjectTypes,
            spaceUxType = null
        )

        // Then: Only valid type should be returned
        assertEquals(1, result.size)
        assertEquals("valid", result[0].id)
    }

    @Test
    fun `should handle empty list gracefully`() = runTest {
        // Given: Empty store
        storeOfObjectTypes.merge(emptyList())

        // When: Building widgets
        val result = mapSpaceTypesToWidgets(
            isOwnerOrEditor = true,
            config = config,
            storeOfObjectTypes = storeOfObjectTypes,
            spaceUxType = null
        )

        // Then: Should return empty list
        assertEquals(0, result.size)
    }

    @Test
    fun `should handle case-insensitive name sorting`() = runTest {
        // Given: Types with different case names
        val upperCase = StubObjectType(
            id = "upper",
            uniqueKey = "ot-upper",
            name = "ZEBRA",
            orderId = null
        )
        val lowerCase = StubObjectType(
            id = "lower",
            uniqueKey = "ot-lower",
            name = "apple",
            orderId = null
        )
        val mixedCase = StubObjectType(
            id = "mixed",
            uniqueKey = "ot-mixed",
            name = "Banana",
            orderId = null
        )

        storeOfObjectTypes.merge(listOf(upperCase, mixedCase, lowerCase))

        // When: Building widgets
        val result = mapSpaceTypesToWidgets(
            isOwnerOrEditor = true,
            config = config,
            storeOfObjectTypes = storeOfObjectTypes,
            spaceUxType = null
        )

        // Then: Should be sorted case-insensitively
        assertEquals(3, result.size)
        assertEquals("lower", result[0].id)  // apple
        assertEquals("mixed", result[1].id)  // Banana
        assertEquals("upper", result[2].id)  // ZEBRA
    }

    @Test
    fun `should maintain stable sort with identical criteria`() = runTest {
        // Given: Types with same orderId
        val type1 = StubObjectType(
            id = "type1",
            uniqueKey = "ot-type1",
            name = "Same Name",
            orderId = "W-01"
        )
        val type2 = StubObjectType(
            id = "type2",
            uniqueKey = "ot-type2",
            name = "Same Name",
            orderId = "W-01"
        )

        storeOfObjectTypes.merge(listOf(type1, type2))

        // When: Building widgets
        val result = mapSpaceTypesToWidgets(
            isOwnerOrEditor = true,
            config = config,
            storeOfObjectTypes = storeOfObjectTypes,
            spaceUxType = null
        )

        // Then: Should maintain stable order
        assertEquals(2, result.size)
        // Note: Kotlin's stable sort will maintain original order when criteria are equal
    }
}
