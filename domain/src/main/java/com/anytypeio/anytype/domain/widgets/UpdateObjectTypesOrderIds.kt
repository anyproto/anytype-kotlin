package com.anytypeio.anytype.domain.widgets

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class UpdateObjectTypesOrderIds @Inject constructor(
    private val repository: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<UpdateObjectTypesOrderIds.Params, List<Id>>(dispatchers.io) {

    override suspend fun doWork(params: Params): List<Id> {
        val command = Command.ObjectTypesSetOrder(
            spaceId = params.spaceId.id,
            orderedIds = params.orderedIds
        )
        return repository.objectTypesSetOrder(command)
    }

    data class Params(
        val spaceId: SpaceId,
        val orderedIds: List<String>
    )
}