package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.data.auth.repo.config.GatewayProvider
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.Gateway
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

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
}