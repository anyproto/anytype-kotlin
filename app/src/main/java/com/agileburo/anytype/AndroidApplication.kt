package com.agileburo.anytype

import android.app.Application
import com.agileburo.anytype.core_utils.di.ContextModule
import com.agileburo.anytype.core_utils.di.CoreComponent
import com.agileburo.anytype.core_utils.di.CoreComponentProvider
import com.agileburo.anytype.core_utils.di.DaggerCoreComponent
import com.agileburo.anytype.core_utils.tools.CrashlyticsTree
import com.agileburo.anytype.di.app.AppModule
import com.agileburo.anytype.di.app.ApplicationComponent
import com.agileburo.anytype.di.app.DaggerApplicationComponent
import timber.log.Timber

class AndroidApplication : Application(), CoreComponentProvider {

    val applicationComponent: ApplicationComponent by lazy {
        DaggerApplicationComponent.builder()
            .appModule(AppModule(this))
            .build()
    }

    private val coreComponent: CoreComponent by lazy {
        DaggerCoreComponent
            .builder()
            .contextModule(ContextModule(this))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        injectMembers()
        setupTimber()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
        else
            Timber.plant(CrashlyticsTree())
    }

    private fun injectMembers() = applicationComponent.inject(this)

    override fun provideCoreComponent() = coreComponent
}