package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.PerScreen
import com.agileburo.anytype.core_utils.di.Provider
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.profile.CreateProfileFragment
import dagger.Subcomponent

@Subcomponent(modules = [CreateProfileModule::class])
@PerScreen
abstract class CreateProfileSubComponent {

    companion object : Provider<CreateProfileSubComponent>() {
        override fun create() = LoginFeatureComponent.get().plus(CreateProfileModule())
    }

    abstract fun inject(fragment: CreateProfileFragment)

}