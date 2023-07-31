package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetObjectInternalFlags(
        private val repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SetObjectInternalFlags.Params, Payload>(dispatchers.io) {

    override suspend fun doWork(params: Params): Payload {
        val command = Command.SetInternalFlags(
                ctx = params.ctx,
                flags = params.flags
        )
        return repo.setInternalFlags(command)
    }

    class Params(
            val ctx: Id,
            val flags: List<InternalFlags>
    )
}