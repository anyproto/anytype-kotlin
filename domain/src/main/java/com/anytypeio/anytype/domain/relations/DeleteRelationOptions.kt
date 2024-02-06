package com.anytypeio.anytype.domain.relations

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class DeleteRelationOptions @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<DeleteRelationOptions.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        val command = Command.DeleteRelationOptions(optionIds = params.optionIds)
        repo.deleteRelationOption(command)
    }

    data class Params(val optionIds: List<Id>)
}