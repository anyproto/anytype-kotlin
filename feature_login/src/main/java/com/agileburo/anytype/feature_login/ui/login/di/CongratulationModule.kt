package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.PerScreen
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.congratulation.CongratulationViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class CongratulationModule {

    @PerScreen
    @Provides
    fun provideCongratulationViewModelFactory(): CongratulationViewModelFactory {
        return CongratulationViewModelFactory()
    }

}