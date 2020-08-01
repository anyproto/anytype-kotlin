package com.agileburo.anytype.di.main

import com.agileburo.anytype.analytics.base.Analytics
import com.agileburo.anytype.analytics.base.DefaultAnalytics
import com.agileburo.anytype.analytics.tracker.AmplitudeTracker
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.GlobalScope
import javax.inject.Singleton

@Module
class AnalyticsModule {

    @Provides
    @Singleton
    fun provideAnalytics(): Analytics = DefaultAnalytics()

    @Provides
    @Singleton
    fun provideAmplitudeTracker(
        analytics: Analytics
    ): AmplitudeTracker = AmplitudeTracker(
        scope = GlobalScope,
        analytics = analytics
    )
}