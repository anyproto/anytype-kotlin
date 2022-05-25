package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetObjectType(
    private val repo: BlockRepository
) : BaseUseCase<Payload, SetObjectType.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Payload> = safe {
        repo.setObjectTypeToObject(
            ctx = params.context,
            typeId = params.typeId
        )
    }

    class Params(
        val context: Id,
        val typeId: Id
    )
}