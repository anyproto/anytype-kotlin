package com.anytypeio.anytype.domain.table

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class DeleteTableColumn(
    private val repo: BlockRepository
) : BaseUseCase<Payload, DeleteTableColumn.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Payload> = safe {
        repo.deleteTableColumn(
            ctx = params.ctx,
            targetId = params.target
        )
    }

    /**
     * @param ctx - id of the context object
     * @param target - id of the column to delete
     */
    data class Params(
        val ctx: Id,
        val target: Id
    )
}