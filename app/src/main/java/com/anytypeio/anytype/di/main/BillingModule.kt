package com.anytypeio.anytype.di.main

import android.content.Context
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module
object BillingModule {

    @Singleton
    @Provides
    fun provideBillingLifecycle(
        context: Context,
        dispatchers: AppCoroutineDispatchers,
        @Named(ConfigModule.DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope
    ): BillingClientLifecycle {
        return BillingClientLifecycle(
            dispatchers = dispatchers,
            applicationContext = context,
            scope = scope
        )
    }
}