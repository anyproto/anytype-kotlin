package com.agileburo.anytype.di.main

import com.agileburo.anytype.data.auth.event.EventDataChannel
import com.agileburo.anytype.data.auth.event.EventRemoteChannel
import com.agileburo.anytype.domain.event.interactor.EventChannel
import com.agileburo.anytype.middleware.EventProxy
import com.agileburo.anytype.middleware.interactor.EventHandler
import com.agileburo.anytype.middleware.interactor.MiddlewareEventChannel
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