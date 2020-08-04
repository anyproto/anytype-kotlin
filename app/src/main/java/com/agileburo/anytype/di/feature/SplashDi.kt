package com.agileburo.anytype.di.feature

import com.agileburo.anytype.analytics.base.Analytics
import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.agileburo.anytype.domain.auth.interactor.LaunchAccount
import com.agileburo.anytype.domain.auth.interactor.LaunchWallet
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.device.PathProvider
import com.agileburo.anytype.presentation.splash.SplashViewModelFactory
import com.agileburo.anytype.ui.splash.SplashFragment
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
        analytics: Analytics
    ): SplashViewModelFactory = SplashViewModelFactory(
        checkAuthorizationStatus = checkAuthorizationStatus,
        launchAccount = launchAccount,
        launchWallet = launchWallet,
        analytics = analytics
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
        pathProvider: PathProvider
    ): LaunchAccount =
        LaunchAccount(
            repository = authRepository,
            pathProvider = pathProvider
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
}