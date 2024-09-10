package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.data.auth.account.AccountStatusDataChannel
import com.anytypeio.anytype.data.auth.account.AccountStatusRemoteChannel
import com.anytypeio.anytype.data.auth.event.ChatEventRemoteChannel
import com.anytypeio.anytype.data.auth.event.EventDataChannel
import com.anytypeio.anytype.data.auth.event.EventRemoteChannel
import com.anytypeio.anytype.data.auth.event.FileLimitsDataChannel
import com.anytypeio.anytype.data.auth.event.FileLimitsRemoteChannel
import com.anytypeio.anytype.data.auth.event.SubscriptionDataChannel
import com.anytypeio.anytype.data.auth.event.SubscriptionEventRemoteChannel
import com.anytypeio.anytype.data.auth.status.SyncAndP2PStatusEventsStore
import com.anytypeio.anytype.di.main.ConfigModule.DEFAULT_APP_COROUTINE_SCOPE
import com.anytypeio.anytype.domain.account.AccountStatusChannel
import com.anytypeio.anytype.domain.chats.ChatEventChannel
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.workspace.FileLimitsEventChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.interactor.AccountStatusMiddlewareChannel
import com.anytypeio.anytype.middleware.interactor.EventHandler
import com.anytypeio.anytype.middleware.interactor.EventHandlerChannel
import com.anytypeio.anytype.middleware.interactor.EventHandlerChannelImpl
import com.anytypeio.anytype.middleware.interactor.FileLimitsMiddlewareChannel
import com.anytypeio.anytype.middleware.interactor.MiddlewareEventChannel
import com.anytypeio.anytype.middleware.interactor.MiddlewareProtobufLogger
import com.anytypeio.anytype.middleware.interactor.MiddlewareSubscriptionEventChannel
import com.anytypeio.anytype.middleware.interactor.SyncAndP2PStatusEventsStoreImpl
import com.anytypeio.anytype.middleware.interactor.events.ChatEventMiddlewareChannel
import com.anytypeio.anytype.presentation.common.PayloadDelegator
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module(includes = [EventModule.Bindings::class])
object EventModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideEventChannel(
        channel: EventDataChannel
    ): EventChannel = channel

    @JvmStatic
    @Provides
    @Singleton
    fun provideEventDataChannel(
        remote: EventRemoteChannel
    ): EventDataChannel = EventDataChannel(remote)

    @JvmStatic
    @Provides
    @Singleton
    fun provideEventRemoteChannel(
        proxy: EventProxy,
        featureToggles: FeatureToggles
    ): EventRemoteChannel = MiddlewareEventChannel(events = proxy, featureToggles = featureToggles)

    @JvmStatic
    @Provides
    @Singleton
    fun provideAccountStatusChannel(
        channel: AccountStatusDataChannel
    ) : AccountStatusChannel = channel

    @JvmStatic
    @Provides
    @Singleton
    fun provideAccountStatusDataChannel(
        channel: AccountStatusRemoteChannel
    ) = AccountStatusDataChannel(
        remote = channel
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideAccountStatusRemoteChannel(
        proxy: EventProxy
    ) : AccountStatusRemoteChannel = AccountStatusMiddlewareChannel(
        events = proxy
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideSubscriptionEventChannel(
        channel: SubscriptionDataChannel
    ): SubscriptionEventChannel = channel

    @JvmStatic
    @Provides
    @Singleton
    fun provideSubscriptionEventDataChannel(
        remote: SubscriptionEventRemoteChannel
    ): SubscriptionDataChannel = SubscriptionDataChannel(remote)

    @JvmStatic
    @Provides
    @Singleton
    fun provideSubscriptionEventRemoteChannel(
        proxy: EventProxy
    ): SubscriptionEventRemoteChannel = MiddlewareSubscriptionEventChannel(events = proxy)

    @JvmStatic
    @Provides
    @Singleton
    fun provideFileLimitsRemoteChannel(
        proxy: EventProxy
    ): FileLimitsRemoteChannel = FileLimitsMiddlewareChannel(events = proxy)

    @JvmStatic
    @Provides
    @Singleton
    fun provideFileLimitsDataChannel(
        channel: FileLimitsRemoteChannel
    ): FileLimitsEventChannel = FileLimitsDataChannel(channel = channel)

    @JvmStatic
    @Provides
    @Singleton
    fun provideEventHandler(
        logger: MiddlewareProtobufLogger,
        @Named(DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope,
        channel: EventHandlerChannel,
        syncP2PStore: SyncAndP2PStatusEventsStore
    ): EventProxy = EventHandler(
        scope = scope,
        logger = logger,
        channel = channel,
        syncP2PStore = syncP2PStore
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideSyncAndP2PStatusEventsSubscription(
        @Named(DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope,
        channel: EventHandlerChannel
    ): SyncAndP2PStatusEventsStore = SyncAndP2PStatusEventsStoreImpl(
        scope = scope,
        channel = channel
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideDefaultEventChannel(): EventHandlerChannel = EventHandlerChannelImpl()

    //region Chats

    @JvmStatic
    @Provides
    @Singleton
    fun provideChatEventChannel(
        channel: ChatEventRemoteChannel.Default
    ): ChatEventChannel = channel

    @JvmStatic
    @Provides
    @Singleton
    fun provideChatEventDataChannel(
        remote: ChatEventRemoteChannel
    ): ChatEventRemoteChannel.Default = ChatEventRemoteChannel.Default(
        channel = remote
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideChatEventMWChannel(
        proxy: EventProxy
    ): ChatEventRemoteChannel = ChatEventMiddlewareChannel(
        proxy
    )

    //endregion

    @Module
    interface Bindings {

        @Binds
        @Singleton
        fun payloadDelegator(default: PayloadDelegator.Default): PayloadDelegator
    }
}