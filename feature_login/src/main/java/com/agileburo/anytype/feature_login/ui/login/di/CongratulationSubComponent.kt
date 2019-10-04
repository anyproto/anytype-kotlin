package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.common.ParametrizedProvider
import com.agileburo.anytype.core_utils.di.CoreComponent
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.congratulation.CongratulationFragment
import dagger.Subcomponent

@Subcomponent(modules = [CongratulationModule::class])
abstract class CongratulationSubComponent {

    companion object : ParametrizedProvider<CoreComponent, CongratulationSubComponent>() {
        override fun create(param: CoreComponent) = LoginFeatureComponent.get(param).plus(CongratulationModule())
    }

    abstract fun inject(fragment: CongratulationFragment)
}