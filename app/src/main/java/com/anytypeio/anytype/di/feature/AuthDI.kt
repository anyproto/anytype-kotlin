package com.anytypeio.anytype.di.feature

import androidx.compose.material.ExperimentalMaterialApi
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerFeature
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.auth.interactor.ConvertWallet
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.interactor.ObserveAccounts
import com.anytypeio.anytype.domain.auth.interactor.RecoverWallet
import com.anytypeio.anytype.domain.auth.interactor.SaveMnemonic
import com.anytypeio.anytype.domain.auth.interactor.SelectAccount
import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.auth.interactor.StartLoadingAccounts
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.`object`.SetupMobileUseCaseSkip
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.auth.account.CreateAccountViewModelFactory
import com.anytypeio.anytype.presentation.auth.account.SelectAccountViewModelFactory
import com.anytypeio.anytype.presentation.auth.account.SetupNewAccountViewModelFactory
import com.anytypeio.anytype.presentation.auth.account.SetupSelectedAccountViewModelFactory
import com.anytypeio.anytype.presentation.auth.keychain.KeychainLoginViewModelFactory
import com.anytypeio.anytype.presentation.auth.model.Session
import com.anytypeio.anytype.presentation.auth.start.StartLoginViewModelFactory
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.ui.auth.AboutAnalyticsFragment
import com.anytypeio.anytype.ui.auth.InvitationFragment
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

    fun inject(fragment: InvitationFragment)

    @ExperimentalMaterialApi
    fun inject(fragment: AboutAnalyticsFragment)

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
        session: Session,
        analytics: Analytics
    ): CreateAccountViewModelFactory {
        return CreateAccountViewModelFactory(
            session = session,
            analytics = analytics
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
        analytics: Analytics,
        relationsSubscriptionManager: RelationsSubscriptionManager,
        objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
        spaceGradientProvider: SpaceGradientProvider,
        configStorage: ConfigStorage,
        crashReporter: CrashReporter,
        setupMobileUseCaseSkip: SetupMobileUseCaseSkip
    ): SetupNewAccountViewModelFactory {
        return SetupNewAccountViewModelFactory(
            createAccount = createAccount,
            session = session,
            analytics = analytics,
            relationsSubscriptionManager = relationsSubscriptionManager,
            objectTypesSubscriptionManager = objectTypesSubscriptionManager,
            spaceGradientProvider = spaceGradientProvider,
            configStorage = configStorage,
            crashReporter = crashReporter,
            setupMobileUseCaseSkip = setupMobileUseCaseSkip
        )
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun gradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Impl()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateAccountUseCase(
        repository: AuthRepository,
        configStorage: ConfigStorage,
        workspaceManager: WorkspaceManager
    ): CreateAccount = CreateAccount(
        repository = repository,
        configStorage = configStorage,
        workspaceManager = workspaceManager
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideImportUseCase(
        dispatchers: AppCoroutineDispatchers,
        blockRepository: BlockRepository
    ) = SetupMobileUseCaseSkip(
        repo = blockRepository,
        dispatchers = dispatchers
    )

}

@Module
object SetupSelectedAccountModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetupSelectedAccountViewModelFactory(
        selectAccount: SelectAccount,
        pathProvider: PathProvider,
        analytics: Analytics,
        relationsSubscriptionManager: RelationsSubscriptionManager,
        objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
        crashReporter: CrashReporter,
        configStorage: ConfigStorage
    ): SetupSelectedAccountViewModelFactory {
        return SetupSelectedAccountViewModelFactory(
            selectAccount = selectAccount,
            pathProvider = pathProvider,
            analytics = analytics,
            relationsSubscriptionManager = relationsSubscriptionManager,
            objectTypesSubscriptionManager = objectTypesSubscriptionManager,
            crashReporter = crashReporter,
            configStorage = configStorage
        )
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSelectAccountUseCase(
        repository: AuthRepository,
        configStorage: ConfigStorage,
        featuresConfigProvider: FeaturesConfigProvider,
        workspaceManager: WorkspaceManager
    ): SelectAccount {
        return SelectAccount(
            repository = repository,
            configStorage = configStorage,
            featuresConfigProvider = featuresConfigProvider,
            workspaceManager = workspaceManager
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
        observeAccounts: ObserveAccounts,
        analytics: Analytics
    ): SelectAccountViewModelFactory {
        return SelectAccountViewModelFactory(
            startLoadingAccounts = startLoadingAccounts,
            observeAccounts = observeAccounts,
            analytics = analytics
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
    ): ConvertWallet {
        return ConvertWallet(
            authRepository = authRepository
        )
    }
}