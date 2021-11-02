package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.GetLastOpenedObject
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.block.interactor.sets.StoreObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.FlavourConfigProvider
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.launch.SetDefaultEditorType
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.presentation.splash.SplashViewModelFactory
import com.anytypeio.anytype.ui.splash.SplashFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-10-21.
 */

@PerScreen
@Subcomponent(modules = [SplashModule::class])
interface SplashSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): SplashSubComponent
        fun module(module: SplashModule): Builder
    }

    fun inject(fragment: SplashFragment)
}

@Module
object SplashModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun provideSplashViewModelFactory(
        checkAuthorizationStatus: CheckAuthorizationStatus,
        launchAccount: LaunchAccount,
        launchWallet: LaunchWallet,
        analytics: Analytics,
        storeObjectTypes: StoreObjectTypes,
        getLastOpenedObject: GetLastOpenedObject,
        getDefaultEditorType: GetDefaultEditorType,
        setDefaultEditorType: SetDefaultEditorType,
        createPage: CreatePage,
        appActionManager: AppActionManager
    ): SplashViewModelFactory = SplashViewModelFactory(
        checkAuthorizationStatus = checkAuthorizationStatus,
        launchAccount = launchAccount,
        launchWallet = launchWallet,
        analytics = analytics,
        storeObjectTypes = storeObjectTypes,
        getLastOpenedObject = getLastOpenedObject,
        setDefaultEditorType = setDefaultEditorType,
        getDefaultEditorType = getDefaultEditorType,
        createPage = createPage,
        appActionManager = appActionManager
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideCheckAuthorizationStatusUseCase(
        authRepository: AuthRepository
    ): CheckAuthorizationStatus = CheckAuthorizationStatus(
        repository = authRepository
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideLaunchAccountUseCase(
        authRepository: AuthRepository,
        pathProvider: PathProvider,
        flavourConfigProvider: FlavourConfigProvider
    ): LaunchAccount = LaunchAccount(
        repository = authRepository,
        pathProvider = pathProvider,
        flavourConfigProvider = flavourConfigProvider
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideLaunchWalletUseCase(
        authRepository: AuthRepository,
        pathProvider: PathProvider
    ): LaunchWallet =
        LaunchWallet(
            repository = authRepository,
            pathProvider = pathProvider
        )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideStoreObjectTypesUseCase(
        repo: BlockRepository,
        objectTypesProvider: ObjectTypesProvider
    ) : StoreObjectTypes = StoreObjectTypes(repo, objectTypesProvider)

    @JvmStatic
    @Provides
    @PerScreen
    fun getLastOpenedObject(
        repo: BlockRepository,
        auth: AuthRepository
    ) : GetLastOpenedObject = GetLastOpenedObject(
        authRepo = auth,
        blockRepo = repo
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideGetDefaultPageType(repo: UserSettingsRepository): GetDefaultEditorType =
        GetDefaultEditorType(repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideSetDefaultPageType(repo: UserSettingsRepository): SetDefaultEditorType =
        SetDefaultEditorType(repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideCreatePage(repo: BlockRepository): CreatePage = CreatePage(repo = repo)
}