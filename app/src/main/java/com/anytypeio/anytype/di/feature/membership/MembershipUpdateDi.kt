package com.anytypeio.anytype.di.feature.membership

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.presentation.membership.MembershipUpgradeViewModel
import com.anytypeio.anytype.ui.payments.MembershipUpgradeFragment
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [MembershipUpdateComponentDependencies::class],
    modules = [
        MembershipUpdateModule::class,
        MembershipUpdateModule.Declarations::class
    ]
)
@PerScreen
interface MembershipUpdateComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: MembershipUpdateComponentDependencies): MembershipUpdateComponent
    }

    fun inject(fragment: MembershipUpgradeFragment)
}

@Module
object MembershipUpdateModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetAccountUseCase(
        repo: AuthRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetAccount = GetAccount(repo = repo, dispatcher = dispatchers)

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: MembershipUpgradeViewModel.MembershipUpgradeViewModelFactory
        ): ViewModelProvider.Factory

    }
}

interface MembershipUpdateComponentDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun appCoroutineDispatchers(): AppCoroutineDispatchers
}