package com.agileburo.anytype.di.main

import com.agileburo.anytype.data.auth.repo.config.Configuration
import com.agileburo.anytype.data.auth.repo.config.Configurator
import com.agileburo.anytype.domain.config.Config
import com.agileburo.anytype.middleware.config.DefaultConfigurator
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
    fun provideConfigurator(): Configurator {
        return DefaultConfigurator()
    }
}