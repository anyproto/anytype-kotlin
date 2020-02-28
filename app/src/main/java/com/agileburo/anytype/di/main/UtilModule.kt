package com.agileburo.anytype.di.main

import com.agileburo.anytype.domain.config.Config
import com.agileburo.anytype.domain.misc.UrlBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UtilModule {

    @Provides
    @Singleton
    fun provideUrlBuilder(config: Config): UrlBuilder {
        return UrlBuilder(config)
    }
}