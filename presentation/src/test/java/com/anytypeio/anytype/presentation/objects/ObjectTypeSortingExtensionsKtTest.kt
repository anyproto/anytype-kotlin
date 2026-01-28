package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.StubObjectType
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

class ObjectTypeSortingExtensionsKtTest {

    @Test
    fun `Empty list handling`() {
        // Given an empty list of ObjectWrapper.Type
        val emptyList = emptyList<com.anytypeio.anytype.core_models.ObjectWrapper.Type>()

        // When sorted with isChatSpace = false
        val resultNonChat = emptyList.sortByTypePriority(isChatSpace = false)
        // Then result is empty
        assertTrue(resultNonChat.isEmpty())

        // When sorted with isChatSpace = true
        val resultChat = emptyList.sortByTypePriority(isChatSpace = true)
        // Then result is empty
        assertTrue(resultChat.isEmpty())
    }

    @Test
    fun `Single item list sorting`() {
        // Given a list with a single ObjectWrapper.Type
        val singleType = StubObjectType(
            id = "type1",
            uniqueKey = ObjectTypeIds.PAGE,
            name = "Page"
        )
        val list = listOf(singleType)

        // When sorted
        val result = list.sortByTypePriority(isChatSpace = false)

        // Then the result contains just that element
        assertEquals(1, result.size)
        assertEquals(singleType.id, result[0].id)
    }

    @Test
    fun `Primary sort by orderId presence`() {
        // Given types with and without orderId
        val typeWithOrderId = StubObjectType(
            id = "type1",
            uniqueKey = "unknown1",
            name = "ZZZ Type",
            orderId = "001"
        )
        val typeWithoutOrderId = StubObjectType(
            id = "type2",
            uniqueKey = ObjectTypeIds.PAGE,
            name = "AAA Type",
            orderId = null
        )
        val list = listOf(typeWithoutOrderId, typeWithOrderId)

        // When sorted
        val result = list.sortByTypePriority(isChatSpace = false)

        // Then type with orderId comes first
        assertEquals(typeWithOrderId.id, result[0].id)
        assertEquals(typeWithoutOrderId.id, result[1].id)
    }

    @Test
    fun `Primary sort by orderId ascending value`() {
        // Given multiple types all having non-null orderIds
        val type1 = StubObjectType(id = "type1", uniqueKey = "uk1", name = "Type1", orderId = "003")
        val type2 = StubObjectType(id = "type2", uniqueKey = "uk2", name = "Type2", orderId = "001")
        val type3 = StubObjectType(id = "type3", uniqueKey = "uk3", name = "Type3", orderId = "002")
        val list = listOf(type1, type2, type3)

        // When sorted
        val result = list.sortByTypePriority(isChatSpace = false)

        // Then sorted in ascending order by orderId
        assertEquals("001", result[0].orderId)
        assertEquals("002", result[1].orderId)
        assertEquals("003", result[2].orderId)
    }

    @Test
    fun `Primary sort with mixed orderId non null and null`() {
        // Given a mix of types with and without orderIds
        val typeNoOrder1 = StubObjectType(id = "no1", uniqueKey = ObjectTypeIds.PAGE, name = "Page", orderId = null)
        val typeWithOrder1 = StubObjectType(id = "with1", uniqueKey = "uk1", name = "Type1", orderId = "002")
        val typeNoOrder2 = StubObjectType(id = "no2", uniqueKey = ObjectTypeIds.NOTE, name = "Note", orderId = null)
        val typeWithOrder2 = StubObjectType(id = "with2", uniqueKey = "uk2", name = "Type2", orderId = "001")
        val list = listOf(typeNoOrder1, typeWithOrder1, typeNoOrder2, typeWithOrder2)

        // When sorted
        val result = list.sortByTypePriority(isChatSpace = false)

        // Then types with orderId come first, sorted by orderId value
        assertEquals("with2", result[0].id) // orderId = "001"
        assertEquals("with1", result[1].id) // orderId = "002"
        // Then types without orderId, sorted by custom order
        assertEquals("no1", result[2].id) // PAGE comes before NOTE
        assertEquals("no2", result[3].id)
    }

    @Test
    fun `Secondary sort for non chat space`() {
        // Given types with null orderIds and different uniqueKeys
        val pageType = StubObjectType(id = "page", uniqueKey = ObjectTypeIds.PAGE, name = "Page")
        val noteType = StubObjectType(id = "note", uniqueKey = ObjectTypeIds.NOTE, name = "Note")
        val taskType = StubObjectType(id = "task", uniqueKey = ObjectTypeIds.TASK, name = "Task")
        val imageType = StubObjectType(id = "image", uniqueKey = ObjectTypeIds.IMAGE, name = "Image")
        // Shuffle order
        val list = listOf(imageType, taskType, pageType, noteType)

        // When sorted with isChatSpace = false
        val result = list.sortByTypePriority(isChatSpace = false)

        // Then sorted by non-chat custom order: PAGE, NOTE, TASK, ..., IMAGE
        assertEquals("page", result[0].id)
        assertEquals("note", result[1].id)
        assertEquals("task", result[2].id)
        assertEquals("image", result[3].id)
    }

