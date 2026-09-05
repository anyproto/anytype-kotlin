package com.anytypeio.anytype.domain.quickcapture

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.NO_VALUE
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Quick capture: "text follows the chip" — retargets the current draft to another space.
 *
 * Copies the source draft's blocks via the middleware clipboard slots (Block.Copy →
 * Block.Paste addressed by context id only, so the cross-space sequence is legal),
 * creates a hidden draft in the target space (same type where it exists there, else the
 * target space's default type), pastes the blocks and deletes the source draft.
 *
 * Failure contract: EITHER the move fully commits (returns the new draft id, source
 * deleted) OR the source draft stays authoritative and the half-created target object is
 * rolled back — never a hidden orphan copy of the user's text. Object deletion here is
 * `Object.ListDelete` (permanent, no bin), which is why the ordering matters.
 *
 * The draft pointers are rewritten HERE rather than by the caller, inside the same
 * uninterruptible block as the source delete. A caller cannot do it safely: ResultInteractor
 * runs doWork in `withContext(dispatchers.io)`, and withContext rethrows CancellationException
 * on resume if its parent was cancelled, so a caller's `fold(onSuccess = ...)` never runs when
 * the sheet is dismissed mid-move. Bookkeeping left there would be skipped while this block
 * still deleted the source, stranding the user's text in a hidden object no pointer names.
 * The target pointer is written BEFORE the source is deleted, so an interruption leaves a
 * recoverable duplicate rather than an unreachable orphan.
 *
 * Deliberately uses the repository directly instead of the [com.anytypeio.anytype.domain.clipboard.Copy]
 * use case: that one also writes the user's system clipboard, which must not be clobbered
 * as a side effect of switching the space chip.
 *
 * N.B. File/image blocks are space-scoped; whether Block.Paste re-resolves them across
 * spaces is middleware behavior (spec verification item V4).
 */
class MoveQuickCaptureDraft @Inject constructor(
    private val repo: BlockRepository,
    private val getDefaultObjectType: GetDefaultObjectType,
    private val settings: UserSettingsRepository,
    private val logger: Logger,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<MoveQuickCaptureDraft.Params, Id>(dispatchers.io) {

    override suspend fun doWork(params: Params): Id {
        val view = repo.getObject(id = params.from, space = params.fromSpace)

        val root = view.blocks.firstOrNull { it.id == view.root }
        val header = view.blocks.firstOrNull { block ->
            val content = block.content
            content is Block.Content.Layout && content.type == Block.Content.Layout.Type.HEADER
        }
        val contentRootIds = root?.children.orEmpty().filter { it != header?.id }
        val contentBlocks = collectWithDescendants(contentRootIds, view.blocks)

        val name = view.details[view.root]?.get(Relations.NAME) as? String
        val sourceTypeKey = resolveSourceTypeKey(view.details, view.root)
        val targetTypeKey = sourceTypeKey?.takeIf { key ->
            typeExistsInSpace(key = key, space = params.toSpace)
        }

        val created = repo.createObject(
            Command.CreateObject(
                space = params.toSpace,
                typeKey = targetTypeKey?.let { TypeKey(it) }
                    ?: getDefaultObjectType.run(params.toSpace).type,
                template = null,
                internalFlags = buildList {
                    add(InternalFlags.ShouldEmptyDelete)
                    // Reopen the type bar only when the source type could not be carried over.
                    if (targetTypeKey == null) add(InternalFlags.ShouldSelectType)
                },
                prefilled = buildMap {
                    put(Relations.IS_HIDDEN, true)
                    if (!name.isNullOrEmpty()) put(Relations.NAME, name)
                }
            )
        )

        try {
            // Belt-and-braces for spec V5: some create paths may ignore prefilled details,
            // and a visible unpublished draft is the exact failure the hidden-draft
            // mechanism exists to prevent.
            if (created.details[Relations.IS_HIDDEN] != true) {
                repo.setObjectDetails(
                    ctx = created.id,
                    details = mapOf(Relations.IS_HIDDEN to true)
                )
            }
            if (contentBlocks.isNotEmpty()) {
                val copy = repo.copy(
                    Command.Copy(
                        context = params.from,
                        range = null,
                        blocks = contentBlocks
                    )
                )
                val (focus, _) = repo.create(
                    Command.Create(
                        context = created.id,
                        prototype = Block.Prototype.Text(style = Block.Content.Text.Style.P),
                        position = Position.NONE,
                        target = NO_VALUE
                    )
                )
                repo.paste(
                    Command.Paste(
                        context = created.id,
                        focus = focus,
                        selected = emptyList(),
                        range = IntRange(0, 0),
                        text = copy.text,
                        html = copy.html,
                        blocks = copy.blocks,
                        isPartOfBlock = null
                    )
                )
                // Verify the text actually landed before the source is destroyed. A copy or
                // paste that fails *without throwing* (an empty clipboard slot; cross-space
                // paste behaving differently than expected — spec V4) would otherwise commit
                // an empty target and permanently delete the only copy of the body. Throwing
                // here routes into the rollback below, leaving the source authoritative.
                if (hasText(contentBlocks) && !hasText(readContentBlocks(created.id, params.toSpace))) {
                    error("Quick capture: move produced an empty target draft; keeping source")
                }
            }
        } catch (e: Throwable) {
            // Source stays authoritative; never leave a hidden orphan copy of the text.
            withContext(NonCancellable) {
                runCatching { repo.deleteObjects(listOf(created.id)) }
                    .onFailure { logger.logException(it, "Quick capture: could not roll back moved draft") }
            }
            throw e
        }

        // Commit point: the content now lives in the target draft. Deleting the source must
        // not be interruptible half-way (cancellation between paste and delete would leave
        // two copies); a *failed* delete leaves the hidden source as a logged orphan, which
        // is preferable to rolling back the copy the caller is about to point at.
        //
        // Pointer first, delete second. A hidden draft that no pointer names is unreachable
        // by construction — it is excluded from search, recents and widgets — so writing the
        // pointer last would make any interruption here permanent data loss, while writing it
        // first makes the same interruption a duplicate the next open can reconcile.
        withContext(NonCancellable) {
            runCatching { settings.setQuickCaptureDraft(space = params.toSpace, obj = created.id) }
                .onFailure { logger.logException(it, "Quick capture: could not point target space at moved draft") }
            runCatching { settings.clearQuickCaptureDraft(params.fromSpace) }
                .onFailure { logger.logException(it, "Quick capture: could not clear source draft pointer") }
            runCatching { repo.deleteObjects(listOf(params.from)) }
                .onFailure { logger.logException(it, "Quick capture: could not delete source draft after move") }
        }

        return created.id
    }

    /** True when any of these blocks carries non-blank text. */
    private fun hasText(blocks: List<Block>): Boolean = blocks.any { block ->
        val content = block.content
        content is Block.Content.Text && content.text.isNotBlank()
    }

    /** The target draft's blocks below the header, as they exist server-side after the paste. */
    private suspend fun readContentBlocks(obj: Id, space: SpaceId): List<Block> {
        val view = repo.getObject(id = obj, space = space)
        val root = view.blocks.firstOrNull { it.id == view.root }
        val header = view.blocks.firstOrNull { block ->
            val content = block.content
            content is Block.Content.Layout && content.type == Block.Content.Layout.Type.HEADER
        }
        return collectWithDescendants(root?.children.orEmpty().filter { it != header?.id }, view.blocks)
    }

    private fun collectWithDescendants(roots: List<Id>, all: List<Block>): List<Block> {
        val byId = all.associateBy { it.id }
        val result = mutableListOf<Block>()
        val queue = ArrayDeque(roots)
        while (queue.isNotEmpty()) {
            val block = byId[queue.removeFirst()] ?: continue
            result.add(block)
            queue.addAll(block.children)
        }
        return result
    }

    private fun resolveSourceTypeKey(details: Map<Id, Map<String, Any?>>, root: Id): String? {
        val typeId = (details[root]?.get(Relations.TYPE) as? List<*>)
            ?.filterIsInstance<String>()
            ?.firstOrNull()
            ?: details[root]?.get(Relations.TYPE) as? String
            ?: return null
        return details[typeId]?.get(Relations.UNIQUE_KEY) as? String
    }

    private suspend fun typeExistsInSpace(key: String, space: SpaceId): Boolean {
        val result = repo.searchObjects(
            space = space,
            filters = listOf(
                DVFilter(
                    relation = Relations.UNIQUE_KEY,
                    value = key,
                    condition = DVFilterCondition.EQUAL
                ),
                DVFilter(
                    relation = Relations.IS_ARCHIVED,
                    value = true,
                    condition = DVFilterCondition.NOT_EQUAL
                ),
                DVFilter(
                    relation = Relations.IS_DELETED,
                    value = true,
                    condition = DVFilterCondition.NOT_EQUAL
                )
            ),
            limit = 1,
            keys = listOf(Relations.ID, Relations.UNIQUE_KEY)
        )
        return result.isNotEmpty()
    }

    data class Params(
        val from: Id,
        val fromSpace: SpaceId,
        val toSpace: SpaceId
    )
}
