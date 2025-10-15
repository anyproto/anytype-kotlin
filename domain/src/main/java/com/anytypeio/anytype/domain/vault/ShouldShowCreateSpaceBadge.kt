package com.anytypeio.anytype.domain.vault

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.ShouldShowFeatureIntroduction
import com.anytypeio.anytype.domain.debugging.Logger
import javax.inject.Inject

/**
 * Determines whether the blue dot badge should be shown on the "Create a new space" button.
 * Uses the same logic as the spaces introduction:
 * - Fresh installs: Don't show badge (not new for them)
 * - Existing users: Show badge once
 * - Already shown: Don't show again
 *
 * This is independent of the spaces introduction popup.
 */
class ShouldShowCreateSpaceBadge @Inject constructor(
    private val shouldShowFeatureIntroduction: ShouldShowFeatureIntroduction,
    private val auth: AuthRepository,
    dispatchers: AppCoroutineDispatchers,
    private val logger: Logger
) : ResultInteractor<ShouldShowCreateSpaceBadge.Params, Boolean>(dispatchers.io) {

    override suspend fun doWork(params: Params): Boolean {
        val account = auth.getCurrentAccount()

        logger.logInfo("DROID-3864, Checking if badge should show for account: ${account.id}")

        val result = shouldShowFeatureIntroduction.run(
            ShouldShowFeatureIntroduction.Params(
                account = account,
                currentAppVersion = params.currentAppVersion,
                featureType = ShouldShowFeatureIntroduction.FeatureType.CREATE_SPACE_BADGE
            )
        )

        val shouldShow = result.shouldDisplay()
        logger.logInfo("DROID-3864, Should show badge: $shouldShow, result: $result")

        return shouldShow
    }

    data class Params(
        val currentAppVersion: String
    )
}