package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.SelectAccount
import com.agileburo.anytype.feature_login.ui.login.domain.repository.UserRepository
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.setup.SetupSelectedAccountViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class SetupSelectedAccountModule {

    @Provides
    @PerScreen
    fun provideSetupSelectedAccountViewModelFactory(
        selectAccount: SelectAccount
    ): SetupSelectedAccountViewModelFactory {
        return SetupSelectedAccountViewModelFactory(
            selectAccount = selectAccount
        )
    }

    @Provides
    @PerScreen
    fun provideSelectAccountUseCase(
        userRepository: UserRepository
    ): SelectAccount {
        return SelectAccount(userRepository)
    }

}