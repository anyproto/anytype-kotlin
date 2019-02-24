package com.agileburo.anytype

import android.app.Application
import com.agileburo.anytype.di.app.AppModule
import com.agileburo.anytype.di.app.ApplicationComponent
import com.agileburo.anytype.di.app.DaggerApplicationComponent
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
        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun injectMembers() = applicationComponent.inject(this)
}