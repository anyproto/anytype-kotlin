package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.common.ParametrizedProvider
import com.agileburo.anytype.core_utils.di.CoreComponent
import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.profile.CreateAccountFragment
import dagger.Subcomponent

@Subcomponent(modules = [CreateProfileModule::class])
@PerScreen
abstract class CreateProfileSubComponent {

    companion object : ParametrizedProvider<CoreComponent, CreateProfileSubComponent>() {
        override fun create(param: CoreComponent) = LoginFeatureComponent.get(param).plus(CreateProfileModule())
    }

    abstract fun inject(fragment: CreateAccountFragment)

}