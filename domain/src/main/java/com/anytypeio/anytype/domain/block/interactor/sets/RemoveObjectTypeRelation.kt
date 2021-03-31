package com.anytypeio.anytype.domain.block.interactor.sets

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Url

class RemoveObjectTypeRelation : BaseUseCase<List<Relation>, RemoveObjectTypeRelation.Params>() {

    override suspend fun run(params: Params): Either<Throwable, List<Relation>> {
        TODO("Not yet implemented")
    }

    data class Params(
        val objectType: Url,
        val relationKey: String
    )
}