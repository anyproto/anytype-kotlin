package com.anytypeio.anytype.di.feature.payments

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.ui.payments.PaymentsFragment
import com.anytypeio.anytype.viewmodel.PaymentsViewModelFactory
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [PaymentsComponentDependencies::class],
    modules = [
        PaymentsModule::class,
        PaymentsModule.Declarations::class
    ]
)
@PerScreen
interface PaymentsComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: PaymentsComponentDependencies): PaymentsComponent
    }

    fun inject(fragment: PaymentsFragment)
}

@Module
object PaymentsModule {

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: PaymentsViewModelFactory
        ): ViewModelProvider.Factory

    }
}

interface PaymentsComponentDependencies : ComponentDependencies {
    fun analytics(): Analytics
}