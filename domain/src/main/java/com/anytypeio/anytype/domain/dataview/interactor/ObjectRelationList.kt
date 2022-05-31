package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class ObjectRelationList(
    private val repo: BlockRepository
) : BaseUseCase<List<Relation>, ObjectRelationList.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.relationListAvailable(ctx = params.ctx)
            .let {
                if (params.sorted) {
                    it.sortedBy { it.name }
                } else {
                    it
                }
            }
    }

    class Params(
        val ctx: Id,
        val sorted: Boolean = true
    )
}