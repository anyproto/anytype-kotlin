package com.anytypeio.anytype.domain.config

/**
 * Data class representing information about app installation and launch state
 *
 * @property isFirstLaunch True if this is the first time the app has been launched
 * @property currentVersion The current version of the application
 * @property previousVersion The previous version of the application, or null if first launch
 * @property installedAtTimestamp Unix timestamp (milliseconds) when the app was first installed
 */
data class AppInstallationData(
    val isFirstLaunch: Boolean,
    val currentVersion: String,
    val previousVersion: String?,
    val installedAtTimestamp: Long
)
