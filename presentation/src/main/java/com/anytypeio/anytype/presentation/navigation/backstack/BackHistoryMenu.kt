package com.anytypeio.anytype.presentation.navigation.backstack

import com.anytypeio.anytype.core_models.Id

/**
 * An object-screen entry currently present in the navigation back stack.
 *
 * @property entryId unique id of the underlying back-stack entry, used for popping back to it.
 * @property objectId id of the object shown by the screen.
 * @property space id of the space the object belongs to.
 */
data class BackStackObjectEntry(
    val entryId: String,
    val objectId: Id,
    val space: Id
)

sealed class BackHistoryMenuState {
    data object Hidden : BackHistoryMenuState()
    data class Visible(val items: List<BackHistoryMenuItem>) : BackHistoryMenuState()
}

data class BackHistoryMenuItem(
    val entryId: String,
    val objectId: Id,
    val space: Id,
    val name: String
)

const val BACK_HISTORY_MENU_LIMIT = 5

/**
 * Builds the list of back-history candidates for the long-press menu.
 *
 * @param entries object-screen entries in back-stack order, bottom -> top (last = current screen).
 * @return previous objects, most-recent-first, deduped by object id, capped at [limit];
 * entries showing the same object as the current screen are excluded.
 */
fun buildBackHistoryCandidates(
    entries: List<BackStackObjectEntry>,
    limit: Int = BACK_HISTORY_MENU_LIMIT
): List<BackStackObjectEntry> {
    if (entries.size < 2) return emptyList()
    val current = entries.last()
    return entries.dropLast(1)
        .filter { it.objectId != current.objectId }
        .reversed()
        .distinctBy { it.objectId }
        .take(limit)
}
