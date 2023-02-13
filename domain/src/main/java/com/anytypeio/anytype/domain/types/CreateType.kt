package com.anytypeio.anytype.domain.types

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import kotlinx.coroutines.Dispatchers

class CreateType(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateType.Params, ObjectWrapper.Type>(dispatchers.io) {

    class Params(val name: String)

    override suspend fun doWork(params: Params): ObjectWrapper.Type {
        return repo.createType(params.name)
    }

}