package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id

/**
 * Use-case for merging a pair of blocks.
 */
class MergeBlocks(private val repo: BlockRepository) : BaseUseCase<Unit, MergeBlocks.Params>() {

    override suspend fun run(params: Params) = try {
        repo.merge(
            command = Command.Merge(
                context = params.context,
                pair = params.pair
            )
        ).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * Params for merging two blocks into one block
     * @property context context id
     * @property pair pair of the blocks, which we need to merge
     */
    data class Params(
        val context: Id,
        val pair: Pair<Id, Id>
    )
}