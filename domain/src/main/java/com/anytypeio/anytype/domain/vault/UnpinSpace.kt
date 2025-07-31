package com.anytypeio.anytype.domain.vault

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class UnpinSpace @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<UnpinSpace.Params, Unit>(dispatchers.io) {

    data class Params(
        val spaceId: Id
    )

    override suspend fun doWork(params: Params): Unit {
        repo.spaceUnsetOrder(
            spaceViewId = params.spaceId
        )
    }
} 