package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

/**
 * Generic use case to determine if a feature introduction should be shown.
 *
 * This handles the common pattern:
 * - Fresh installs: Skip introduction (feature isn't "new" for them)
 * - Existing users who updated: Show introduction (feature is new for them)
 * - Already shown: Don't show again
 *
 * @param featureType The type of feature introduction to check
 */
class ShouldShowFeatureIntroduction @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val initializeAppInstallationData: InitializeAppInstallationData,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<ShouldShowFeatureIntroduction.Params, ShouldShowFeatureIntroduction.Result>(dispatchers.io) {

    override suspend fun doWork(params: Params): Result {
        val account = params.account
        val featureType = params.featureType

        // Check if already shown
        val hasShownBefore = when (featureType) {
            FeatureType.SPACES_INTRODUCTION ->
                userSettingsRepository.getHasShownSpacesIntroduction()
            FeatureType.CREATE_SPACE_BADGE ->
                userSettingsRepository.getHasSeenCreateSpaceBadge()
        }

        if (hasShownBefore) {
            return Result.AlreadyShown
        }

        // Initialize installation data to check if this is a fresh install
        val installData = initializeAppInstallationData.run(
            InitializeAppInstallationData.Params(
                account = account,
                currentAppVersion = params.currentAppVersion
            )
        )

        return if (installData.isFirstLaunch) {
            // Fresh install - feature is not new for them
            Result.SkipForFreshInstall
        } else {
            // Existing user who updated - should see introduction
            Result.ShouldShow
        }
    }

    data class Params(
        val account: Account,
        val currentAppVersion: String,
        val featureType: FeatureType
    )

    sealed class Result {
        /** Should show the introduction to existing users */
        object ShouldShow : Result()

        /** Already shown before, don't show again */
        object AlreadyShown : Result()

        /** Fresh install, skip introduction */
        object SkipForFreshInstall : Result()

        /** Error occurred, safe default is to not show */
        data class Error(val throwable: Throwable) : Result()

        /** Helper to check if we should actually show */
        fun shouldDisplay(): Boolean = this is ShouldShow
    }

    enum class FeatureType {
        SPACES_INTRODUCTION,
        CREATE_SPACE_BADGE
    }
}
