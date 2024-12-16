package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.core_models.Command.Move
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.interactor.Move.Params
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

/**
 * Use-case for moving a group of blocks (cross-document, inside one document, one block after another, etc).
 * @see Params for details.
 */
class Move @Inject constructor(
    private val repo: BlockRepository,
    appCoroutineDispatchers: AppCoroutineDispatchers
) : ResultInteractor<Params, Payload>(appCoroutineDispatchers.io) {

    override suspend fun doWork(params: Params) = repo.move(
        command = Move(
            ctx = params.context,
            targetId = params.targetId,
            targetContextId = params.targetContext,
            position = params.position,
            blockIds = params.blockIds
        )
    )

    /**
     * Params for moving a group of blocks.
     * @param context context for this action (i.e. a page's id or a dashboard's id)
     * @param targetId id of the target block (i.e. target of a drag-and-drop action). Can be empty.
     * @param blockIds id of the blocks that are being dragged and dropped (as opposed to the target block).
     * @param targetContext context for target (used primarily for cross-page drag-and-drop)
     * @param position position of the blocks that are being dragged and dropped related to the target block
     * @see Position for details
     */
    data class Params(
        val context: Id,
        val targetId: Id,
        val targetContext: Id,
        val blockIds: List<Id>,
        val position: Position
    )
}