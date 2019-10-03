package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.PerScreen
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.pin.EnterPinCodeViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class EnterPinCodeModule {

    @Provides
    @PerScreen
    fun provideEnterPinCodeViewModelFactory(): EnterPinCodeViewModelFactory {
        return EnterPinCodeViewModelFactory()
    }

}