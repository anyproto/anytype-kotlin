package com.agileburo.anytype.di.main

import com.agileburo.anytype.domain.config.Gateway
import com.agileburo.anytype.domain.misc.UrlBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object UtilModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideUrlBuilder(gateway: Gateway): UrlBuilder = UrlBuilder(gateway)
}