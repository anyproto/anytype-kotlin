package com.anytypeio.anytype.domain.templates

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class CreateTemplate @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateTemplate.Params, Id>(dispatchers.io) {

    override suspend fun doWork(params: Params): Id {
        val command = Command.CreateObject(
            template = null,
            prefilled = mapOf(
                Relations.TYPE to ObjectTypeIds.TEMPLATE,
                Relations.TARGET_OBJECT_TYPE to params.targetObjectTypeId
            ),
            internalFlags = emptyList()
        )
        val result = repo.createObject(command)
        return result.id
    }

    data class Params(val targetObjectTypeId: Id)
}