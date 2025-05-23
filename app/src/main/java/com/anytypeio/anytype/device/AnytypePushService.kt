package com.anytypeio.anytype.device

import com.anytypeio.anytype.app.AndroidApplication
import com.anytypeio.anytype.core_utils.ext.runSafely
import com.anytypeio.anytype.domain.device.DeviceTokenStoringService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class AnytypePushService : FirebaseMessagingService() {

    @Inject
    lateinit var deviceTokenSavingService: DeviceTokenStoringService

    @Inject
    lateinit var processor: PushMessageProcessor

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

        // Skip showing notification if app is in foreground
        if (isAppInForeground()) {
            Timber.d("App is in foreground, skipping notification")
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            runSafely("processing push message") {
                processor.process(message.data)
            }
        }
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false

        val packageName = packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && appProcess.processName == packageName
            ) {
                return true
            }
        }
        return false
    }

    companion object {
        const val ACTION_OPEN_CHAT = "com.anytype.ACTION_OPEN_CHAT"
    }
}