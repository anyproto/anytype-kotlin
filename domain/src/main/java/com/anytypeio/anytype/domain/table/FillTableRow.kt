package com.anytypeio.anytype.domain.table

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class FillTableRow(
    private val repo: BlockRepository
) : BaseUseCase<Payload, FillTableRow.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Payload> = safe {
        repo.fillTableRow(
            ctx = params.ctx,
            targetIds = params.targetIds
        )
    }

    /**
     * @property [targetIds] the list of rows that need to be filled in
     */
    data class Params(
        val ctx: Id,
        val targetIds: List<Id>
    )
}