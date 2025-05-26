package com.anytypeio.anytype.domain.deeplink

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import javax.inject.Inject

/**
 * Use case for saving invite deeplinks when user is not authenticated.
 * This allows processing the deeplink later when the user logs in.
 */
class SavePendingDeeplink @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val deepLinkResolver: DeepLinkResolver,
    private val logger: Logger,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SavePendingDeeplink.Params, Boolean>(dispatchers.io) {

    override suspend fun doWork(params: Params): Boolean {
        logger.logInfo("Saving pending deeplink: ${params.deeplink}")
        val action = deepLinkResolver.resolve(params.deeplink)
        if (action is DeepLinkResolver.Action.Invite) {
            logger.logInfo("Deeplink is an invite, saving to user settings")
            userSettingsRepository.setPendingInviteDeeplink(params.deeplink)
            return true
        } else {
            logger.logInfo("Deeplink is not an invite, not saving")
            return false
        }
    }

    data class Params(val deeplink: String)
} 