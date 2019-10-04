package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_login.ui.login.domain.common.PathProvider
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.RecoverWallet
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.SaveMnemonic
import com.agileburo.anytype.feature_login.ui.login.domain.repository.AuthRepository
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.keychain.KeychainLoginViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class KeychainLoginModule {

    @Provides
    @PerScreen
    fun provideKeychainLoginViewModelFactory(
        pathProvider: PathProvider,
        recoverWallet: RecoverWallet,
        saveMnemonic: SaveMnemonic
    ): KeychainLoginViewModelFactory {
        return KeychainLoginViewModelFactory(
            pathProvider = pathProvider,
            recoverWallet = recoverWallet,
            saveMnemonic = saveMnemonic
        )
    }

    @Provides
    @PerScreen
    fun provideRecoverWalletUseCase(
        authRepository: AuthRepository
    ): RecoverWallet {
        return RecoverWallet(
            repository = authRepository
        )
    }

    @Provides
    @PerScreen
    fun provideSaveMnemonicUseCase(
        authRepository: AuthRepository
    ): SaveMnemonic {
        return SaveMnemonic(
            repository = authRepository
        )
    }

}