package com.anytypeio.anytype.di.feature.payments

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.payments.GetMembershipPaymentUrl
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import com.anytypeio.anytype.ui.payments.PaymentsFragment
import com.anytypeio.anytype.payments.viewmodel.PaymentsViewModelFactory
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [PaymentsComponentDependencies::class],
    modules = [
        PaymentsModule::class,
        PaymentsModule.Declarations::class
    ]
)
@PerScreen
interface PaymentsComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: PaymentsComponentDependencies): PaymentsComponent
    }

    fun inject(fragment: PaymentsFragment)
}

@Module
object PaymentsModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetAccountUseCase(
        repo: AuthRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetAccount = GetAccount(repo = repo, dispatcher = dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetPaymentsUrl(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetMembershipPaymentUrl = GetMembershipPaymentUrl(repo = repo, dispatchers = dispatchers)

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: PaymentsViewModelFactory
        ): ViewModelProvider.Factory

    }
}

interface PaymentsComponentDependencies : ComponentDependencies {
    fun analytics(): Analytics
    fun context(): Context
    fun billingListener(): BillingClientLifecycle
    fun authRepository(): AuthRepository
    fun blockRepository(): BlockRepository
    fun appCoroutineDispatchers(): AppCoroutineDispatchers
    fun provideMembershipProvider(): MembershipProvider
}