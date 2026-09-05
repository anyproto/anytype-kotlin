package com.anytypeio.anytype.domain.quickcapture

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import javax.inject.Inject

/**
 * Deletes a quick-capture draft the user walked away from, but only when it holds nothing.
 *
 * Drafts are created with [com.anytypeio.anytype.core_models.InternalFlags.ShouldEmptyDelete],
 * which is heart's own "bin this on close if it is still empty" mechanism — but that flag is
 * one-way and any details write clears it, and quick capture writes details at creation
 * (`isHidden`, `isDraft`). So abandoned empties never actually went away, and each one left a
 * pointer and a picker dot behind it. This does that cleanup explicitly.
 *
 * Emptiness is decided from the STORE, not from the editor, for two reasons: the editor is
 * usually gone by the time the sheet closes, and the store is what the next device will see.
 *
 * The delete is `Object.ListDelete` — permanent, no bin. That is only defensible because the
 * object holds nothing, so every uncertainty resolves to "keep it":
 *  - the object cannot be read, or the read fails      -> keep
 *  - it is no longer a draft (published, archived)     -> keep
 *  - it carries a title, any text, or any attachment   -> keep
 *  - an unrecognised block type is present             -> keep
 */
class DeleteQuickCaptureDraftIfEmpty @Inject constructor(
    private val repo: BlockRepository,
    private val settings: UserSettingsRepository,
    private val logger: Logger,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<DeleteQuickCaptureDraftIfEmpty.Params, Boolean>(dispatchers.io) {

    /** @return true when the draft was empty and has been deleted. */
    override suspend fun doWork(params: Params): Boolean {
        val view = runCatching { repo.getObject(id = params.draft, space = params.space) }
            .onFailure { logger.logException(it, "Quick capture: could not read draft to clean up") }
            .getOrNull() ?: return false

        val details = view.details[view.root].orEmpty()

        // Still ours to delete? A published or archived object is not an abandoned draft.
        // isDraft may be absent on drafts made before the relation existed, in which case
        // isHidden is the marker; an explicit false always means published.
        val isDraft = details[Relations.IS_DRAFT] as? Boolean
        val isHidden = details[Relations.IS_HIDDEN] as? Boolean
        if (isDraft == false || isHidden != true) return false
        if (details[Relations.IS_ARCHIVED] == true || details[Relations.IS_DELETED] == true) {
            return false
        }

        if (!(details[Relations.NAME] as? String).isNullOrBlank()) return false

        val root = view.blocks.firstOrNull { it.id == view.root }
        val header = view.blocks.firstOrNull { block ->
            val content = block.content
            content is Block.Content.Layout && content.type == Block.Content.Layout.Type.HEADER
        }
        val body = root?.children.orEmpty().filter { it != header?.id }
        if (view.blocks.any { it.id in body && it.holdsContent() }) return false

        runCatching { repo.deleteObjects(listOf(params.draft)) }
            .onFailure {
                logger.logException(it, "Quick capture: could not delete empty draft")
                return false
            }
        runCatching { settings.clearQuickCaptureDraft(params.space) }
            .onFailure { logger.logException(it, "Quick capture: could not clear pointer") }
        return true
    }

    /**
     * Inverted on purpose: only known-structural blocks are treated as empty, so a block type
     * this code does not recognise counts as content and spares the object.
     */
    private fun Block.holdsContent(): Boolean = when (val content = content) {
        is Block.Content.Smart,
        is Block.Content.Layout,
        is Block.Content.FeaturedRelations,
        is Block.Content.RelationBlock,
        is Block.Content.Icon -> false
        is Block.Content.Text -> content.text.isNotBlank()
        else -> true
    }

    data class Params(
        val space: SpaceId,
        val draft: Id
    )
}
