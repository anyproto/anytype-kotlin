package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.FlowInteractor
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.vault.SetSpacesIntroductionShown
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

/**
 * Observes whether to show Spaces Introduction screen.
 *
 * Automatically waits for account start, then checks:
 * - If this is a fresh install (skip showing, mark as shown)
 * - If already shown before (skip showing)
 * - If existing user who hasn't seen it (show)
 *
 * Emits a single boolean value and completes.
 *
 * Pattern follows [ObserveVaultSettings] for consistency.
 */
class ObserveShowSpacesIntroduction @Inject constructor(
    private val awaitAccountStart: AwaitAccountStartManager,
    private val initializeAppInstallationData: InitializeAppInstallationData,
    private val shouldShowFeatureIntroduction: ShouldShowFeatureIntroduction,
    private val setSpacesIntroductionShown: SetSpacesIntroductionShown,
    private val logger: Logger,
    dispatchers: AppCoroutineDispatchers
) : FlowInteractor<ObserveShowSpacesIntroduction.Params, Boolean>(dispatchers.io) {

    data class Params(val currentAppVersion: String, val account: Account)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun build(params: Params): Flow<Boolean> = awaitAccountStart
        .awaitStart()
        .flatMapLatest {
            flow {
                // Get current account
                val account: Account = params.account

                // Initialize installation data (records first launch timestamp if needed)
                initializeAppInstallationData.run(
                    InitializeAppInstallationData.Params(
                        account = account,
                        currentAppVersion = params.currentAppVersion
                    )
                )

                // Check if should show
                val result = shouldShowFeatureIntroduction.run(
                    ShouldShowFeatureIntroduction.Params(
                        account = account,
                        currentAppVersion = params.currentAppVersion,
                        featureType = ShouldShowFeatureIntroduction.FeatureType.SPACES_INTRODUCTION
                    )
                )

                // Mark as shown for fresh installs so they never see it
                if (result is ShouldShowFeatureIntroduction.Result.SkipForFreshInstall) {
                    setSpacesIntroductionShown.async(Unit)
                }

                emit(result.shouldDisplay())
            }
        }
        .catch { e ->
            logger.logException(e, "Error checking spaces introduction")
            emit(false) // Don't show on error
        }

    override fun build(): Flow<Boolean> {
        throw IllegalStateException("Use build(params) instead")
    }
}
