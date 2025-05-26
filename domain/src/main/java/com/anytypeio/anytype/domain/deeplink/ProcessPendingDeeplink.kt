package com.anytypeio.anytype.domain.deeplink

import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.base.BaseUseCase
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
    private val deepLinkResolver: DeepLinkResolver
) : BaseUseCase<DeepLinkResolver.Action?, Unit>() {

    override suspend fun run(params: Unit) = safe {
        // Check if user is authenticated
        val authStatus = checkAuthorizationStatus.run(Unit).fold(
            fnL = { return@safe null },
            fnR = { it }
        )

        if (authStatus != AuthStatus.AUTHORIZED) {
            return@safe null
        }

        // Get pending deeplink
        val pendingDeeplink = userSettingsRepository.getPendingInviteDeeplink()

        if (!pendingDeeplink.isNullOrEmpty()) {
            // Clear the pending deeplink first to prevent reprocessing
            userSettingsRepository.clearPendingInviteDeeplink()
            
            // Resolve and return the deeplink action
            val action = deepLinkResolver.resolve(pendingDeeplink)
            return@safe action
        }

        null
    }
} 