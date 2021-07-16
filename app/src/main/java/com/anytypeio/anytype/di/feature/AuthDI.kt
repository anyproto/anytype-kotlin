package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerFeature
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.auth.interactor.*
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.config.FlavourConfigProvider
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.presentation.auth.account.CreateAccountViewModelFactory
import com.anytypeio.anytype.presentation.auth.account.SelectAccountViewModelFactory
import com.anytypeio.anytype.presentation.auth.account.SetupNewAccountViewModelFactory
import com.anytypeio.anytype.presentation.auth.account.SetupSelectedAccountViewModelFactory
import com.anytypeio.anytype.presentation.auth.keychain.KeychainLoginViewModelFactory
import com.anytypeio.anytype.presentation.auth.model.Session
import com.anytypeio.anytype.presentation.auth.start.StartLoginViewModelFactory
import com.anytypeio.anytype.ui.auth.KeychainLoginFragment
import com.anytypeio.anytype.ui.auth.StartLoginFragment
import com.anytypeio.anytype.ui.auth.account.CreateAccountFragment
import com.anytypeio.anytype.ui.auth.account.SelectAccountFragment
import com.anytypeio.anytype.ui.auth.account.SetupNewAccountFragment
import com.anytypeio.anytype.ui.auth.account.SetupSelectedAccountFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [AuthModule::class])
@PerFeature
interface AuthSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun authModule(module: AuthModule): Builder
        fun build(): AuthSubComponent
    }

    fun startLoginComponentBuilder(): StartLoginSubComponent.Builder
    fun createAccountComponentBuilder(): CreateAccountSubComponent.Builder
    fun setupNewAccountComponentBuilder(): SetupNewAccountSubComponent.Builder
    fun setupSelectedAccountComponentBuilder(): SetupSelectedAccountSubComponent.Builder
    fun selectAccountComponentBuilder(): SelectAccountSubComponent.Builder
    fun keychainLoginComponentBuilder(): KeychainLoginSubComponent.Builder
}

@Subcomponent(modules = [StartLoginModule::class])
@PerScreen
interface StartLoginSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun startLoginModule(module: StartLoginModule): Builder
        fun build(): StartLoginSubComponent
    }

    fun inject(fragment: StartLoginFragment)
}

@Subcomponent(modules = [CreateAccountModule::class])
@PerScreen
interface CreateAccountSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun createAccountModule(module: CreateAccountModule): Builder
        fun build(): CreateAccountSubComponent
    }

    fun inject(fragment: CreateAccountFragment)
}

@Subcomponent(modules = [SetupNewAccountModule::class])
@PerScreen
interface SetupNewAccountSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun setupNewAccountModule(module: SetupNewAccountModule): Builder
        fun build(): SetupNewAccountSubComponent
    }

    fun inject(fragment: SetupNewAccountFragment)
}

@Subcomponent(modules = [SetupSelectedAccountModule::class])
@PerScreen
interface SetupSelectedAccountSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun setupSelectedAccountModule(module: SetupSelectedAccountModule): Builder
        fun build(): SetupSelectedAccountSubComponent
    }

    fun inject(fragment: SetupSelectedAccountFragment)
}

@Subcomponent(modules = [SelectAccountModule::class])
@PerScreen
interface SelectAccountSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun selectAccountModule(module: SelectAccountModule): Builder
        fun build(): SelectAccountSubComponent
    }

    fun inject(fragment: SelectAccountFragment)
}

@Subcomponent(modules = [KeychainLoginModule::class])
@PerScreen
interface KeychainLoginSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun keychainLoginModule(module: KeychainLoginModule): Builder
        fun build(): KeychainLoginSubComponent
    }

    fun inject(fragment: KeychainLoginFragment)
}

@Module
object AuthModule {

    @JvmStatic
    @PerFeature
    @Provides
    fun provideSession(): Session {
        return Session()
    }
}

@Module
object StartLoginModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun provideStartLoginViewModelFactory(
        setupWallet: SetupWallet,
        pathProvider: PathProvider,
        analytics: Analytics
    ): StartLoginViewModelFactory {
        return StartLoginViewModelFactory(
            setupWallet = setupWallet,
            pathProvider = pathProvider,
            analytics = analytics
        )
    }

    @JvmStatic
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

@Module
object CreateAccountModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun provideCreateAccountViewModelFactory(
        session: Session
    ): CreateAccountViewModelFactory {
        return CreateAccountViewModelFactory(
            session = session
        )
    }
}

@Module
object SetupNewAccountModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetupAccountViewModelFactory(
        createAccount: CreateAccount,
        session: Session,
        analytics: Analytics
    ): SetupNewAccountViewModelFactory {
        return SetupNewAccountViewModelFactory(
            createAccount = createAccount,
            session = session,
            analytics = analytics
        )
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateAccountUseCase(
        repository: AuthRepository
    ): CreateAccount {
        return CreateAccount(
            repository = repository
        )
    }
}

@Module
object SetupSelectedAccountModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetupSelectedAccountViewModelFactory(
        startAccount: StartAccount,
        pathProvider: PathProvider,
        analytics: Analytics
    ): SetupSelectedAccountViewModelFactory {
        return SetupSelectedAccountViewModelFactory(
            startAccount = startAccount,
            pathProvider = pathProvider,
            analytics = analytics
        )
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSelectAccountUseCase(
        repository: AuthRepository,
        flavourConfigProvider: FlavourConfigProvider
    ): StartAccount {
        return StartAccount(
            repository = repository,
            flavourConfigProvider = flavourConfigProvider
        )
    }
}

@Module
object SelectAccountModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun provideSelectAccountViewModelFactory(
        startLoadingAccounts: StartLoadingAccounts,
        observeAccounts: ObserveAccounts
    ): SelectAccountViewModelFactory {
        return SelectAccountViewModelFactory(
            startLoadingAccounts = startLoadingAccounts,
            observeAccounts = observeAccounts
        )
    }

    @JvmStatic
    @PerScreen
    @Provides
    fun provideRecoverAccountUseCase(repository: AuthRepository): StartLoadingAccounts {
        return StartLoadingAccounts(
            repository = repository
        )
    }

    @JvmStatic
    @PerScreen
    @Provides
    fun provideObserveAccountsUseCase(
        repository: AuthRepository
    ): ObserveAccounts {
        return ObserveAccounts(
            repository = repository
        )
    }
}

@Module
object KeychainLoginModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideKeychainLoginViewModelFactory(
        pathProvider: PathProvider,
        recoverWallet: RecoverWallet,
        convertWallet: ConvertWallet,
        saveMnemonic: SaveMnemonic,
        analytics: Analytics
    ): KeychainLoginViewModelFactory {
        return KeychainLoginViewModelFactory(
            pathProvider = pathProvider,
            convertWallet = convertWallet,
            recoverWallet = recoverWallet,
            saveMnemonic = saveMnemonic,
            analytics = analytics
        )
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideRecoverWalletUseCase(
        authRepository: AuthRepository
    ): RecoverWallet {
        return RecoverWallet(
            repository = authRepository
        )
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSaveMnemonicUseCase(
        authRepository: AuthRepository
    ): SaveMnemonic {
        return SaveMnemonic(
            repository = authRepository
        )
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideConvertWallet(
        authRepository: AuthRepository
    ) : ConvertWallet {
        return ConvertWallet(
            authRepository = authRepository
        )
    }
}