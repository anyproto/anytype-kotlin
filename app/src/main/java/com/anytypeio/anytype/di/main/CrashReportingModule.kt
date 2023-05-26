package com.anytypeio.anytype.di.main

import android.content.Context
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.SentryCrashReporter
import com.anytypeio.anytype.core_utils.tools.AppInfo
import com.anytypeio.anytype.device.BuildProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object CrashReportingModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideCrashReporter(
        context: Context,
        appInfo: AppInfo,
        buildProvider: BuildProvider
    ): CrashReporter = SentryCrashReporter(
        context = context,
        appInfo = appInfo,
        withTimber = !buildProvider.isDebug()
    )
}