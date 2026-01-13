package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper

/**
 * Provides custom order for object types based on space context.
 * Chat spaces prioritize media types (images, files), while normal spaces prioritize document types (pages, notes).
 */
object ObjectTypePriority {
    fun getCustomOrder(isChatSpace: Boolean): List<String> = if (!isChatSpace) {
        listOf(
            ObjectTypeIds.PAGE,
            ObjectTypeIds.NOTE,
            ObjectTypeIds.TASK,
            ObjectTypeIds.CHAT_DERIVED,
            ObjectTypeIds.COLLECTION,
            ObjectTypeIds.SET,
            ObjectTypeIds.BOOKMARK,
            ObjectTypeIds.PROJECT,
            ObjectTypeIds.IMAGE,
            ObjectTypeIds.FILE,
            ObjectTypeIds.VIDEO,
            ObjectTypeIds.AUDIO,
            ObjectTypeIds.TEMPLATE
        )
    } else {
        listOf(
            ObjectTypeIds.IMAGE,
            ObjectTypeIds.BOOKMARK,
            ObjectTypeIds.FILE,
            ObjectTypeIds.PAGE,
            ObjectTypeIds.NOTE,
            ObjectTypeIds.TASK,
            ObjectTypeIds.COLLECTION,
            ObjectTypeIds.SET,
            ObjectTypeIds.PROJECT,
            ObjectTypeIds.VIDEO,
            ObjectTypeIds.AUDIO,
            ObjectTypeIds.TEMPLATE
        )
    }
}

/**
 * Sorts a list of ObjectWrapper.Type objects by priority.
 * 1. Primary: orderId (ascending, nulls at end)
 * 2. Secondary: customUniqueKeyOrder (position in list based on space context)
 * 3. Tertiary: name (ascending, case-insensitive)
 *
 * @param isChatSpace Whether the current space is a chat space (affects type ordering)
 * @return A new list of ObjectWrapper.Type objects sorted by the specified priority.
 */
fun List<ObjectWrapper.Type>.sortByTypePriority(
    isChatSpace: Boolean = false
): List<ObjectWrapper.Type> {
    val customOrder = ObjectTypePriority.getCustomOrder(isChatSpace)
    return sortedWith(
        compareBy<ObjectWrapper.Type> { objectType ->
            // Primary sort: orderId presence (items with orderId come first)
            if (objectType.orderId != null) 0 else 1
        }.thenBy { objectType ->
            // Primary sort continuation: orderId value (for items that have orderId)
            objectType.orderId ?: ""
        }.thenBy { objectType ->
            // Secondary sort: custom order by uniqueKey
            val index = customOrder.indexOf(objectType.uniqueKey)
            if (index >= 0) index else Int.MAX_VALUE
        }.thenBy { objectType ->
            // Tertiary sort: name (case-insensitive)
            objectType.name?.lowercase() ?: ""
        }
    )
}
