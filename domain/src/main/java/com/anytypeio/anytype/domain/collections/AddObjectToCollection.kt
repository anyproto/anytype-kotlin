package com.anytypeio.anytype.domain.collections

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class AddObjectToCollection(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<AddObjectToCollection.Params, Payload>(dispatchers.io) {

    override suspend fun doWork(params: Params): Payload {
        val command = Command.AddObjectToCollection(
            ctx = params.ctx,
            afterId = params.after,
            ids = params.targets
        )
        return repo.addObjectToCollection(command)
    }

    class Params(
        val ctx: Id,
        val after: Id,
        val targets: List<Id>
    )
}