    @Test
    fun `Secondary sort for chat space`() {
        // Given types with null orderIds and different uniqueKeys
        val pageType = StubObjectType(id = "page", uniqueKey = ObjectTypeIds.PAGE, name = "Page")
        val noteType = StubObjectType(id = "note", uniqueKey = ObjectTypeIds.NOTE, name = "Note")
        val imageType = StubObjectType(id = "image", uniqueKey = ObjectTypeIds.IMAGE, name = "Image")
        val bookmarkType = StubObjectType(id = "bookmark", uniqueKey = ObjectTypeIds.BOOKMARK, name = "Bookmark")
        // Shuffle order
        val list = listOf(pageType, noteType, imageType, bookmarkType)

        // When sorted with isChatSpace = true
        val result = list.sortByTypePriority(isChatSpace = true)

        // Then sorted by chat custom order: IMAGE, BOOKMARK, ..., PAGE, NOTE
        assertEquals("image", result[0].id)
        assertEquals("bookmark", result[1].id)
        assertEquals("page", result[2].id)
        assertEquals("note", result[3].id)
    }

    @Test
    fun `Secondary sort with unknown uniqueKey`() {
        // Given types where some have unknown uniqueKeys
        val pageType = StubObjectType(id = "page", uniqueKey = ObjectTypeIds.PAGE, name = "Page")
        val unknownType = StubObjectType(id = "unknown", uniqueKey = "custom-unknown-key", name = "Unknown")
        val noteType = StubObjectType(id = "note", uniqueKey = ObjectTypeIds.NOTE, name = "Note")
        val list = listOf(unknownType, pageType, noteType)

        // When sorted
        val result = list.sortByTypePriority(isChatSpace = false)

        // Then known types come first in custom order, unknown at the end
        assertEquals("page", result[0].id)
        assertEquals("note", result[1].id)
        assertEquals("unknown", result[2].id)
    }

    @Test
    fun `Tertiary sort by name case insensitive`() {
        // Given types with same orderId (null) and same unknown uniqueKey, different names
        val typeA = StubObjectType(id = "a", uniqueKey = "unknown", name = "Apple")
        val typeB = StubObjectType(id = "b", uniqueKey = "unknown", name = "banana")
        val typeC = StubObjectType(id = "c", uniqueKey = "unknown", name = "Cherry")
        val list = listOf(typeC, typeA, typeB)

        // When sorted
        val result = list.sortByTypePriority(isChatSpace = false)

        // Then sorted alphabetically case-insensitive: Apple, banana, Cherry
        assertEquals("a", result[0].id) // Apple
        assertEquals("b", result[1].id) // banana
        assertEquals("c", result[2].id) // Cherry
    }

    @Test
    fun `Tertiary sort with empty name`() {
        // Given types with empty name vs non-empty name
        // Note: sorting treats null name as "", so we test with ""
        val typeWithName = StubObjectType(id = "named", uniqueKey = "unknown", name = "Zebra")
        val typeEmptyName = StubObjectType(id = "empty", uniqueKey = "unknown", name = "")
        val list = listOf(typeWithName, typeEmptyName)

        // When sorted
        val result = list.sortByTypePriority(isChatSpace = false)

        // Then empty name (treated as "") comes before "Zebra"
        assertEquals("empty", result[0].id)
        assertEquals("named", result[1].id)
    }

    @Test
    fun `Tertiary sort with multiple empty names`() {
        // Given multiple types with empty names
        // Note: sorting treats null name as "", so we test with ""
        val type1 = StubObjectType(id = "empty1", uniqueKey = "unknown", name = "")
        val type2 = StubObjectType(id = "empty2", uniqueKey = "unknown", name = "")
        val type3 = StubObjectType(id = "empty3", uniqueKey = "unknown", name = "")
        val list = listOf(type1, type2, type3)

        // When sorted
        val result = list.sortByTypePriority(isChatSpace = false)

        // Then all are present (stable sort)
        assertEquals(3, result.size)
        assertTrue(result.map { it.id }.containsAll(listOf("empty1", "empty2", "empty3")))
    }

