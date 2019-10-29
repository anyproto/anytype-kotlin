package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.agileburo.anytype.domain.auth.repo.AuthRepository
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
class SplashModule {

    @PerScreen
    @Provides
    fun provideSplashViewModelFactory(checkAuthorizationStatus: CheckAuthorizationStatus) =
        SplashViewModelFactory(checkAuthorizationStatus)

    @PerScreen
    @Provides
    fun provideCheckAuthorizationStatus(authRepository: AuthRepository) =
        CheckAuthorizationStatus(authRepository)
}