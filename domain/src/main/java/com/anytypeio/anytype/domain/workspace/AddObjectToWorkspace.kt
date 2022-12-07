package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for adding one or more object to the current workspace.
 * Returns of list of ids of object added to workspace.
 */
class AddObjectToWorkspace(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
): BaseUseCase<List<Id>, AddObjectToWorkspace.Params>(dispatchers.io) {

    override suspend fun run(params: Params) = safe {
        repo.addObjectToWorkspace(objects = params.objects)
    }

    data class Params(val objects: List<Id>)
}