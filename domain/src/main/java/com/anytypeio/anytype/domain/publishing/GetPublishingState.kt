package com.anytypeio.anytype.domain.publishing

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.publishing.Publishing
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetPublishingState @Inject constructor(
    private val repository: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<GetPublishingState.Params, Publishing.State?>(dispatchers.io) {

    override suspend fun doWork(params: Params): Publishing.State? {
        val command = Command.Publishing.GetStatus(
            space = params.space,
            objectId = params.objectId
        )
        return repository.publishingGetStatus(command)
    }

    data class Params(
        val space: SpaceId,
        val objectId: Id
    )
}
