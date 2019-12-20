package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.interactor.DragAndDrop.Params
import com.agileburo.anytype.domain.block.model.Command.Dnd
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for drag-and-drop actions (can be applied to a page or a dashboard).
 * @see Params for details.
 */
class DragAndDrop(
    private val repo: BlockRepository
) : BaseUseCase<Unit, DragAndDrop.Params>() {

    override suspend fun run(params: Params) = try {
        repo.dnd(
            command = Dnd(
                contextId = params.context,
                targetId = params.targetId,
                targetContextId = params.targetContext,
                position = params.position,
                blockIds = params.blockIds
            )
        ).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * Params for a drag-and-drop action
     * @param context context for this action (i.e. a page's id or a dashboard's id)
     * @param targetId id of the target block (i.e. target of a drag-and-drop action)
     * @param blockIds id of the blocks that are being dragged and dropped (as opposed to the target block).
     * @param targetContext context for target (used primarily for cross-page drag-and-drop)
     * @param position position of the blocks that are being dragged and dropped related to the target block
     * @see Position for details
     */
    data class Params(
        val context: String,
        val targetId: String,
        val targetContext: String,
        val blockIds: List<String>,
        val position: Position
    )
}