package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import kotlinx.coroutines.Dispatchers

class ObjectRelationList(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers = AppCoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main
    )
) : BaseUseCase<List<Relation>, ObjectRelationList.Params>(context = dispatchers.io) {

    override suspend fun run(params: Params) = safe {
        val relations = repo.relationListAvailable(ctx = params.ctx)
        if (params.sorted) {
            relations.sortedBy { it.name }
        } else {
            relations
        }
    }

    class Params(
        val ctx: Id,
        val sorted: Boolean = true
    )
}