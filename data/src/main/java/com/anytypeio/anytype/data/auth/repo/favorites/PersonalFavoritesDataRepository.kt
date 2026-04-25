package com.anytypeio.anytype.data.auth.repo.favorites

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.favorites.PersonalFavoritesRepository
import com.anytypeio.anytype.domain.favorites.personalWidgetsId

// Data module has no Timber dependency; println output lands in logcat under
// "System.out" and is filterable by the "DROID-4397-FAV" prefix alongside the
// Timber.d entries from the presentation layer.
private fun favLog(msg: String) = println("DROID-4397-FAV $msg")

class PersonalFavoritesDataRepository(
    private val blocks: BlockRepository
) : PersonalFavoritesRepository {

    override suspend fun add(space: SpaceId, target: Id): Payload {
        val ctx = personalWidgetsId(space)
        favLog("[repo] add ENTER: ctx=$ctx, target=$target, space=${space.id}")
        val payload = blocks.createWidget(
            ctx = ctx,
            source = target,
            layout = Block.Content.Widget.Layout.LINK,
            target = null,
            position = Position.INNER_FIRST
        )
        favLog("[repo] add DONE: ctx=$ctx, target=$target, events=${payload.events.size}")
        return payload
    }

    // Note on [openObject] calls in remove/reorder:
    // ObservePersonalFavoriteTargets (domain layer) already holds the personal-
    // widgets doc open via its own OpenObject for the reactive subscription.
    // Each openObject call below adds an extra reference-counted open to the
    // same doc. Middleware handles this (ref-counted, never negative), but the
    // ideal shape would accept a snapshot of wrapper IDs from the caller
    // (which already has them via the reactive flow) rather than re-fetching.
    // Follow-up: pass a snapshot through the use-case boundary.

    override suspend fun remove(space: SpaceId, target: Id): Payload? {
        val ctx = personalWidgetsId(space)
        favLog("[repo] remove ENTER: ctx=$ctx, target=$target, space=${space.id}")
        val view = blocks.openObject(ctx, space)
        favLog(
            "[repo] remove openObject DONE: ctx=$ctx, " +
                    "view.root=${view.root}, view.blocks.size=${view.blocks.size}"
        )
        // Per anytype-heart GO-6962 (PR #3092), ListDelete on the personal-widgets
        // doc expects the INNER LINK block IDs — NOT the wrapper. The middleware
        // unlink handler internally cascades to remove both the link and its
        // parent wrapper. Matches iOS removeWidgetBlock(widgetBlockId:), where
        // the "widgetBlockId" parameter is (per sibling helper
        // targetObjectIdByLinkFor(widgetBlockId:)) actually the link block ID.
        val linkIds = view.innerLinkIdsTargeting(target)
        favLog(
            "[repo] remove linkIds for target=$target: " +
                    "found=${linkIds.size}, ids=$linkIds"
        )
        if (linkIds.isEmpty()) {
            favLog(
                "[repo] remove EARLY-RETURN (NO-OP): " +
                        "target=$target not found in personal-widgets doc snapshot"
            )
            return null
        }
        val payload = blocks.unlink(Command.Unlink(context = ctx, targets = linkIds))
        favLog(
            "[repo] remove unlink DONE: ctx=$ctx, targets=$linkIds, " +
                    "payload.events=${payload.events.size}"
        )
        return payload
    }

    override suspend fun reorder(space: SpaceId, orderedTargets: List<Id>): List<Payload> {
        favLog(
            "[repo] reorder ENTER: space=${space.id}, " +
                    "orderedTargets.size=${orderedTargets.size}"
        )
        if (orderedTargets.size < 2) {
            favLog("[repo] reorder EARLY-RETURN: <2 targets, nothing to reorder")
            return emptyList()
        }
        val ctx = personalWidgetsId(space)
        val view = blocks.openObject(ctx, space)
        val wrappers = orderedTargets.mapNotNull { view.wrapperIdTargeting(it) }
        favLog(
            "[repo] reorder mapped wrappers: " +
                    "asked=${orderedTargets.size}, resolved=${wrappers.size}"
        )
        if (wrappers.size < 2) {
            favLog(
                "[repo] reorder EARLY-RETURN: <2 wrappers resolved — " +
                        "personal-widgets snapshot may be stale"
            )
            return emptyList()
        }
        // Anchor each subsequent wrapper to Position.BOTTOM of the previous one.
        // Since the personal-widgets doc's root has only favorite wrappers as
        // children, after this loop the final children order matches [wrappers]
        // exactly. Matches iOS favoritesDropFinish and onMovePinned (channel pins).
        // Position.INNER targeting root — used previously here — is ambiguous and
        // does not express a relative position, which produced unpredictable order.
        val payloads = wrappers.zipWithNext().map { (prev, curr) ->
            favLog("[repo] reorder move: $curr → BOTTOM of $prev")
            blocks.move(
                Command.Move(
                    ctx = ctx,
                    targetContextId = ctx,
                    blockIds = listOf(curr),
                    targetId = prev,
                    position = Position.BOTTOM
                )
            )
        }
        favLog("[repo] reorder DONE: ${payloads.size} move RPCs sent")
        return payloads
    }

    private fun ObjectView.innerLinkIdsTargeting(target: Id): List<Id> {
        val byId = blocks.associateBy { it.id }
        return blocks.mapNotNull { wrapper ->
            val link = wrapper.firstLinkChild(byId) ?: return@mapNotNull null
            link.id.takeIf { link.targetId() == target }
        }
    }

    private fun ObjectView.wrapperIdTargeting(target: Id): Id? {
        val byId = blocks.associateBy { it.id }
        return blocks.firstOrNull { wrapper ->
            val link = wrapper.firstLinkChild(byId)
            link != null && link.targetId() == target
        }?.id
    }

    private fun Block.firstLinkChild(byId: Map<Id, Block>): Block? {
        if (content !is Block.Content.Widget) return null
        val firstChildId = children.firstOrNull() ?: return null
        val child = byId[firstChildId] ?: return null
        return child.takeIf { it.content is Block.Content.Link }
    }

    private fun Block.targetId(): Id? = (content as? Block.Content.Link)?.target
}
