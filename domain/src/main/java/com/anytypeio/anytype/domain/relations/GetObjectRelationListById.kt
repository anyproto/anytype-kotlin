package com.anytypeio.anytype.domain.relations

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelationListWithValueItem
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.relations.GetObjectRelationListById.Params
import javax.inject.Inject

class GetObjectRelationListById @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Params, List<RelationListWithValueItem>>(dispatchers.io) {

    override suspend fun doWork(params: Params): List<RelationListWithValueItem> {
        val command = Command.RelationListWithValue(
            space = params.space,
            value = params.value
        )
        return repo.objectRelationListWithValue(command)
    }

    data class Params(val space: SpaceId, val value: Id)
}