package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.PerScreen
import com.agileburo.anytype.feature_login.ui.login.domain.common.Session
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.CreateAccount
import com.agileburo.anytype.feature_login.ui.login.domain.repository.UserRepository
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.setup.SetupNewAccountViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class SetupNewAccountModule {

    @Provides
    @PerScreen
    fun provideSetupAccountViewModelFactory(
        createAccount: CreateAccount,
        session: Session
    ): SetupNewAccountViewModelFactory {
        return SetupNewAccountViewModelFactory(
            createAccount = createAccount,
            session = session
        )
    }

    @Provides
    @PerScreen
    fun provideCreateAccountUseCase(
        userRepository: UserRepository
    ): CreateAccount {
        return CreateAccount(
            userRepository = userRepository
        )
    }
}