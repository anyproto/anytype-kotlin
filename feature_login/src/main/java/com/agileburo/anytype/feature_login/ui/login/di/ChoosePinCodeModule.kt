package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.pin.ChoosePinCodeViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class ChoosePinCodeModule {

    @Provides
    @PerScreen
    fun provideChoosePinCodeViewModelFactory(): ChoosePinCodeViewModelFactory {
        return ChoosePinCodeViewModelFactory()
    }

}