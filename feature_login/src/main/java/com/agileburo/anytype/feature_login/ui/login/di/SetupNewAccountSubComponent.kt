package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.PerScreen
import com.agileburo.anytype.core_utils.di.Provider
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.setup.SetupNewAccountFragment
import dagger.Subcomponent


@Subcomponent(modules = [SetupNewAccountModule::class])
@PerScreen
abstract class SetupNewAccountSubComponent {
    companion object : Provider<SetupNewAccountSubComponent>() {
        override fun create() = LoginFeatureComponent.get().plus(SetupNewAccountModule())
    }

    abstract fun inject(fragment: SetupNewAccountFragment)
}