package com.anytypeio.anytype.app

import android.app.Application
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import com.amplitude.api.Amplitude
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.tracker.AmplitudeTracker
import com.anytypeio.anytype.core_utils.tools.CrashlyticsTree
import com.anytypeio.anytype.di.common.ComponentManager
import com.anytypeio.anytype.di.main.ContextModule
import com.anytypeio.anytype.di.main.DaggerMainComponent
import com.anytypeio.anytype.di.main.MainComponent
import com.facebook.stetho.Stetho
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import javax.inject.Inject

class AndroidApplication : Application() {

    @Inject
    lateinit var amplitudeTracker: AmplitudeTracker

    private val main: MainComponent by lazy {
        DaggerMainComponent
            .builder()
            .contextModule(ContextModule(this))
            .build()
    }

    val componentManager by lazy {
        ComponentManager(main)
    }

    override fun onCreate() {
        super.onCreate()
        main.inject(this)
        setupAnalytics()
        setupCrashlytics()
        setupEmojiCompat()
        setupTimber()
        setupStetho()
    }

    private fun setupEmojiCompat() {
        val fontRequest = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            "Graphik Regular",
            R.array.certs
        )
        val config = FontRequestEmojiCompatConfig(this, fontRequest)
        EmojiCompat.init(config)
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
        else
            Timber.plant(CrashlyticsTree())
    }

    private fun setupStetho() {
        if (BuildConfig.DEBUG)
            Stetho.initializeWithDefaults(this)
    }

    private fun setupAnalytics() {
        if (BuildConfig.DEBUG) {
            Amplitude.getInstance().initialize(this, getString(R.string.amplitude_api_key_debug))
        } else {
            Amplitude.getInstance().initialize(this, getString(R.string.amplitude_api_key))
        }
    }

    private fun setupCrashlytics() {
        if (BuildConfig.DEBUG)
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        else
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }
}