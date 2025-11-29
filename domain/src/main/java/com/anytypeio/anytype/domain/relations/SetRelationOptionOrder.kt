package com.anytypeio.anytype.domain.relations

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class SetRelationOptionOrder @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SetRelationOptionOrder.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        val command = Command.SetRelationOptionsOrder(
            space = params.spaceId,
            relationKey = params.relationKey,
            orderedIds = params.orderedIds
        )
        repo.setRelationOptionOrder(
            command
        )
    }

    data class Params(
        val spaceId: SpaceId,
        val relationKey: RelationKey,
        val orderedIds: List<Id>
    )
}
