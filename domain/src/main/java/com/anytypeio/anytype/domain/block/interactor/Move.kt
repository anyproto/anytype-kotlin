package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.Move.Params
import com.anytypeio.anytype.domain.block.model.Command.Move
import com.anytypeio.anytype.domain.block.model.Position
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.event.model.Payload

/**
 * Use-case for moving a group of blocks (cross-document, inside one document, one block after another, etc).
 * @see Params for details.
 */
class Move(
    private val repo: BlockRepository
) : BaseUseCase<Payload, Params>() {

    override suspend fun run(params: Params) = try {
        repo.move(
            command = Move(
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
     * Params for moving a group of blocks.
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