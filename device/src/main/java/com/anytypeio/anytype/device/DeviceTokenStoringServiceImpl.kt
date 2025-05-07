package com.anytypeio.anytype.device

import android.content.SharedPreferences
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.device.DeviceTokenStoringService
import com.anytypeio.anytype.domain.notifications.RegisterDeviceTokenUseCase
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class DeviceTokenStoringServiceImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val registerDeviceToken: RegisterDeviceTokenUseCase,
    private val dispatchers: AppCoroutineDispatchers,
    private val scope: CoroutineScope
) : DeviceTokenStoringService {

    override fun saveToken(token: String) {
        scope.launch(dispatchers.io) {
            Timber.d("Saving token: $token")
            sharedPreferences.edit().apply {
                putString(PREF_KEY, token)
                apply()
            }
        }
    }

    override fun start() {
        val token = sharedPreferences.getString(PREF_KEY, null)
        if (!token.isNullOrEmpty()) {
            scope.launch(dispatchers.io) {
                val params = RegisterDeviceTokenUseCase.Params(token = token)
                registerDeviceToken.async(params).fold(
                    onSuccess = {
                        Timber.d("Successfully registered token: $token")
                    },
                    onFailure = { error ->
                        Timber.w("Failed to register token: $token, error: $error")
                    }
                )
            }
        }
    }

    override fun stop() {
        // Nothing to do here
    }

    companion object {
        private const val PREF_KEY = "prefs.device_token"
    }
} 