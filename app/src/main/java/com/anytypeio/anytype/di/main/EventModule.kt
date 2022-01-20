package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.data.auth.event.EventDataChannel
import com.anytypeio.anytype.data.auth.event.EventRemoteChannel
import com.anytypeio.anytype.data.auth.event.SubscriptionDataChannel
import com.anytypeio.anytype.data.auth.event.SubscriptionEventRemoteChannel
import com.anytypeio.anytype.data.auth.status.ThreadStatusDataChannel
import com.anytypeio.anytype.data.auth.status.ThreadStatusRemoteChannel
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.status.ThreadStatusChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.interactor.EventHandler
import com.anytypeio.anytype.middleware.interactor.MiddlewareEventChannel
import com.anytypeio.anytype.middleware.interactor.MiddlewareSubscriptionEventChannel
import com.anytypeio.anytype.middleware.interactor.ThreadStatusMiddlewareChannel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
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
        proxy: EventProxy
    ): EventRemoteChannel = MiddlewareEventChannel(events = proxy)

    @JvmStatic
    @Provides
    @Singleton
    fun provideThreadStatusChannel(
        channel: ThreadStatusDataChannel
    ): ThreadStatusChannel = channel

    @JvmStatic
    @Provides
    @Singleton
    fun provideThreadStatusDataChannel(
        remote: ThreadStatusRemoteChannel
    ): ThreadStatusDataChannel = ThreadStatusDataChannel(remote)

    @JvmStatic
    @Provides
    @Singleton
    fun provideThreadStatusRemoteChannel(
        proxy: EventProxy
    ): ThreadStatusRemoteChannel = ThreadStatusMiddlewareChannel(events = proxy)

    @JvmStatic
    @Provides
    @Singleton
    fun provideEventProxy(): EventProxy {
        return EventHandler()
    }

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
}