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
    override lateinit var dependencies: ComponentDependenciesProvider
        protected set

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
        super.onCreate()
        main.inject(this)
        setupAnalytics()
        setupEmojiCompat()
        setupTimber()
        setupLocalNetworkAddressHandler()
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

    private fun setupAnalytics() {
        Amplitude.getInstance().initialize(this, BuildConfig.AMPLITUDE_KEY)
    }

    private fun setupLocalNetworkAddressHandler() {
        localNetworkAddressHandler.start()
        discoveryManager.setup()
    }
}