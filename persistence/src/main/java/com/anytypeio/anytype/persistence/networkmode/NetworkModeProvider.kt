package com.anytypeio.anytype.persistence.networkmode

import android.content.SharedPreferences
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_APP_FILE_PATH_PREF
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_CUSTOM
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_DEFAULT
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_LOCAL
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_PREF
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_USER_FILE_PATH_PREF

interface NetworkModeProvider {
    fun get(): NetworkModeConfig
}

class DefaultNetworkModeProvider(private val sharedPreferences: SharedPreferences) :
    NetworkModeProvider {

    override fun get(): NetworkModeConfig {
        val networkMode = when (sharedPreferences.getString(NETWORK_MODE_PREF, null)) {
            NETWORK_MODE_DEFAULT -> NetworkMode.DEFAULT
            NETWORK_MODE_LOCAL -> NetworkMode.LOCAL
            NETWORK_MODE_CUSTOM -> NetworkMode.CUSTOM
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