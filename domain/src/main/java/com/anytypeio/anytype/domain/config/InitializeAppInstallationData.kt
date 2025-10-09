package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

/**
 * Use case for initializing and tracking app installation data.
 *
 * This use case:
 * 1. Checks if installedAtDate exists (to determine if this is a fresh install)
 * 2. If null, stores the current timestamp as installedAtDate
 * 3. Determines if this is the first app launch
 * 4. Updates the app version tracking (previous and current)
 *
 * Returns [AppInstallationData] with information about the launch state.
 */
class InitializeAppInstallationData @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<InitializeAppInstallationData.Params, AppInstallationData>(dispatchers.io) {

    override suspend fun doWork(params: Params): AppInstallationData {
        val account = params.account
        val currentVersion = params.currentAppVersion

        // Check if installedAtDate exists
        val installedAtDate = userSettingsRepository.getInstalledAtDate(account)
        val isFirstLaunch = installedAtDate == null

        // If this is first launch, store the current timestamp
        val timestamp = if (isFirstLaunch) {
            val now = System.currentTimeMillis()
            userSettingsRepository.setInstalledAtDate(account, now)
            now
        } else {
            installedAtDate
        }

        // Get previous app version
        val previousVersion = userSettingsRepository.getCurrentAppVersion(account)

        // Update version tracking
        if (previousVersion != currentVersion) {
            // Move current to previous
            previousVersion?.let {
                userSettingsRepository.setPreviousAppVersion(account, it)
            }
            // Set new current version
            userSettingsRepository.setCurrentAppVersion(account, currentVersion)
        }

        return AppInstallationData(
            isFirstLaunch = isFirstLaunch,
            currentVersion = currentVersion,
            previousVersion = previousVersion,
            installedAtTimestamp = timestamp
        )
    }

    data class Params(
        val account: Account,
        val currentAppVersion: String
    )
}
