package com.agileburo.anytype.sample

import android.app.Application
import com.agileburo.anytype.core_utils.tools.CrashlyticsTree
import timber.log.Timber

class SampleApp : Application() {


    override fun onCreate() {
        super.onCreate()
        setupTimber()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
        else
            Timber.plant(CrashlyticsTree())
    }
}