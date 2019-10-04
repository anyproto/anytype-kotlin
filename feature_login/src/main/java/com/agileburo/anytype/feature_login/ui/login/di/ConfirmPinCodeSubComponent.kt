package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.common.ParametrizedProvider
import com.agileburo.anytype.core_utils.di.CoreComponent
import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.pin.ConfirmPinCodeFragment
import dagger.Subcomponent

@Subcomponent(modules = [ConfirmPinCodeModule::class])
@PerScreen
abstract class ConfirmPinCodeSubComponent {

    companion object : ParametrizedProvider<CoreComponent, ConfirmPinCodeSubComponent>() {
        override fun create(param: CoreComponent) = LoginFeatureComponent.get(param).plus(ConfirmPinCodeModule())
    }

    abstract fun inject(fragment: ConfirmPinCodeFragment)

}