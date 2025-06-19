package com.anytypeio.anytype.domain.notifications

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

/**
 * Use case for setting notification mode for a specific space.
 */
class SetSpaceNotificationMode @Inject constructor(
    private val repository: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SetSpaceNotificationMode.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repository.setSpaceNotificationMode(
            spaceViewId = params.spaceViewId,
            mode = params.mode
        )
    }

    data class Params(
        val spaceViewId: Id,
        val mode: NotificationState
    )
} 