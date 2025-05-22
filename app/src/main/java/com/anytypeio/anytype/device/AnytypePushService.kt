package com.anytypeio.anytype.device

import android.app.NotificationManager
import android.content.Context
import com.anytypeio.anytype.app.AndroidApplication
import com.anytypeio.anytype.domain.device.DeviceTokenStoringService
import com.anytypeio.anytype.presentation.notifications.DecryptionPushContentService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject
import timber.log.Timber

class AnytypePushService : FirebaseMessagingService() {

    @Inject
    lateinit var deviceTokenSavingService: DeviceTokenStoringService

    @Inject
    lateinit var decryptionService: DecryptionPushContentService

    private lateinit var processor: PushMessageProcessor
    private lateinit var notificationBuilder: NotificationBuilder

    init {
        Timber.d("AnytypePushService initialized")
    }

    override fun onCreate() {
        super.onCreate()
        (application as AndroidApplication).componentManager.pushContentComponent.get().inject(this)

        notificationBuilder = NotificationBuilder(
            context = this,
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ).apply {
            createNotificationChannelIfNeeded()
        }

        processor = DefaultPushMessageProcessor(decryptionService, notificationBuilder)

        Timber.d("AnytypePushService initialized")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New token received: $token")
        deviceTokenSavingService.saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("Received message: $message")

        processor.process(message.data)
    }

    companion object {
        const val ACTION_OPEN_CHAT = "com.anytype.ACTION_OPEN_CHAT"
    }
}