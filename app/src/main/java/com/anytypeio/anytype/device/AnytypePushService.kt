package com.anytypeio.anytype.device

import com.anytypeio.anytype.di.main.ConfigModule.DEFAULT_APP_COROUTINE_SCOPE
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.notifications.RegisterDeviceTokenUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope

class AnytypePushService: FirebaseMessagingService() {

    @Inject
    lateinit var dispatchers: AppCoroutineDispatchers

    @Inject
    @Named(DEFAULT_APP_COROUTINE_SCOPE)
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var registerDeviceToken: RegisterDeviceTokenUseCase

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Handle the new token here
        // For example, send it to your server or save it locally
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }
}