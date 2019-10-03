package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.PerScreen
import com.agileburo.anytype.core_utils.di.Provider
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.pin.ChoosePinCodeFragment
import dagger.Subcomponent

@Subcomponent(modules = [ChoosePinCodeModule::class])
@PerScreen
abstract class ChoosePinCodeSubComponent {

    companion object : Provider<ChoosePinCodeSubComponent>() {
        override fun create() = LoginFeatureComponent.get().plus(ChoosePinCodeModule())
    }

    abstract fun inject(fragment: ChoosePinCodeFragment)

}