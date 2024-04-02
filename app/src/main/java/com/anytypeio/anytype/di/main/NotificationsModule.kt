package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.data.auth.event.NotificationsDateChannel
import com.anytypeio.anytype.data.auth.event.NotificationsRemoteChannel
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.workspace.NotificationsChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.interactor.NotificationsMiddlewareChannel
import com.anytypeio.anytype.presentation.notifications.NotificationsProvider
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module
object NotificationsModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideNotificationsRemoteChannel(
        proxy: EventProxy
    ): NotificationsRemoteChannel = NotificationsMiddlewareChannel(
        eventsProxy = proxy
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideNotificationsChannel(
        channel: NotificationsRemoteChannel
    ): NotificationsChannel = NotificationsDateChannel(
        channel = channel
    )

    @JvmStatic
    @Singleton
    @Provides
    fun provideNotificationsProvider(
        dispatchers: AppCoroutineDispatchers,
        @Named(ConfigModule.DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope,
        awaitAccountStartManager: AwaitAccountStartManager,
        notificationsChannel: NotificationsChannel
    ): NotificationsProvider = NotificationsProvider.Default(
        dispatchers = dispatchers,
        scope = scope,
        notificationsChannel = notificationsChannel,
        awaitAccountStartManager = awaitAccountStartManager,
    )
}