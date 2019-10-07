package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile.ChooseProfileViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class SelectProfileModule {

    @PerScreen
    @Provides
    fun provideSelectProfileViewModelFactory(): ChooseProfileViewModelFactory {
        return ChooseProfileViewModelFactory()
    }

}