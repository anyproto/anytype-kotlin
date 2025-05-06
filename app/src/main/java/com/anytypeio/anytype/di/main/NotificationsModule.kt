package com.anytypeio.anytype.di.main

import android.content.Context
import android.content.SharedPreferences
import com.anytypeio.anytype.app.AnytypeNotificationService
import com.anytypeio.anytype.data.auth.event.NotificationsDateChannel
import com.anytypeio.anytype.data.auth.event.NotificationsRemoteChannel
import com.anytypeio.anytype.data.auth.event.PushKeyDateChannel
import com.anytypeio.anytype.data.auth.event.PushKeyRemoteChannel
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.chats.PushKeyChannel
import com.anytypeio.anytype.domain.notifications.SystemNotificationService
import com.anytypeio.anytype.domain.workspace.NotificationsChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.interactor.EventHandlerChannel
import com.anytypeio.anytype.middleware.interactor.NotificationsMiddlewareChannel
import com.anytypeio.anytype.middleware.interactor.events.PushKeyMiddlewareChannel
import com.anytypeio.anytype.presentation.notifications.NotificationsProvider
import com.anytypeio.anytype.presentation.notifications.PushKeyProvider
import com.anytypeio.anytype.presentation.notifications.PushKeyProviderImpl
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
    @Provides
    @Singleton
    fun provideNotificator(
        context: Context
    ): SystemNotificationService = AnytypeNotificationService(context = context)

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

    @JvmStatic
    @Provides
    @Singleton
    fun providePushKeyProvider(
        @Named("encrypted") sharedPreferences: SharedPreferences,
        dispatchers: AppCoroutineDispatchers,
        @Named(ConfigModule.DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope,
        channel: PushKeyChannel
    ): PushKeyProvider {
        return PushKeyProviderImpl(
            sharedPreferences = sharedPreferences,
            dispatchers = dispatchers,
            scope = scope,
            channel = channel
        )
    }

    @JvmStatic
    @Provides
    @Singleton
    fun providePushKeyRemoteChannel(
        channel: EventHandlerChannel,
        @Named(ConfigModule.DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope,
        dispatchers: AppCoroutineDispatchers
    ): PushKeyRemoteChannel = PushKeyMiddlewareChannel(
        channel = channel,
        scope = scope,
        dispatcher = dispatchers.io
    )

    @JvmStatic
    @Provides
    @Singleton
    fun providePushKeyChannel(
        channel: PushKeyRemoteChannel
    ): PushKeyChannel = PushKeyDateChannel(
        channel = channel
    )
}