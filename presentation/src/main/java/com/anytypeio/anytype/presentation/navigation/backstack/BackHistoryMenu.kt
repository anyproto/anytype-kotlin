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

    /**
     * @property homeEntryId back-stack id of the space home (widgets) screen if it is present
     * in the navigation history, otherwise null. When set, the menu shows a "Home" entry.
     * @property items previously visited objects, most-recent-first.
     */
    data class Visible(
        val homeEntryId: String? = null,
        val items: List<BackHistoryMenuItem>
    ) : BackHistoryMenuState()
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
 * @param entries object-screen entries in back-stack order, bottom -> top.
 * @param currentEntryId back-stack id of the currently visible screen. When provided, that exact
 * entry is treated as current; otherwise the last entry is assumed to be current.
 * @return previous objects, most-recent-first, deduped by object id, capped at [limit];
 * the current screen and any other entries showing the same object are excluded.
 */
fun buildBackHistoryCandidates(
    entries: List<BackStackObjectEntry>,
    currentEntryId: String? = null,
    limit: Int = BACK_HISTORY_MENU_LIMIT
): List<BackStackObjectEntry> {
    if (entries.isEmpty()) return emptyList()
    val current = entries.firstOrNull { it.entryId == currentEntryId } ?: entries.last()
    return entries
        .filter { it.entryId != current.entryId && it.objectId != current.objectId }
        .reversed()
        .distinctBy { it.objectId }
        .take(limit)
}
