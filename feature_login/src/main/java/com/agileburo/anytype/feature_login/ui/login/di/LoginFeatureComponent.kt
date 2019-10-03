package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.PerFeature
import com.agileburo.anytype.core_utils.di.Provider
import dagger.Component

@Component(modules = [LoginFeatureModule::class])
@PerFeature
abstract class LoginFeatureComponent {

    companion object : Provider<LoginFeatureComponent>() {
        override fun create(): LoginFeatureComponent {
            return DaggerLoginFeatureComponent
                .builder()
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