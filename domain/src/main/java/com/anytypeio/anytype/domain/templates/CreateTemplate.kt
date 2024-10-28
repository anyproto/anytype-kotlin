package com.anytypeio.anytype.domain.templates

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.CreateObjectResult
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class CreateTemplate @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateTemplate.Params, CreateObjectResult>(dispatchers.io) {

    override suspend fun doWork(params: Params): CreateObjectResult {
        val command = Command.CreateObject(
            template = null,
            prefilled = mapOf(Relations.TARGET_OBJECT_TYPE to params.targetObjectTypeId),
            internalFlags = emptyList(),
            space = params.spaceId,
            typeKey = TypeKey(ObjectTypeUniqueKeys.TEMPLATE)
        )
        val result = repo.createObject(command)
        return result
    }

    data class Params(
        val targetObjectTypeId: Id,
        val spaceId: SpaceId
    )
}