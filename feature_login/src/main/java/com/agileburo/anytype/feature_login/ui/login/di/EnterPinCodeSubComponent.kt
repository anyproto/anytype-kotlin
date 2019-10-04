package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.common.ParametrizedProvider
import com.agileburo.anytype.core_utils.di.CoreComponent
import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.pin.EnterPinCodeFragment
import dagger.Subcomponent

@Subcomponent(modules = [EnterPinCodeModule::class])
@PerScreen
abstract class EnterPinCodeSubComponent {

    companion object : ParametrizedProvider<CoreComponent, EnterPinCodeSubComponent>() {
        override fun create(param: CoreComponent) = LoginFeatureComponent.get(param).plus(EnterPinCodeModule())
    }

    abstract fun inject(fragment: EnterPinCodeFragment)

}