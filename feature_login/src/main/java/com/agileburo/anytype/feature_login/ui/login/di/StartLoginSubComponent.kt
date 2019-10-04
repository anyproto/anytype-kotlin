package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.common.ParametrizedProvider
import com.agileburo.anytype.core_utils.di.CoreComponent
import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.start.StartLoginFragment
import dagger.Subcomponent

@Subcomponent(modules = [StartLoginModule::class])
@PerScreen
abstract class StartLoginSubComponent {

    companion object : ParametrizedProvider<CoreComponent, StartLoginSubComponent>() {
        override fun create(param: CoreComponent) = LoginFeatureComponent.get(param).plus(StartLoginModule())
    }

    abstract fun inject(fragment: StartLoginFragment)

}