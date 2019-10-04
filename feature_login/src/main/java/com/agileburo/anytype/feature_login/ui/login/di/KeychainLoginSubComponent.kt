package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.common.ParametrizedProvider
import com.agileburo.anytype.core_utils.di.CoreComponent
import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.keychain.KeychainLoginFragment
import dagger.Subcomponent

@Subcomponent(modules = [KeychainLoginModule::class])
@PerScreen
abstract class KeychainLoginSubComponent {

    companion object : ParametrizedProvider<CoreComponent, KeychainLoginSubComponent>() {
        override fun create(param: CoreComponent) = LoginFeatureComponent.get(param).plus(KeychainLoginModule())
    }

    abstract fun inject(fragment: KeychainLoginFragment)

}