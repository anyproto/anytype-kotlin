package com.anytypeio.anytype.di.main

import android.content.Context
import com.anytypeio.anytype.data.auth.event.MembershipDateChannel
import com.anytypeio.anytype.data.auth.event.MembershipRemoteChannel
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.payments.GetMembershipStatus
import com.anytypeio.anytype.domain.payments.GetMembershipTiers
import com.anytypeio.anytype.domain.workspace.MembershipChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.interactor.MembershipMiddlewareChannel
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module
object MembershipModule {

    @Singleton
    @Provides
    fun provideBillingLifecycle(
        context: Context,
        dispatchers: AppCoroutineDispatchers,
        @Named(ConfigModule.DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope
    ): BillingClientLifecycle {
        return BillingClientLifecycle(
            dispatchers = dispatchers,
            applicationContext = context,
            scope = scope
        )
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideMembershipRemoteChannel(
        proxy: EventProxy
    ): MembershipRemoteChannel = MembershipMiddlewareChannel(
        eventsProxy = proxy
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideMembershipChannel(
        channel: MembershipRemoteChannel
    ): MembershipChannel = MembershipDateChannel(
        channel = channel
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideGetMembershipStatus(
        repository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetMembershipStatus = GetMembershipStatus(dispatchers, repository)

    @JvmStatic
    @Singleton
    @Provides
    fun provideMembershipProvider(
        dispatchers: AppCoroutineDispatchers,
        @Named(ConfigModule.DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope,
        awaitAccountStartManager: AwaitAccountStartManager,
        membershipChannel: MembershipChannel,
        getMembershipStatus: GetMembershipStatus,
        localeProvider: LocaleProvider,
        getTiers: GetMembershipTiers
    ): MembershipProvider = MembershipProvider.Default(
        dispatchers = dispatchers,
        scope = scope,
        membershipChannel = membershipChannel,
        awaitAccountStartManager = awaitAccountStartManager,
        getMembershipStatus = getMembershipStatus,
        localeProvider = localeProvider,
        getTiers = getTiers
    )
}