package com.agileburo.anytype.feature_profile.di

import com.agileburo.anytype.core_utils.common.ParametrizedProvider
import com.agileburo.anytype.core_utils.di.CoreComponent
import com.agileburo.anytype.core_utils.di.scope.PerFeature
import dagger.Component

@Component(
    modules = [ProfileFeatureModule::class],
    dependencies = [CoreComponent::class]
)
@PerFeature
abstract class ProfileFeatureComponent {

    companion object : ParametrizedProvider<CoreComponent, ProfileFeatureComponent>() {

        override fun create(param: CoreComponent): ProfileFeatureComponent {
            return DaggerProfileFeatureComponent
                .builder()
                .coreComponent(param)
                .build()
        }
    }

    abstract fun plus(module: ProfileModule): ProfileSubComponent

}