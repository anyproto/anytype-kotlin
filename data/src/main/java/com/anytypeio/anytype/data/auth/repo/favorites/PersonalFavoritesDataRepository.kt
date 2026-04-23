package com.anytypeio.anytype.data.auth.repo.favorites

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.favorites.PersonalFavoritesRepository
import com.anytypeio.anytype.domain.favorites.personalWidgetsId

class PersonalFavoritesDataRepository(
    private val blocks: BlockRepository
) : PersonalFavoritesRepository {

    override suspend fun add(space: SpaceId, target: Id) {
        blocks.createWidget(
            ctx = personalWidgetsId(space),
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
    // ideal shape would accept a snapshot of wrapper/link IDs from the caller
    // (which already has them via the reactive flow) rather than re-fetching.
    // Follow-up: pass a snapshot through the use-case boundary.

    override suspend fun remove(space: SpaceId, target: Id) {
        val ctx = personalWidgetsId(space)
        val view = blocks.openObject(ctx, space)
        val linkIds = view.innerLinkIdsTargeting(target)
        if (linkIds.isEmpty()) return
        blocks.unlink(Command.Unlink(context = ctx, targets = linkIds))
    }

    override suspend fun reorder(space: SpaceId, orderedTargets: List<Id>) {
        if (orderedTargets.isEmpty()) return
        val ctx = personalWidgetsId(space)
        val view = blocks.openObject(ctx, space)
        val root = view.root
        // TODO(DROID-4397 follow-up): this emits O(n) sequential move RPCs.
        // GO-6962 did not include a batch-move endpoint; when one lands (or
        // once widget-doc snapshots are accepted here), collapse into a single
        // round-trip. Acceptable today since favorite counts stay small.
        orderedTargets.forEach { target ->
            val wrapperId = view.wrapperIdTargeting(target) ?: return@forEach
            blocks.move(
                Command.Move(
                    ctx = ctx,
                    targetContextId = ctx,
                    blockIds = listOf(wrapperId),
                    targetId = root,
                    position = Position.INNER
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
