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
