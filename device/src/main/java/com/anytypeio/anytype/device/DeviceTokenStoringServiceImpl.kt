package com.anytypeio.anytype.device

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.device.DeviceTokenStoringService
import com.anytypeio.anytype.domain.notifications.RegisterDeviceToken
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class DeviceTokenStoringServiceImpl @Inject constructor(
    private val registerDeviceToken: RegisterDeviceToken,
    private val dispatchers: AppCoroutineDispatchers,
    private val scope: CoroutineScope
) : DeviceTokenStoringService {

    override fun saveToken(token: String) {
        proceedWithUpdatingToken(token = token)
    }

    override fun start() {
        scope.launch(dispatchers.io) {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    if (token.isNotEmpty()) {
                        proceedWithUpdatingToken(token = token)
                    } else {
                        Timber.w("Firebase token is empty")
                    }
                }
                .addOnFailureListener { exception ->
                    Timber.w("Failed to get Firebase token: ${exception.message}")
                }
        }
    }

    private fun proceedWithUpdatingToken(token: String?) {
        if (!token.isNullOrEmpty()) {
            scope.launch(dispatchers.io) {
                val params = RegisterDeviceToken.Params(token = token)
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
} 