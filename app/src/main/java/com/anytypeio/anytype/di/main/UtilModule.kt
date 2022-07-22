package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.core_utils.tools.DefaultUrlValidator
import com.anytypeio.anytype.core_utils.tools.UrlValidator
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.misc.UrlBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object UtilModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideUrlBuilder(gateway: Gateway): UrlBuilder = UrlBuilder(gateway)

    @JvmStatic
    @Provides
    @Singleton
    fun provideUrlValidator() : UrlValidator = DefaultUrlValidator()
}