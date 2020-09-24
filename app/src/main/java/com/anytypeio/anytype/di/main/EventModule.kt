package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.data.auth.event.EventDataChannel
import com.anytypeio.anytype.data.auth.event.EventRemoteChannel
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.interactor.EventHandler
import com.anytypeio.anytype.middleware.interactor.MiddlewareEventChannel
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
    fun provideEventProxy(): EventProxy {
        return EventHandler()
    }
}