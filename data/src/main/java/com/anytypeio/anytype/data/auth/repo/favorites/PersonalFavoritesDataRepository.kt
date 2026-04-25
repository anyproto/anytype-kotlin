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

class PersonalFavoritesDataRepository(
    private val blockRepo: BlockRepository
) : PersonalFavoritesRepository {

    override suspend fun add(space: SpaceId, target: Id): Payload {
        val ctx = personalWidgetsId(space)
        return blockRepo.createWidget(
            ctx = ctx,
            source = target,
            layout = Block.Content.Widget.Layout.LINK,
            target = null,
            position = Position.INNER_FIRST
        )
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
        val view = blockRepo.openObject(ctx, space)
        // Per anytype-heart GO-6962 (PR #3092), ListDelete on the personal-widgets
        // doc expects the INNER LINK block IDs — NOT the wrapper. The middleware
        // unlink handler internally cascades to remove both the link and its
        // parent wrapper. Matches iOS removeWidgetBlock(widgetBlockId:), where
        // the "widgetBlockId" parameter is (per sibling helper
        // targetObjectIdByLinkFor(widgetBlockId:)) actually the link block ID.
        val linkIds = view.innerLinkIdsTargeting(target)
        if (linkIds.isEmpty()) return null
        return blockRepo.unlink(Command.Unlink(context = ctx, targets = linkIds))
    }

    override suspend fun reorder(space: SpaceId, orderedTargets: List<Id>): List<Payload> {
        if (orderedTargets.size < 2) return emptyList()
        val ctx = personalWidgetsId(space)
        val view = blockRepo.openObject(ctx, space)
        val wrappers = orderedTargets.mapNotNull { view.wrapperIdTargeting(it) }
        if (wrappers.size < 2) return emptyList()
        // Anchor each subsequent wrapper to Position.BOTTOM of the previous one.
        // Since the personal-widgets doc's root has only favorite wrappers as
        // children, after this loop the final children order matches [wrappers]
        // exactly. Matches iOS favoritesDropFinish and onMovePinned (channel pins).
        // Position.INNER targeting root — used previously here — is ambiguous and
        // does not express a relative position, which produced unpredictable order.
        return wrappers.zipWithNext().map { (prev, curr) ->
            blockRepo.move(
                Command.Move(
                    ctx = ctx,
                    targetContextId = ctx,
                    blockIds = listOf(curr),
                    targetId = prev,
                    position = Position.BOTTOM
                )
            )
        }
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
