package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.PerScreen
import com.agileburo.anytype.feature_login.ui.login.domain.common.PathProvider
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.SetupWallet
import com.agileburo.anytype.feature_login.ui.login.domain.repository.AuthRepository
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.start.StartLoginViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class StartLoginModule {

    @PerScreen
    @Provides
    fun provideViewModelFactory(
        setupWallet: SetupWallet,
        pathProvider: PathProvider
    ): StartLoginViewModelFactory {
        return StartLoginViewModelFactory(
            setupWallet = setupWallet,
            pathProvider = pathProvider
        )
    }

    @PerScreen
    @Provides
    fun provideSetupWalletUseCase(
        authRepository: AuthRepository
    ): SetupWallet {
        return SetupWallet(
            repository = authRepository
        )
    }

}