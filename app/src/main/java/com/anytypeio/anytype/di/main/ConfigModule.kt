package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.data.auth.repo.config.GatewayProvider
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.config.TechSpaceProvider
import com.anytypeio.anytype.domain.debugging.Logger
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
object ConfigModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideGateway(
        configStorage: ConfigStorage,
        logger: Logger
    ): Gateway = GatewayProvider(
        configStorage = configStorage,
        logger = logger
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideConfigProvider(): ConfigStorage = ConfigStorage.CacheStorage()

    @JvmStatic
    @Provides
    @Singleton
    fun provideTechSpaceProvider(
        configStorage: ConfigStorage
    ): TechSpaceProvider = configStorage

    @JvmStatic
    @Provides
    @Singleton
    fun dispatchers(): AppCoroutineDispatchers = AppCoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main
    )

    @JvmStatic
    @Provides
    @Singleton
    @Named(DEFAULT_APP_COROUTINE_SCOPE)
    fun applicationCoroutineScope() : CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )

    @JvmStatic
    @Provides
    @Singleton
    fun awaitAccountStartedManager(): AwaitAccountStartManager = AwaitAccountStartManager.Default

    const val DEFAULT_APP_COROUTINE_SCOPE = "Default application coroutine scope"
}