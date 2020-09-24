package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.model.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.event.model.Payload

/**
 * Use-case for block duplication.
 * Should return id of the new block.
 */
open class DuplicateBlock(
    private val repo: BlockRepository
) : BaseUseCase<Pair<Id, Payload>, DuplicateBlock.Params>() {

    override suspend fun run(params: Params) = try {
        repo.duplicate(
            command = Command.Duplicate(
                context = params.context,
                original = params.original
            )
        ).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * @property context context id
     * @property original id of the original block id, which we need to duplicate
     */
    data class Params(
        val context: Id,
        val original: Id
    )
}