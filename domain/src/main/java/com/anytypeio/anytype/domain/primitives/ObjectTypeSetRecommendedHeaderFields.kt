package com.anytypeio.anytype.domain.primitives

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class ObjectTypeSetRecommendedHeaderFields @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<ObjectTypeSetRecommendedHeaderFields.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        val command = Command.ObjectTypeSetRecommendedHeaderFields(
            objectTypeId = params.objectTypeId,
            fields = params.fields
        )
        repo.objectTypeSetRecommendedHeaderFields(command)
    }

    data class Params(
        val objectTypeId: String,
        val fields: List<String>
    )
}