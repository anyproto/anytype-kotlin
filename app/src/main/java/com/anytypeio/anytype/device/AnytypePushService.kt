package com.anytypeio.anytype.device

import com.anytypeio.anytype.app.AndroidApplication
import com.anytypeio.anytype.di.main.ConfigModule.DEFAULT_APP_COROUTINE_SCOPE
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.device.DeviceTokenStoringService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

class AnytypePushService : FirebaseMessagingService() {

    @Inject
    lateinit var dispatchers: AppCoroutineDispatchers

    @Inject
    @Named(DEFAULT_APP_COROUTINE_SCOPE)
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var deviceTokenSavingService: DeviceTokenStoringService

    init {
        Timber.d("AnytypePushService initialized")
    }

    override fun onCreate() {
        (application as AndroidApplication).componentManager.pushContentComponent.get().inject(this)
        super.onCreate()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New token received: $token")
        deviceTokenSavingService.saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }
}