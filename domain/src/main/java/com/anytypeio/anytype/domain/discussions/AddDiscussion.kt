package com.anytypeio.anytype.domain.discussions

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class AddDiscussion @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Id, Id>(dispatchers.io) {

    override suspend fun doWork(params: Id): Id {
        return repo.addDiscussion(params)
    }
}
