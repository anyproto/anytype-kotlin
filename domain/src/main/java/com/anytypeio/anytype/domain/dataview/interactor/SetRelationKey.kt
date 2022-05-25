package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetRelationKey(
    private val repo: BlockRepository
) : BaseUseCase<Payload, SetRelationKey.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Payload> = safe {
        repo.setRelationKey(
            Command.SetRelationKey(
                contextId = params.contextId,
                blockId = params.blockId,
                key = params.key
            )
        )
    }

    class Params(
        val contextId: Id,
        val blockId: Id,
        val key: Id
    )
}