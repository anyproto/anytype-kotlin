package com.anytypeio.anytype.domain.templates

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject

class CreateTemplate @Inject constructor(
    private val repo: BlockRepository,
    private val spaceManager: SpaceManager,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateTemplate.Params, Id>(dispatchers.io) {

    override suspend fun doWork(params: Params): Id {
        val command = Command.CreateObject(
            template = null,
            prefilled = mapOf(
                Relations.TARGET_OBJECT_TYPE to params.targetObjectTypeId
            ),
            internalFlags = emptyList(),
            space = SpaceId(spaceManager.get()),
            type = TypeKey(ObjectTypeUniqueKeys.TEMPLATE)
        )
        val result = repo.createObject(command)
        return result.id
    }

    data class Params(val targetObjectTypeId: Id)
}