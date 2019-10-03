package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.PerScreen
import com.agileburo.anytype.core_utils.di.Provider
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.pin.EnterPinCodeFragment
import dagger.Subcomponent

@Subcomponent(modules = [EnterPinCodeModule::class])
@PerScreen
abstract class EnterPinCodeSubComponent {

    companion object : Provider<EnterPinCodeSubComponent>() {
        override fun create() = LoginFeatureComponent.get().plus(EnterPinCodeModule())
    }

    abstract fun inject(fragment: EnterPinCodeFragment)

}