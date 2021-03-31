package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.data.auth.repo.config.Configuration
import com.anytypeio.anytype.data.auth.repo.config.Configurator
import com.anytypeio.anytype.data.auth.repo.config.GatewayProvider
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.middleware.config.DefaultConfigurator
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object ConfigModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideApplicationConfig(configurator: Configurator): Config {
        return Configuration(configurator).init()
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideGateway(configurator: Configurator): Gateway = GatewayProvider(configurator)

    @JvmStatic
    @Provides
    @Singleton
    fun provideConfigurator(): Configurator {
        return DefaultConfigurator()
    }
}