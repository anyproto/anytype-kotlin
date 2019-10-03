package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.Provider
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.congratulation.CongratulationFragment
import dagger.Subcomponent

@Subcomponent(modules = [CongratulationModule::class])
abstract class CongratulationSubComponent {

    companion object : Provider<CongratulationSubComponent>() {
        override fun create() = LoginFeatureComponent.get().plus(CongratulationModule())
    }

    abstract fun inject(fragment: CongratulationFragment)
}