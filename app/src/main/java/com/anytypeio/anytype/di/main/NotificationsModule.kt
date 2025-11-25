package com.anytypeio.anytype.di.main

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import com.anytypeio.anytype.app.AnytypeNotificationService
import com.anytypeio.anytype.data.auth.event.NotificationsDateChannel
import com.anytypeio.anytype.data.auth.event.NotificationsRemoteChannel
import com.anytypeio.anytype.device.NotificationBuilderImpl
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.chats.ChatsDetailsSubscriptionContainer
import com.anytypeio.anytype.domain.chats.PushKeyChannel
import com.anytypeio.anytype.domain.chats.PushKeySpaceViewChannel
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.notifications.NotificationBuilder
import com.anytypeio.anytype.domain.notifications.PushKeyProvider
import com.anytypeio.anytype.domain.notifications.SystemNotificationService
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.workspace.NotificationsChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.interactor.NotificationsMiddlewareChannel
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManagerImpl
import com.anytypeio.anytype.presentation.notifications.NotificationsProvider
import com.anytypeio.anytype.presentation.notifications.PushKeyProviderImpl
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

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
        channel: PushKeyChannel,
        json: Json
    ): PushKeyProvider = PushKeyProviderImpl(
        sharedPreferences = sharedPreferences,
        dispatchers = dispatchers,
        scope = scope,
        channel = channel,
        json = json
    )

    @JvmStatic
    @Provides
    @Singleton
    fun providePushKeyChannel(
        spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer
    ): PushKeyChannel = PushKeySpaceViewChannel(spaceViewSubscriptionContainer)

    @Provides
    @Singleton
    @JvmStatic
    fun provideNotificationPermissionManager(
        @Named("default") prefs: SharedPreferences,
        context: Context
    ): NotificationPermissionManager {
        return NotificationPermissionManagerImpl(
            sharedPreferences = prefs,
            context = context
        )
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideNotificationBuilder(
        context: Context,
        notificationManager: NotificationManager,
        stringResourceProvider: StringResourceProvider,
        urlBuilder: com.anytypeio.anytype.domain.misc.UrlBuilder,
        spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
        chatsDetailsSubscriptionContainer: ChatsDetailsSubscriptionContainer
    ): NotificationBuilder = NotificationBuilderImpl(
        context = context,
        notificationManager = notificationManager,
        resourceProvider = stringResourceProvider,
        urlBuilder = urlBuilder,
        spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
        chatsDetailsSubscriptionContainer = chatsDetailsSubscriptionContainer
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideNotificationManager(
        context: Context
    ): NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}