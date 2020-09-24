package com.anytypeio.anytype.sample

import android.app.Application
import com.anytypeio.anytype.core_utils.tools.CrashlyticsTree
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

    companion object {
        const val BASE_URI = "content://com.agileburo.anytype"
    }
}