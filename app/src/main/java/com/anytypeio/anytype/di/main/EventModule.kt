package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.data.auth.account.AccountStatusDataChannel
import com.anytypeio.anytype.data.auth.account.AccountStatusRemoteChannel
import com.anytypeio.anytype.data.auth.event.EventDataChannel
import com.anytypeio.anytype.data.auth.event.EventRemoteChannel
import com.anytypeio.anytype.data.auth.event.FileLimitsDataChannel
import com.anytypeio.anytype.data.auth.event.SubscriptionDataChannel
import com.anytypeio.anytype.data.auth.event.SubscriptionEventRemoteChannel
import com.anytypeio.anytype.data.auth.status.ThreadStatusDataChannel
import com.anytypeio.anytype.data.auth.status.ThreadStatusRemoteChannel
import com.anytypeio.anytype.domain.account.AccountStatusChannel
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.status.ThreadStatusChannel
import com.anytypeio.anytype.data.auth.event.FileLimitsRemoteChannel
import com.anytypeio.anytype.domain.workspace.FileLimitsEventChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.interactor.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

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

    @Module
    interface Bindings {

        @Binds
        @Singleton
        fun bindEventProxy(handler: EventHandler): EventProxy
    }
}