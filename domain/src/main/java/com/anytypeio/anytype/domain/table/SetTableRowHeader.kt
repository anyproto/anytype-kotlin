package com.anytypeio.anytype.domain.table

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetTableRowHeader(
    private val repo: BlockRepository
) : BaseUseCase<Payload, SetTableRowHeader.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Payload> = safe {
        repo.setTableRowHeader(
            ctx = params.ctx,
            targetId = params.row,
            isHeader = params.isHeader
        )
    }

    data class Params(
        val ctx: Id,
        val row: Id,
        val isHeader: Boolean
    )
}