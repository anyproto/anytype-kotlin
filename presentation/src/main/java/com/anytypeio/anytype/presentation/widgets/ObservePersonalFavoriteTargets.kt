package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.widgets.BundledWidgetSourceIds
import com.anytypeio.anytype.domain.base.getOrThrow
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.favorites.personalWidgetsId
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.presentation.common.PayloadDelegator
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan

/**
 * Observes the user's personal-favorite object IDs in [SpaceId].
 *
 * Opens the per-user, per-space virtual widgets doc
 * (`_personalWidgets_<encodedSpaceId>` — see
 * [com.anytypeio.anytype.domain.favorites.personalWidgetsId]) via [OpenObject]
 * for the initial [ObjectView], then folds two event streams into the tree:
 *  - [InterceptEvents] on the same context — receives free-standing events
 *    middleware pushes via the global event stream (e.g. desktop or another
 *    device mutates the personal-widgets doc);
 *  - [PayloadDelegator.intercept] on the same context — receives events from
 *    locally-initiated mutations (createWidget / unlink / move) that the
 *    middleware embeds in the RPC response and does NOT also broadcast on the
 *    push stream. Without this merge, locally-initiated changes never reach
 *    this reducer until the next OpenObject.
 *
 * Lives in the presentation module so it can consume the
 * presentation-layer [PayloadDelegator] without violating module boundaries.
 *
 * Shared between [PersonalFavoritesWidgetContainer] (which renders the
 * "My Favorites" section rows) and the object-menu options provider (which
 * decides whether to show Favorite vs Unfavorite).
 */
class ObservePersonalFavoriteTargets @Inject constructor(
    private val openObject: OpenObject,
    private val interceptEvents: InterceptEvents,
    private val payloadDelegator: PayloadDelegator
) {

    operator fun invoke(space: SpaceId): Flow<List<Id>> = personalWidgetsTree(space)
        .map { tree -> tree.orderedRealTargets() }
        .distinctUntilChanged()

    private fun personalWidgetsTree(space: SpaceId): Flow<ObjectView> = flow {
        val docId = personalWidgetsId(space)
        val initial = openObject.async(
            OpenObject.Params(
                obj = docId,
                spaceId = space,
                saveAsLastOpened = false
            )
        ).getOrThrow()
        val mwEvents: Flow<List<Event>> = interceptEvents
            .build(InterceptEvents.Params(context = docId))
        val localEvents: Flow<List<Event>> = payloadDelegator
            .intercept(docId)
            .map { it.events }
        emitAll(
            merge(mwEvents, localEvents)
                .scan(initial) { state, events -> reduce(state, events) }
        )
    }
}

/**
 * Reduces block-tree events relevant to personal-favorites rendering into an
 * updated [ObjectView]. Handles add/delete block, update-structure (reorder),
 * and link-target change — the events that can change the flat list of
 * `<wrapper-widget> -> <link-block(target=objectId)>` children of root.
 * Other events are intentionally ignored.
 */
private fun reduce(state: ObjectView, events: List<Event>): ObjectView {
    var curr = state
    events.forEach { event ->
        when (event) {
            is Event.Command.AddBlock -> {
                curr = curr.copy(blocks = curr.blocks + event.blocks)
            }
            is Event.Command.DeleteBlock -> {
                curr = curr.copy(blocks = curr.blocks.filter { it.id !in event.targets })
            }
            is Event.Command.UpdateStructure -> {
                curr = curr.copy(blocks = curr.blocks.map { block ->
                    if (block.id == event.id) block.copy(children = event.children) else block
                })
            }
            is Event.Command.LinkGranularChange -> {
                curr = curr.copy(blocks = curr.blocks.map { block ->
                    if (block.id != event.id) return@map block
                    val content = block.content as? Block.Content.Link ?: return@map block
                    block.copy(content = content.copy(target = event.target))
                })
            }
            else -> {
                // Other events do not affect the personal-favorites target list.
            }
        }
    }
    return curr
}

/**
 * Walks root's children to collect ordered target-object IDs. Expects each
 * root child to be a wrapper block whose first child is a link block pointing
 * at a real object. Built-in targets (`favorite`, `recent`, `allObjects`, …)
 * are filtered out.
 */
private fun ObjectView.orderedRealTargets(): List<Id> {
    val byId = blocks.associateBy { it.id }
    val rootBlock = byId[root] ?: return emptyList()
    return rootBlock.children.mapNotNull { wrapperId ->
        val wrapper = byId[wrapperId] ?: return@mapNotNull null
        if (wrapper.content !is Block.Content.Widget) return@mapNotNull null
        val firstChildId = wrapper.children.firstOrNull() ?: return@mapNotNull null
        val child = byId[firstChildId] ?: return@mapNotNull null
        val link = child.content as? Block.Content.Link ?: return@mapNotNull null
        link.target.takeIf { it !in BundledWidgetSourceIds.ids }
    }
}
