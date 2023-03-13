package com.anytypeio.anytype.domain.sets

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetQueryToObjectSet(private val repo: BlockRepository, dispatchers: AppCoroutineDispatchers) :
    ResultInteractor<SetQueryToObjectSet.Params, Payload>(dispatchers.io) {

    override suspend fun doWork(params: Params): Payload {
        val command = Command.SetQueryToSet(
            ctx = params.ctx,
            query = params.query
        )
        return repo.setQueryToSet(command)
    }

    class Params(
        val ctx: Id,
        val query: Id
    )
}