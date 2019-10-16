package com.agileburo.anytype.feature_profile.di

import com.agileburo.anytype.core_utils.common.ParametrizedProvider
import com.agileburo.anytype.core_utils.di.CoreComponent
import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_profile.presentation.ui.ProfileFragment
import dagger.Subcomponent


@Subcomponent(
    modules = [ProfileModule::class]
)
@PerScreen
abstract class ProfileSubComponent {

    abstract fun inject(fragment: ProfileFragment)

    companion object : ParametrizedProvider<CoreComponent, ProfileSubComponent>() {
        override fun create(param: CoreComponent) = ProfileFeatureComponent.get(param).plus(ProfileModule())
    }

}