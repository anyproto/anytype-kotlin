package com.anytypeio.anytype.feature_chats.ui

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.chats.ChatReadSnapshot

internal data class ChatReadVisibility(
    val range: Pair<Id, Id>?,
    val snapshot: ChatReadSnapshot?,
    val generation: Int
)

internal fun isChatReadLayoutCurrent(
    keys: List<String>,
    totalItemsCount: Int,
    visibleKeys: List<Pair<Int, Any>>
): Boolean = totalItemsCount == keys.size && visibleKeys.all { (index, key) -> keys.getOrNull(index) == key }

/** Ordinary messages must fit on screen; oversized messages must fill the viewport. */
internal fun isMessageVisibleForReading(
    offset: Int,
    size: Int,
    viewportStart: Int,
    viewportEnd: Int
): Boolean {
    if (size <= 0 || viewportEnd <= viewportStart) return false
    val end = offset.toLong() + size
    return if (size <= viewportEnd.toLong() - viewportStart) {
        offset >= viewportStart && end <= viewportEnd
    } else {
        offset <= viewportStart && end >= viewportEnd
    }
}
