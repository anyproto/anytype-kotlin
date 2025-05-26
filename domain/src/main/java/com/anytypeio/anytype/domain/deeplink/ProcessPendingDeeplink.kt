package com.anytypeio.anytype.domain.deeplink

import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import javax.inject.Inject

/**
 * Use case for processing pending invite deeplinks when user becomes authenticated.
 * This handles the scenario where user receives an invite deeplink while not logged in,
 * and processes it once they log in.
 */
class ProcessPendingDeeplink @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val checkAuthorizationStatus: CheckAuthorizationStatus,
    private val deepLinkResolver: DeepLinkResolver,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, DeepLinkResolver.Action?>(dispatchers.io) {

    override suspend fun doWork(params: Unit): DeepLinkResolver.Action? {
        // Get pending deeplink
        val pendingDeeplink = userSettingsRepository.getPendingInviteDeeplink()

        if (!pendingDeeplink.isNullOrEmpty()) {
            // Clear the pending deeplink first to prevent reprocessing
            userSettingsRepository.clearPendingInviteDeeplink()

            // Resolve and return the deeplink action
            val action = deepLinkResolver.resolve(pendingDeeplink)
            return action
        }
        return null
    }
} 