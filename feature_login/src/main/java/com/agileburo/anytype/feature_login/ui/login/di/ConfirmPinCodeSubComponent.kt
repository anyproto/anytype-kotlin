package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.PerScreen
import com.agileburo.anytype.core_utils.di.Provider
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.pin.ConfirmPinCodeFragment
import dagger.Subcomponent

@Subcomponent(modules = [ConfirmPinCodeModule::class])
@PerScreen
abstract class ConfirmPinCodeSubComponent {

    companion object : Provider<ConfirmPinCodeSubComponent>() {
        override fun create() = LoginFeatureComponent.get().plus(ConfirmPinCodeModule())
    }

    abstract fun inject(fragment: ConfirmPinCodeFragment)

}