package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.DefaultAnalytics
import com.anytypeio.anytype.analytics.tracker.AmplitudeTracker
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.analytics.DefaultAnalyticsParamsProvider
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.GlobalScope
import javax.inject.Singleton

@Module
object AnalyticsModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideAnalytics(): Analytics = DefaultAnalytics()

    @JvmStatic
    @Provides
    @Singleton
    fun provideAmplitudeTracker(
        analytics: Analytics
    ): AmplitudeTracker = AmplitudeTracker(
        scope = GlobalScope,
        analytics = analytics
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideAnalyticsParams(
        userPermissionProvider: UserPermissionProvider,
        spaceViewContainer: SpaceViewSubscriptionContainer
    ): AnalyticSpaceHelperDelegate = DefaultAnalyticsParamsProvider(
        userPermissionProvider = userPermissionProvider,
        spaceViewContainer = spaceViewContainer
    )
}