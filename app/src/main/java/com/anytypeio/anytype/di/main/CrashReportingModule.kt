package com.anytypeio.anytype.di.main

import android.content.Context
import com.anytypeio.anytype.SentryCrashReporter
import com.anytypeio.anytype.core_utils.tools.AppInfo
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
        appInfo: AppInfo
    ): SentryCrashReporter = SentryCrashReporter(context, appInfo)

}