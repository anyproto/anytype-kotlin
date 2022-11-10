package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.app.DefaultFeatureToggles
import com.anytypeio.anytype.core_utils.tools.DefaultUrlValidator
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.core_utils.tools.UrlValidator
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.middleware.interactor.MiddlewareProtobufLogger
import com.anytypeio.anytype.middleware.interactor.ProtobufConverterProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [UtilModule.Bindings::class])
object UtilModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideUrlBuilder(gateway: Gateway): UrlBuilder = UrlBuilder(gateway)


    @Module
    interface Bindings {

        @Binds
        @Singleton
        fun bindUrlValidator(applicator: DefaultUrlValidator): UrlValidator

        @Binds
        @Singleton
        fun bindFeatureToggles(applicator: DefaultFeatureToggles): FeatureToggles

        @Binds
        @Singleton
        fun bindMiddlewareProtobufLogger(logger: MiddlewareProtobufLogger.Impl): MiddlewareProtobufLogger

        @Binds
        @Singleton
        fun bindProtobufConverterProvider(provider: ProtobufConverterProvider.Impl): ProtobufConverterProvider
    }
}