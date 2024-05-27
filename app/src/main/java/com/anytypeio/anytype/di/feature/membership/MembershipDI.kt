package com.anytypeio.anytype.di.feature.membership

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.payments.GetMembershipEmailStatus
import com.anytypeio.anytype.domain.payments.GetMembershipPaymentUrl
import com.anytypeio.anytype.domain.payments.IsMembershipNameValid
import com.anytypeio.anytype.domain.payments.SetMembershipEmail
import com.anytypeio.anytype.domain.payments.VerifyMembershipEmailCode
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import com.anytypeio.anytype.ui.payments.MembershipFragment
import com.anytypeio.anytype.payments.viewmodel.MembershipViewModelFactory
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [MembershipComponentDependencies::class],
    modules = [
        MembershipModule::class,
        MembershipModule.Declarations::class
    ]
)
@PerScreen
interface MembershipComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: MembershipComponentDependencies): MembershipComponent
    }

    fun inject(fragment: MembershipFragment)
}

@Module
object MembershipModule {

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

    @JvmStatic
    @Provides
    @PerScreen
    fun provideIsNameValid(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): IsMembershipNameValid = IsMembershipNameValid(repo = repo, dispatchers = dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetEmailStatus(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetMembershipEmailStatus = GetMembershipEmailStatus(repo = repo, dispatchers = dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetMembershipEmail(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetMembershipEmail = SetMembershipEmail(repo = repo, dispatchers = dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideVerifyEmailCode(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): VerifyMembershipEmailCode = VerifyMembershipEmailCode(repo = repo, dispatchers = dispatchers)

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: MembershipViewModelFactory
        ): ViewModelProvider.Factory

    }
}

interface MembershipComponentDependencies : ComponentDependencies {
    fun analytics(): Analytics
    fun context(): Context
    fun billingListener(): BillingClientLifecycle
    fun authRepository(): AuthRepository
    fun blockRepository(): BlockRepository
    fun appCoroutineDispatchers(): AppCoroutineDispatchers
    fun provideMembershipProvider(): MembershipProvider
}