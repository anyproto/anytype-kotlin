package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.PerScreen
import com.agileburo.anytype.core_utils.di.Provider
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.keychain.KeychainLoginFragment
import dagger.Subcomponent

@Subcomponent(modules = [KeychainLoginModule::class])
@PerScreen
abstract class KeychainLoginSubComponent {

    companion object : Provider<KeychainLoginSubComponent>() {
        override fun create() = LoginFeatureComponent.get().plus(KeychainLoginModule())
    }

    abstract fun inject(fragment: KeychainLoginFragment)

}