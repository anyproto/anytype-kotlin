package com.anytypeio.anytype.domain.deeplink

import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import javax.inject.Inject

/**
 * Use case for saving invite deeplinks when user is not authenticated.
 * This allows processing the deeplink later when the user logs in.
 */
class SavePendingDeeplink @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val checkAuthorizationStatus: CheckAuthorizationStatus,
    private val deepLinkResolver: DeepLinkResolver
) : BaseUseCase<Boolean, SavePendingDeeplink.Params>() {

    override suspend fun run(params: Params) = safe {
        // Check if user is authenticated
        val authStatus = checkAuthorizationStatus.run(Unit).fold(
            fnL = { return@safe false },
            fnR = { it }
        )

        // Only save if user is not authenticated
        if (authStatus == AuthStatus.AUTHORIZED) {
            return@safe false
        }

        // Check if this is an invite deeplink
        val action = deepLinkResolver.resolve(params.deeplink)
        if (action is DeepLinkResolver.Action.Invite) {
            userSettingsRepository.setPendingInviteDeeplink(params.deeplink)
            return@safe true
        }

        false
    }

    data class Params(val deeplink: String)
} 