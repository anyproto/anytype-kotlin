package com.anytypeio.anytype.app

import android.app.Application
import com.amplitude.api.Amplitude
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.SentryCrashReporter
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
    lateinit var sentryCrashReporter: SentryCrashReporter

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
        setupTimber()
        setupSentry()
        setupLocalNetworkAddressHandler()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }

    private fun setupSentry() {
        sentryCrashReporter.init(
            withTimber = !BuildConfig.DEBUG
        )
    }

    private fun setupAnalytics() {
        Amplitude.getInstance().initialize(this, BuildConfig.AMPLITUDE_KEY)
    }

    private fun setupLocalNetworkAddressHandler() {
        localNetworkAddressHandler.start()
        discoveryManager.setup()
    }
}