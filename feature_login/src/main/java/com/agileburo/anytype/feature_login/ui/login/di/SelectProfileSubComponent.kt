package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.common.ParametrizedProvider
import com.agileburo.anytype.core_utils.di.CoreComponent
import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.profile.SelectAccountFragment
import dagger.Subcomponent

@Subcomponent(modules = [SelectProfileModule::class])
@PerScreen
abstract class SelectProfileSubComponent {
    companion object : ParametrizedProvider<CoreComponent, SelectProfileSubComponent>() {
        override fun create(param: CoreComponent) = LoginFeatureComponent.get(param).plus(SelectProfileModule())
    }

    abstract fun inject(fragment: SelectAccountFragment)
}