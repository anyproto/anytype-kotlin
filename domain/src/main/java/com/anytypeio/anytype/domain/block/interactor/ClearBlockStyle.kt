package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class ClearBlockStyle(
    private val repository: BlockRepository,
) : BaseUseCase<Payload, ClearBlockStyle.Params>() {

    data class Params(
        val ctx: Id,
        val blockIds: List<Id>
    )

    override suspend fun run(params: Params): Either<Throwable, Payload> = safe {
        repository.clearBlockStyle(
            ctx = params.ctx,
            blockIds = params.blockIds
        )
    }
}