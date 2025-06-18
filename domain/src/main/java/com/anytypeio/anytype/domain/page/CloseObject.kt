package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

/**
 * Use-case for closing a smart block by id.
 */
open class CloseObject @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CloseObject.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repo.closeObject(
            id = params.target,
            space = params.space
        )
    }

    data class Params(
        val target: Id,
        val space: SpaceId
    )
}