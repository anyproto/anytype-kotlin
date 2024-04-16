package com.anytypeio.anytype.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import com.amplitude.api.Amplitude
import com.amplitude.api.TrackingOptions
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.tracker.AmplitudeTracker
import com.anytypeio.anytype.di.common.ComponentDependenciesProvider
import com.anytypeio.anytype.di.common.ComponentManager
import com.anytypeio.anytype.di.common.HasComponentDependencies
import com.anytypeio.anytype.di.main.ContextModule
import com.anytypeio.anytype.di.main.DaggerMainComponent
import com.anytypeio.anytype.di.main.MainComponent
import com.anytypeio.anytype.middleware.discovery.MDNSProvider
import com.anytypeio.anytype.middleware.discovery.adresshandler.LocalNetworkAddressProvider
import javax.inject.Inject
import timber.log.Timber

class AndroidApplication : Application(), HasComponentDependencies {

    @Inject
    lateinit var amplitudeTracker: AmplitudeTracker

    @Inject
    lateinit var localNetworkAddressHandler: LocalNetworkAddressProvider

    @Inject
    lateinit var discoveryManager: MDNSProvider

    @Inject
    lateinit var crashReporter: CrashReporter

    @Inject
    override lateinit var dependencies: ComponentDependenciesProvider

    private val main: MainComponent by lazy {
        DaggerMainComponent
            .builder()
            .contextModule(ContextModule(this))
            .build()
    }

    val componentManager by lazy {
        ComponentManager(main, this)
    }

    override fun onCreate() {
        if (BuildConfig.ENABLE_STRICT_MODE) {
            enableStrictMode()
        }
        super.onCreate()
        main.inject(this)
        setupAnalytics()
        setupTimber()
        setupCrashReporter()
        setupLocalNetworkAddressHandler()
        setupNotificationChannel()
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }

    private fun setupCrashReporter() {
        crashReporter.init()
    }

    private fun setupAnalytics() {
        Amplitude
            .getInstance()
            .setTrackingOptions(TrackingOptions().disableIpAddress())
            .initialize(this, BuildConfig.AMPLITUDE_KEY)
    }

    private fun setupLocalNetworkAddressHandler() {
        localNetworkAddressHandler.start()
        discoveryManager.setup()
    }
    private fun setupNotificationChannel() {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationChannel.description = NOTIFICATION_CHANNEL_DESCRIPTION

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }


    companion object {
        const val NOTIFICATION_CHANNEL_ID = "anytype_notification_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Local Anytype notifications"
        const val NOTIFICATION_CHANNEL_DESCRIPTION = "Important notifications from Anytype, including collaboration events in multiplayer mode"
    }
}