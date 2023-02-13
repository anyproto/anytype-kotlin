package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.data.auth.repo.config.GatewayProvider
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.Gateway
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers

@Module
object ConfigModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideGateway(
        provider: ConfigStorage
    ): Gateway = GatewayProvider(provider)

    @JvmStatic
    @Provides
    @Singleton
    fun provideConfigProvider(): ConfigStorage = ConfigStorage.CacheStorage()

    @JvmStatic
    @Provides
    @Singleton
    fun dispatchers(): AppCoroutineDispatchers = AppCoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main
    )
}