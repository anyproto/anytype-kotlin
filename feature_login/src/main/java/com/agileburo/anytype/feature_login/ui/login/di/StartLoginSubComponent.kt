package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.PerScreen
import com.agileburo.anytype.core_utils.di.Provider
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.start.StartLoginFragment
import dagger.Subcomponent

@Subcomponent(modules = [StartLoginModule::class])
@PerScreen
abstract class StartLoginSubComponent {

    companion object : Provider<StartLoginSubComponent>() {
        override fun create() = LoginFeatureComponent.get().plus(StartLoginModule())
    }

    abstract fun inject(fragment: StartLoginFragment)

}