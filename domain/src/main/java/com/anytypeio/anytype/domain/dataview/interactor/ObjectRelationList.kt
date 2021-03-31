package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class ObjectRelationList(
    private val repo: BlockRepository
) : BaseUseCase<List<Relation>, ObjectRelationList.Params>() {

    override suspend fun run(params: Params): Either<Throwable, List<Relation>> = safe {
        repo.relationListAvailable(
            ctx = params.ctx
        )
    }

    class Params(
        val ctx: Id
    )
}