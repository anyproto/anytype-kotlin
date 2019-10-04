package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.common.ParametrizedProvider
import com.agileburo.anytype.core_utils.di.CoreComponent
import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.pin.ChoosePinCodeFragment
import dagger.Subcomponent

@Subcomponent(modules = [ChoosePinCodeModule::class])
@PerScreen
abstract class ChoosePinCodeSubComponent {

    companion object : ParametrizedProvider<CoreComponent, ChoosePinCodeSubComponent>() {
        override fun create(param: CoreComponent) = LoginFeatureComponent.get(param).plus(ChoosePinCodeModule())
    }

    abstract fun inject(fragment: ChoosePinCodeFragment)

}