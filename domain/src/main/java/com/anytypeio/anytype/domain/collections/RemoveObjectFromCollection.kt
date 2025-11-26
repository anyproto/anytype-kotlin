package com.anytypeio.anytype.domain.collections

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

/**
 * Use case to remove one or more objects from a collection.
 */
class RemoveObjectFromCollection @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<RemoveObjectFromCollection.Params, Payload>(dispatchers.io) {

    override suspend fun doWork(params: Params): Payload {
        val command = Command.RemoveObjectFromCollection(
            ctx = params.collectionId,
            ids = params.objectIdsToRemove
        )
        return repo.removeObjectFromCollection(command)
    }

    /**
     * @param collectionId The ID of the collection from which objects will be removed.
     * @param objectIdsToRemove The list of object IDs to remove from the collection.
     */
    data class Params(
        val collectionId: Id,
        val objectIdsToRemove: List<Id>
    )
}