package com.anytypeio.anytype.persistence.networkmode

import android.content.SharedPreferences
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_APP_FILE_PATH_PREF
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_PREF
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_USER_FILE_PATH_PREF

interface NetworkModeProvider {
    fun set(networkModeConfig: NetworkModeConfig)
    fun get(): NetworkModeConfig
}

class DefaultNetworkModeProvider(private val sharedPreferences: SharedPreferences) :
    NetworkModeProvider {

    override fun set(networkModeConfig: NetworkModeConfig) {
        val (userFilePath, storedFilePath) = if (networkModeConfig.networkMode == NetworkMode.CUSTOM) {
            networkModeConfig.userFilePath to networkModeConfig.storedFilePath
        } else {
            null to null
        }

        sharedPreferences.edit().apply {
            putString(NETWORK_MODE_PREF, networkModeConfig.networkMode.value)
            putString(NETWORK_MODE_USER_FILE_PATH_PREF, userFilePath)
            putString(NETWORK_MODE_APP_FILE_PATH_PREF, storedFilePath)
            apply()
        }
    }

    override fun get(): NetworkModeConfig {
        val networkMode = when (sharedPreferences.getString(NETWORK_MODE_PREF, NetworkMode.DEFAULT.value)) {
            NetworkMode.DEFAULT.value -> NetworkMode.DEFAULT
            NetworkMode.LOCAL.value -> NetworkMode.LOCAL
            NetworkMode.CUSTOM.value -> NetworkMode.CUSTOM
            else -> NetworkMode.DEFAULT
        }
        return if (networkMode == NetworkMode.CUSTOM) {
            val userFilePath = sharedPreferences.getString(
                NETWORK_MODE_USER_FILE_PATH_PREF, null
            )
            val storedFilePath = sharedPreferences.getString(
                NETWORK_MODE_APP_FILE_PATH_PREF, null
            )
            NetworkModeConfig(networkMode, userFilePath, storedFilePath)
        } else {
            NetworkModeConfig(networkMode, null, null)
        }
    }
}