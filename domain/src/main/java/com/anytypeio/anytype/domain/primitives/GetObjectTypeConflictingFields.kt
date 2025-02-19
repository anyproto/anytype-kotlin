package com.anytypeio.anytype.domain.primitives

import com.anytypeio.anytype.core_models.Command.ObjectTypeConflictingFields
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetObjectTypeConflictingFields @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<GetObjectTypeConflictingFields.Params, List<Id>>(dispatchers.io) {

    override suspend fun doWork(params: Params): List<Id> {
        val command = ObjectTypeConflictingFields(
            spaceId = params.spaceId,
            objectTypeId = params.objectTypeId
        )
        return repo.objectTypeListConflictingRelations(command = command)
    }

    data class Params(
        val spaceId: String,
        val objectTypeId: String
    )
}