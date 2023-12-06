package com.anytypeio.anytype.persistence.networkmode

import android.content.SharedPreferences
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.persistence.networkmode.NetworkModeConstants.NETWORK_MODE_CUSTOM
import com.anytypeio.anytype.persistence.networkmode.NetworkModeConstants.NETWORK_MODE_DEFAULT
import com.anytypeio.anytype.persistence.networkmode.NetworkModeConstants.NETWORK_MODE_LOCAL
import com.anytypeio.anytype.persistence.networkmode.NetworkModeConstants.NETWORK_MODE_PREF

interface NetworkModeProvider {

    fun get(): NetworkMode
    fun getPath(): String?
}

class DefaultNetworkModeProvider(private val sharedPreferences: SharedPreferences) :
    NetworkModeProvider {

    override fun get(): NetworkMode {
        return when (sharedPreferences.getString(NETWORK_MODE_PREF, null)) {
            NETWORK_MODE_DEFAULT -> NetworkMode.DEFAULT
            NETWORK_MODE_LOCAL -> NetworkMode.LOCAL
            NETWORK_MODE_CUSTOM -> NetworkMode.CUSTOM
            else -> NetworkMode.DEFAULT
        }
    }

    override fun getPath(): String? = sharedPreferences.getString(
        NetworkModeConstants.NETWORK_CONFIG_FILE_PATH_PREF, null
    )
}