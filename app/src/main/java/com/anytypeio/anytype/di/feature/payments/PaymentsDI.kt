package com.anytypeio.anytype.di.feature.payments

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PurchasesUpdatedListener
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.ui.payments.PaymentsFragment
import com.anytypeio.anytype.viewmodel.PaymentsViewModelFactory
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

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
        fun create(
            @BindsInstance listener: PurchasesUpdatedListener,
            dependencies: PaymentsComponentDependencies): PaymentsComponent
    }

    fun inject(fragment: PaymentsFragment)
}

@Module
object PaymentsModule {

    @PerScreen
    @Provides
    fun provideBillingService(
        context: Context,
        listener: PurchasesUpdatedListener,
    ): BillingClient {
        return BillingClient
            .newBuilder(context)
            .setListener(listener)
            .enablePendingPurchases()
            .build()
    }

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
    fun context(): Context
}