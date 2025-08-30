package com.anytypeio.anytype.domain.publishing

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class RemovePublishing @Inject constructor(
    private val repository: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<RemovePublishing.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        val command = Command.Publishing.Remove(
            space = params.space,
            objectId = params.objectId
        )
        repository.publishingRemove(command)
    }

    data class Params(
        val space: SpaceId,
        val objectId: Id
    )
}
