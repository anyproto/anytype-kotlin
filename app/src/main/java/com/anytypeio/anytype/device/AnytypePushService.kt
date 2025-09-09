package com.anytypeio.anytype.device

import com.anytypeio.anytype.app.AndroidApplication
import com.anytypeio.anytype.core_utils.ext.runSafely
import com.anytypeio.anytype.core_utils.ext.isAppInForeground
import com.anytypeio.anytype.di.main.ConfigModule.DEFAULT_APP_COROUTINE_SCOPE
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.device.DeviceTokenStoringService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class AnytypePushService : FirebaseMessagingService() {

    @Inject
    lateinit var deviceTokenSavingService: DeviceTokenStoringService

    @Inject
    lateinit var processor: PushMessageProcessor

    @Inject
    @Named(DEFAULT_APP_COROUTINE_SCOPE)
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var dispatchers: AppCoroutineDispatchers

    @Inject
    lateinit var checkAuthorizationStatus: CheckAuthorizationStatus

    override fun onCreate() {
        super.onCreate()
        (application as AndroidApplication).componentManager.pushContentComponent.get().inject(this)
        Timber.d("AnytypePushService initialized")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New token received: $token")
        runSafely("saving device token") {
            deviceTokenSavingService.saveToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("Received message: $message")
        checkAuthorizationStatus(message)
    }

    private fun checkAuthorizationStatus(message: RemoteMessage) {
        scope.launch(dispatchers.io) {
            checkAuthorizationStatus(Unit).process(
                failure = { e -> Timber.e(e, "Error while checking auth status") },
                success = { status ->
                    if (status == AuthStatus.UNAUTHORIZED) {
                        Timber.w("User is unauthorized, skipping push message processing")
                        // If the user is unauthorized, we do not process push messages
                    } else {
                        proceedWithPushMessage(message)
                    }
                }
            )
        }
    }

    private fun proceedWithPushMessage(message: RemoteMessage) {
        runSafely("processing push message") {
            processor.process(
                messageData = message.data,
                isAppInForeground = isAppInForeground()
            )
        }
    }

    companion object {
        const val ACTION_OPEN_CHAT = "com.anytype.ACTION_OPEN_CHAT"
    }
}