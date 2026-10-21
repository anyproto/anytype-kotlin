package com.anytypeio.anytype.presentation.navigation.backstack

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.search.buildDeletedFilter
import com.anytypeio.anytype.presentation.search.buildLimitedObjectIdsFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Shared logic for the long-press-on-back-button history menu (DROID-4518).
 * Composed into screen view models via Kotlin delegation, like [com.anytypeio.anytype.presentation.vault.ExitToVaultDelegate].
 */
interface BackHistoryDelegate {

    val backHistoryMenu: StateFlow<BackHistoryMenuState>

    /**
     * Back-stack id of the "Home" row the menu is currently offering, or null when no Home row
     * is shown. Read by screens when the user taps Home, to pop back to that entry.
     */
    val currentHomeEntryId: String?

    /**
     * Takes a fresh snapshot of the navigation back stack and shows the menu
     * if there is at least one previous object besides the current screen;
     * otherwise the menu stays hidden (silent no-op).
     */
    suspend fun onBackButtonLongPressed()

    fun onBackHistoryMenuDismissed()

    class Default(
        private val inspector: NavigationBackStackInspector,
        private val searchObjects: SearchObjects,
        private val fieldParser: FieldParser,
        private val dispatchers: AppCoroutineDispatchers
    ) : BackHistoryDelegate {

        private val state = MutableStateFlow<BackHistoryMenuState>(BackHistoryMenuState.Hidden)

        override val backHistoryMenu: StateFlow<BackHistoryMenuState> = state

        override val currentHomeEntryId: String?
            get() = (state.value as? BackHistoryMenuState.Visible)?.homeEntryId

        override suspend fun onBackButtonLongPressed() {
            val candidates = buildBackHistoryCandidates(
                entries = inspector.objectScreenEntries(),
                currentEntryId = inspector.currentEntryId()
            )
            val homeEntryId = inspector.homeScreenEntryId()
            if (candidates.isEmpty() && homeEntryId == null) return
            val names = resolveNames(candidates)
            state.value = BackHistoryMenuState.Visible(
                homeEntryId = homeEntryId,
                items = candidates.map { candidate ->
                    BackHistoryMenuItem(
                        entryId = candidate.entryId,
                        objectId = candidate.objectId,
                        space = candidate.space,
                        name = names[candidate.objectId].orEmpty()
                    )
                }
            )
        }

        override fun onBackHistoryMenuDismissed() {
            state.value = BackHistoryMenuState.Hidden
        }

        private suspend fun resolveNames(
            candidates: List<BackStackObjectEntry>
        ): Map<Id, String> = withContext(dispatchers.io) {
            val names = mutableMapOf<Id, String>()
            candidates.groupBy { it.space }.forEach { (space, group) ->
                searchObjects(
                    SearchObjects.Params(
                        space = SpaceId(space),
                        filters = buildList {
                            add(
                                buildLimitedObjectIdsFilter(
                                limitedObjectIds = group.map { it.objectId }
                            )
                            )
                            addAll(buildDeletedFilter())
                        },
                        keys = listOf(
                            Relations.ID,
                            Relations.NAME,
                            Relations.LAYOUT,
                            Relations.SNIPPET,
                            Relations.TIMESTAMP,
                            Relations.FILE_EXT,
                            Relations.SPACE_ID,
                            Relations.IS_DELETED
                        ),
                        limit = group.size
                    )
                ).process(
                    success = { objects ->
                        objects.forEach { obj ->
                            names[obj.id] = fieldParser.getObjectName(
                                objectWrapper = obj,
                                useUntitled = true
                            )
                        }
                    },
                    failure = {
                        Timber.e(it, "Error while resolving back-history object names")
                    }
                )
            }
            names
        }
    }
}