    @Test
    fun `Complex sort combining all criteria for normal space`() {
        // Given a heterogeneous list
        val typeWithOrderIdLow = StubObjectType(id = "order1", uniqueKey = "uk1", name = "ZType", orderId = "001")
        val typeWithOrderIdHigh = StubObjectType(id = "order2", uniqueKey = "uk2", name = "AType", orderId = "002")
        val pageType = StubObjectType(id = "page", uniqueKey = ObjectTypeIds.PAGE, name = "Page")
        val noteType = StubObjectType(id = "note", uniqueKey = ObjectTypeIds.NOTE, name = "Note")
        val unknownType1 = StubObjectType(id = "unk1", uniqueKey = "unknown", name = "Alpha")
        val unknownType2 = StubObjectType(id = "unk2", uniqueKey = "unknown", name = "Beta")

        val list = listOf(unknownType2, noteType, typeWithOrderIdHigh, pageType, unknownType1, typeWithOrderIdLow)

        // When sorted for normal space
        val result = list.sortByTypePriority(isChatSpace = false)

        // Then:
        // 1. Types with orderId first, sorted by orderId value
        assertEquals("order1", result[0].id) // orderId = "001"
        assertEquals("order2", result[1].id) // orderId = "002"
        // 2. Types without orderId, sorted by custom order
        assertEquals("page", result[2].id) // PAGE
        assertEquals("note", result[3].id) // NOTE
        // 3. Unknown uniqueKeys sorted by name
        assertEquals("unk1", result[4].id) // Alpha
        assertEquals("unk2", result[5].id) // Beta
    }

    @Test
    fun `Complex sort combining all criteria for chat space`() {
        // Given a heterogeneous list
        val typeWithOrderId = StubObjectType(id = "order1", uniqueKey = "uk1", name = "Type", orderId = "001")
        val pageType = StubObjectType(id = "page", uniqueKey = ObjectTypeIds.PAGE, name = "Page")
        val imageType = StubObjectType(id = "image", uniqueKey = ObjectTypeIds.IMAGE, name = "Image")
        val bookmarkType = StubObjectType(id = "bookmark", uniqueKey = ObjectTypeIds.BOOKMARK, name = "Bookmark")
        val unknownType = StubObjectType(id = "unk", uniqueKey = "unknown", name = "Custom")

        val list = listOf(pageType, unknownType, imageType, typeWithOrderId, bookmarkType)

        // When sorted for chat space
        val result = list.sortByTypePriority(isChatSpace = true)

        // Then:
        // 1. Type with orderId first
        assertEquals("order1", result[0].id)
        // 2. Types without orderId, sorted by chat custom order: IMAGE, BOOKMARK, ..., PAGE
        assertEquals("image", result[1].id)
        assertEquals("bookmark", result[2].id)
        assertEquals("page", result[3].id)
        // 3. Unknown uniqueKey at the end
        assertEquals("unk", result[4].id)
    }

    @Test
    fun `List with all elements having same priority`() {
        // Given types where all have null orderIds and unknown uniqueKeys
        val typeC = StubObjectType(id = "c", uniqueKey = "custom1", name = "Charlie")
        val typeA = StubObjectType(id = "a", uniqueKey = "custom2", name = "Alpha")
        val typeB = StubObjectType(id = "b", uniqueKey = "custom3", name = "Bravo")
        val list = listOf(typeC, typeA, typeB)

        // When sorted
        val result = list.sortByTypePriority(isChatSpace = false)

        // Then sorted solely by name
        assertEquals("a", result[0].id) // Alpha
        assertEquals("b", result[1].id) // Bravo
        assertEquals("c", result[2].id) // Charlie
    }

    @Test
    fun `List with duplicate elements`() {
        // Given a list with duplicate ObjectWrapper.Type instances
        val type = StubObjectType(id = "type1", uniqueKey = ObjectTypeIds.PAGE, name = "Page")
        val list = listOf(type, type, type)

        // When sorted
        val result = list.sortByTypePriority(isChatSpace = false)

        // Then all duplicates are present
        assertEquals(3, result.size)
        assertTrue(result.all { it.id == "type1" })
    }

    @Test
    fun `Sort stability check`() {
        // Given elements that are perfectly equal across all sorting criteria
        val type1 = StubObjectType(id = "id1", uniqueKey = "same-key", name = "Same Name", orderId = "001")
        val type2 = StubObjectType(id = "id2", uniqueKey = "same-key", name = "Same Name", orderId = "001")
        val type3 = StubObjectType(id = "id3", uniqueKey = "same-key", name = "Same Name", orderId = "001")
        val list = listOf(type1, type2, type3)

        // When sorted
        val result = list.sortByTypePriority(isChatSpace = false)

        // Then original relative order is preserved (stable sort)
        assertEquals("id1", result[0].id)
        assertEquals("id2", result[1].id)
        assertEquals("id3", result[2].id)
    }
}
