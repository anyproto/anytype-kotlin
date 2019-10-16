package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.ObserveAccounts
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.StartLoadingAccounts
import com.agileburo.anytype.feature_login.ui.login.domain.repository.UserRepository
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile.SelectAccountViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class SelectProfileModule {

    @PerScreen
    @Provides
    fun provideSelectProfileViewModelFactory(
        startLoadingAccounts: StartLoadingAccounts,
        observeAccounts: ObserveAccounts
    ): SelectAccountViewModelFactory {
        return SelectAccountViewModelFactory(
            startLoadingAccounts = startLoadingAccounts,
            observeAccounts = observeAccounts
        )
    }

    @PerScreen
    @Provides
    fun provideRecoverAccountUseCase(repository: UserRepository): StartLoadingAccounts {
        return StartLoadingAccounts(
            repository = repository
        )
    }

    @PerScreen
    @Provides
    fun provideObserveAccountsUseCase(
        repository: UserRepository
    ): ObserveAccounts {
        return ObserveAccounts(
            repository = repository
        )
    }

}