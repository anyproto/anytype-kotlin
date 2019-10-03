package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.PerScreen
import com.agileburo.anytype.core_utils.di.Provider
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.setup.SetupSelectedAccountFragment
import dagger.Subcomponent

@Subcomponent(modules = [SetupSelectedAccountModule::class])
@PerScreen
abstract class SetupSelectedAccountSubComponent {
    companion object : Provider<SetupSelectedAccountSubComponent>() {
        override fun create() = LoginFeatureComponent.get().plus(SetupSelectedAccountModule())
    }

    abstract fun inject(fragment: SetupSelectedAccountFragment)
}