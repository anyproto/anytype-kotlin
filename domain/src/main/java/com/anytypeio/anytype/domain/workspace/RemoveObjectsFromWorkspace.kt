package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class RemoveObjectsFromWorkspace(
    private val repo: BlockRepository
) : ResultInteractor<RemoveObjectsFromWorkspace.Params, List<Id>>() {

    /**
     * Removes objects from workspace by ids
     * @param params - objects to remove by ids
     * @return list of removed object ids
     */
    override suspend fun doWork(params: Params): List<Id> =
        repo.removeObjectFromWorkspace(objects = params.objects)

    data class Params(val objects: List<Id>)
}