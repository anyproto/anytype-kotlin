package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.common.ParametrizedProvider
import com.agileburo.anytype.core_utils.di.CoreComponent
import com.agileburo.anytype.core_utils.di.scope.PerFeature
import dagger.Component

@Component(modules = [LoginFeatureModule::class], dependencies = [CoreComponent::class])
@PerFeature
abstract class LoginFeatureComponent {

    companion object : ParametrizedProvider<CoreComponent, LoginFeatureComponent>() {
        override fun create(param: CoreComponent): LoginFeatureComponent {
            return DaggerLoginFeatureComponent
                .builder()
                .coreComponent(param)
                .loginFeatureModule(LoginFeatureModule())
                .build()
        }

    }

    abstract fun plus(module: StartLoginModule): StartLoginSubComponent
    abstract fun plus(module: CreateProfileModule): CreateProfileSubComponent
    abstract fun plus(module: ChoosePinCodeModule): ChoosePinCodeSubComponent
    abstract fun plus(module: ConfirmPinCodeModule): ConfirmPinCodeSubComponent
    abstract fun plus(module: EnterPinCodeModule): EnterPinCodeSubComponent
    abstract fun plus(module: CongratulationModule): CongratulationSubComponent
    abstract fun plus(module: SetupNewAccountModule): SetupNewAccountSubComponent
    abstract fun plus(module: SetupSelectedAccountModule): SetupSelectedAccountSubComponent
    abstract fun plus(module: KeychainLoginModule): KeychainLoginSubComponent
}