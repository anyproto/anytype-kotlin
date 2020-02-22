package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id

/**
 * Use-case for splitting the target block into two blocks based on cursor position.
 */
class SplitBlock(private val repo: BlockRepository) : BaseUseCase<String, SplitBlock.Params>() {

    override suspend fun run(params: Params) = try {
        repo.split(
            command = Command.Split(
                context = params.context,
                target = params.target,
                index = params.index
            )
        ).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * Params for splitting one block into two blocks
     * @property context context id
     * @property target id of the target block, which we need to split
     * @property index index or cursor position
     */
    data class Params(
        val context: Id,
        val target: Id,
        val index: Int
    )
}