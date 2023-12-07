package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_CUSTOM
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_DEFAULT
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_LOCAL


enum class NetworkMode(val value: String) {
    DEFAULT(NETWORK_MODE_DEFAULT), LOCAL(NETWORK_MODE_LOCAL), CUSTOM(NETWORK_MODE_CUSTOM)
}

data class NetworkModeConfig(
    val networkMode: NetworkMode = NetworkMode.DEFAULT,
    val userFilePath: String? = null,
    val storedFilePath: String? = null
)

object NetworkModeConstants {
    const val NETWORK_MODE_PREF = "pref.network_mode"
    const val NETWORK_MODE_APP_FILE_PATH_PREF = "pref.network_config_file_path"
    const val NETWORK_MODE_USER_FILE_PATH_PREF = "pref.network_mode_user_config_file_path"

    const val NETWORK_MODE_LOCAL = "local"
    const val NETWORK_MODE_DEFAULT = "default"
    const val NETWORK_MODE_CUSTOM = "custom"
}
