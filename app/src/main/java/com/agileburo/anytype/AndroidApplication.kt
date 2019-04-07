package com.agileburo.anytype

import android.app.Application
import com.agileburo.anytype.di.app.AppModule
import com.agileburo.anytype.di.app.ApplicationComponent
import com.agileburo.anytype.di.app.DaggerApplicationComponent
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import timber.log.Timber

class AndroidApplication : Application() {


    val applicationComponent: ApplicationComponent by lazy {
        DaggerApplicationComponent.builder()
            .appModule(AppModule(this))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        injectMembers()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        setupSentry()

        Sentry.capture("This is a test");
    }

    private fun setupSentry() {
        Sentry.init("https://1e0861387fc847caac7addda1a5c3776@sentry.io/1432841", AndroidSentryClientFactory(applicationContext))
    }

    private fun injectMembers() = applicationComponent.inject(this)
}