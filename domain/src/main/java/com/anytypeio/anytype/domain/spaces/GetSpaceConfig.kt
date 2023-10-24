package com.anytypeio.anytype.domain.spaces

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetSpaceConfig @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<GetSpaceConfig.Params, Config>(dispatchers.io) {

    override suspend fun doWork(params: Params) = repo.getSpaceConfig(space = params.space)

    class Params(val space: Id)
}