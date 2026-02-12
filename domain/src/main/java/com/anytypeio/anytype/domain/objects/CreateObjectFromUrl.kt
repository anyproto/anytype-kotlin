package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class CreateObjectFromUrl @Inject constructor(
    private val repository: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateObjectFromUrl.Params, ObjectWrapper.Basic>(dispatchers.io) {

    override suspend fun doWork(params: Params): ObjectWrapper.Basic {
        return repository.createObjectFromUrl(
            space = params.space,
            url = params.url,
            createdInContext = params.createdInContext
        )
    }

    data class Params(
        val space: SpaceId,
        val url: Url,
        val createdInContext: Id? = null
    )
}