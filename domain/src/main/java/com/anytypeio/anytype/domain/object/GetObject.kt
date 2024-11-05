package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for opening an object as preview — without subscribing to its subsequent changes.
 * If you want to receive payload events, you should use [OpenObject] instead.
 */
class GetObject(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<GetObject.Params, ObjectView>(dispatchers.io) {
    override suspend fun doWork(params: Params): ObjectView = repo.getObject(
        id = params.target,
        space = params.space
    )

    data class Params(
        val target: Id,
        val space: SpaceId
    )
}