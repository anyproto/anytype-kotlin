package com.anytypeio.anytype.domain.primitives

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class ObjectTypeSetRecommendedFields @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<ObjectTypeSetRecommendedFields.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        val command = Command.ObjectTypeSetRecommendedFields(
            objectTypeId = params.objectTypeId,
            fields = params.fields
        )
        repo.objectTypeSetRecommendedFields(command)
    }

    data class Params(
        val objectTypeId: String,
        val fields: List<String>
    )
}