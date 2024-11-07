package com.anytypeio.anytype.domain.spaces

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class CreateSpace @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateSpace.Params, Id>(dispatchers.io) {

    override suspend fun doWork(params: Params): Id = repo.createWorkspace(
        details = params.details,
        withChat = params.withChat
    )

    data class Params(val details: Struct, val withChat: Boolean = true)
}