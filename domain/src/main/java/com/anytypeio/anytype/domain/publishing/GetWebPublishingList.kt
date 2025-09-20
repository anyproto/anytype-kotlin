package com.anytypeio.anytype.domain.publishing

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.publishing.Publishing
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetWebPublishingList @Inject constructor(
    private val repository: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<GetWebPublishingList.Params, List<Publishing.State>>(dispatchers.io) {

    override suspend fun doWork(params: Params): List<Publishing.State> {
        val command = Command.Publishing.GetList(
            space = params.space
        )
        return repository.publishingGetList(command)
    }

    data class Params(
        val space: SpaceId?
    )
}