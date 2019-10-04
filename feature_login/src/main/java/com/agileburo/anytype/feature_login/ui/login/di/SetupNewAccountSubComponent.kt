package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.common.ParametrizedProvider
import com.agileburo.anytype.core_utils.di.CoreComponent
import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.setup.SetupNewAccountFragment
import dagger.Subcomponent


@Subcomponent(modules = [SetupNewAccountModule::class])
@PerScreen
abstract class SetupNewAccountSubComponent {
    companion object : ParametrizedProvider<CoreComponent, SetupNewAccountSubComponent>() {
        override fun create(param: CoreComponent) = LoginFeatureComponent.get(param).plus(SetupNewAccountModule())
    }

    abstract fun inject(fragment: SetupNewAccountFragment)
}