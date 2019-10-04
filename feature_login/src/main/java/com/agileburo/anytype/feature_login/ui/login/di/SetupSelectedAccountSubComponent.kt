package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.common.ParametrizedProvider
import com.agileburo.anytype.core_utils.di.CoreComponent
import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.setup.SetupSelectedAccountFragment
import dagger.Subcomponent

@Subcomponent(modules = [SetupSelectedAccountModule::class])
@PerScreen
abstract class SetupSelectedAccountSubComponent {
    companion object : ParametrizedProvider<CoreComponent, SetupSelectedAccountSubComponent>() {
        override fun create(param: CoreComponent) = LoginFeatureComponent.get(param).plus(SetupSelectedAccountModule())
    }

    abstract fun inject(fragment: SetupSelectedAccountFragment)
}