package com.agileburo.anytype.app

import android.app.Application
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import com.agileburo.anytype.BuildConfig
import com.agileburo.anytype.R
import com.agileburo.anytype.analytics.tracker.AmplitudeTracker
import com.agileburo.anytype.core_utils.tools.CrashlyticsTree
import com.agileburo.anytype.di.common.ComponentManager
import com.agileburo.anytype.di.main.ContextModule
import com.agileburo.anytype.di.main.DaggerMainComponent
import com.agileburo.anytype.di.main.MainComponent
import com.amplitude.api.Amplitude
import com.facebook.stetho.Stetho
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
        Amplitude.getInstance().initialize(this, getString(R.string.amplitude_api_key))
    }
}