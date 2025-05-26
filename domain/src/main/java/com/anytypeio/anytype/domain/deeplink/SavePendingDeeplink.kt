package com.anytypeio.anytype.domain.deeplink

import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
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
    private val deepLinkResolver: DeepLinkResolver,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SavePendingDeeplink.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {

        checkAuthorizationStatus(Unit).process(
            failure = {
                // Check if this is an invite deeplink
                val action = deepLinkResolver.resolve(params.deeplink)
                if (action is DeepLinkResolver.Action.Invite) {
                    userSettingsRepository.setPendingInviteDeeplink(params.deeplink)
                }
                //do nothing, assume unauthorized
            },
            success = { status ->
                if (status == AuthStatus.UNAUTHORIZED) {
                    // Check if this is an invite deeplink
                    val action = deepLinkResolver.resolve(params.deeplink)
                    if (action is DeepLinkResolver.Action.Invite) {
                        userSettingsRepository.setPendingInviteDeeplink(params.deeplink)
                    }
                }
            }
        )
    }

    data class Params(val deeplink: String)
} 