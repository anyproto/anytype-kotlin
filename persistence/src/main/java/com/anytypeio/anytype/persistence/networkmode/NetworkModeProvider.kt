package com.anytypeio.anytype.persistence.networkmode

import android.content.SharedPreferences
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_CUSTOM
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_DEFAULT
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_LOCAL
import com.anytypeio.anytype.persistence.networkmode.DefaultNetworkModeProvider.NetworkModeConstants.NETWORK_MODE_APP_FILE_PATH_PREF
import com.anytypeio.anytype.persistence.networkmode.DefaultNetworkModeProvider.NetworkModeConstants.NETWORK_MODE_PREF
import com.anytypeio.anytype.persistence.networkmode.DefaultNetworkModeProvider.NetworkModeConstants.NETWORK_MODE_USER_FILE_PATH_PREF
import com.anytypeio.anytype.persistence.networkmode.DefaultNetworkModeProvider.NetworkModeConstants.USE_RESERVE_MULTIPLEX_LIBRARY_PREF

interface NetworkModeProvider {
    fun set(networkModeConfig: NetworkModeConfig)
    fun get(): NetworkModeConfig
    fun clear()
}

class DefaultNetworkModeProvider(private val sharedPreferences: SharedPreferences) :
    NetworkModeProvider {

    override fun set(networkModeConfig: NetworkModeConfig) {
        val (userFilePath, storedFilePath) = if (networkModeConfig.networkMode == NetworkMode.CUSTOM) {
            networkModeConfig.userFilePath to networkModeConfig.storedFilePath
        } else {
            null to null
        }

        val modeValue= when (networkModeConfig.networkMode) {
            NetworkMode.DEFAULT -> NETWORK_MODE_DEFAULT
            NetworkMode.LOCAL -> NETWORK_MODE_LOCAL
            NetworkMode.CUSTOM -> NETWORK_MODE_CUSTOM
        }

        sharedPreferences.edit().apply {
            putString(NETWORK_MODE_PREF, modeValue)
            putString(NETWORK_MODE_USER_FILE_PATH_PREF, userFilePath)
            putString(NETWORK_MODE_APP_FILE_PATH_PREF, storedFilePath)
            putBoolean(USE_RESERVE_MULTIPLEX_LIBRARY_PREF, networkModeConfig.useReserveMultiplexLib)
            apply()
        }
    }

    override fun get(): NetworkModeConfig {
        val useReserveMultiplexLib = sharedPreferences.getBoolean(USE_RESERVE_MULTIPLEX_LIBRARY_PREF, false)

        val networkMode =
            when (sharedPreferences.getString(NETWORK_MODE_PREF, NETWORK_MODE_DEFAULT)) {
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
            NetworkModeConfig(
                networkMode = networkMode,
                userFilePath = userFilePath,
                storedFilePath = storedFilePath,
                useReserveMultiplexLib = useReserveMultiplexLib
            )
        } else {
            NetworkModeConfig(
                networkMode = networkMode,
                userFilePath = null,
                storedFilePath = null,
                useReserveMultiplexLib = useReserveMultiplexLib
            )
        }
    }

    override fun clear() {
        // TODO?
    }

    object NetworkModeConstants {
        const val NETWORK_MODE_PREF = "pref.network_mode"
        const val NETWORK_MODE_APP_FILE_PATH_PREF = "pref.network_config_file_path"
        const val NETWORK_MODE_USER_FILE_PATH_PREF = "pref.network_mode_user_config_file_path"
        const val USE_RESERVE_MULTIPLEX_LIBRARY_PREF = "pref.use_reserve_multiplex_library"

        const val NAMED_NETWORK_MODE_PREFS = "network_mode"
    }
}