package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.event.model.Payload

/**
 * Use-case for unlinking blocks from its context.
 * Unlinking is a remplacement for delete operations.
 */
open class UnlinkBlocks(private val repo: BlockRepository) :
    BaseUseCase<Payload, UnlinkBlocks.Params>() {

    override suspend fun run(params: Params) = try {
        repo.unlink(
            command = Command.Unlink(
                context = params.context,
                targets = params.targets
            )
        ).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * Params for unlinking a set of blocks from its context (i.e. page)
     * @property context context id
     * @property targets ids of the blocks, which we need to unlink from the [context]
     */
    data class Params(
        val context: Id,
        val targets: List<Id>
    )
}