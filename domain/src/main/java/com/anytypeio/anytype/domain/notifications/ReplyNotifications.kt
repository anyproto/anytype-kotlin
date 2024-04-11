package com.anytypeio.anytype.domain.notifications

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class ReplyNotifications @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers,
): ResultInteractor<List<Id>, Unit>(dispatchers.io) {
    override suspend fun doWork(params: List<Id>) {
        repo.replyNotifications(notifications = params)
    }
}