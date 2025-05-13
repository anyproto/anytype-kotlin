package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class CreateObjectFromUrl @Inject constructor(
    private val repository: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Url, ObjectWrapper.Basic>(dispatchers.io) {

    override suspend fun doWork(params: Url): ObjectWrapper.Basic {
        return repository.createObjectFromUrl(url = params)
    }
} 