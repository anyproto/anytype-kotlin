package com.anytypeio.anytype.di.main

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.app.DefaultFeatureToggles
import com.anytypeio.anytype.app.TogglePrefs
import com.anytypeio.anytype.core_utils.tools.AppInfo
import com.anytypeio.anytype.core_utils.tools.DefaultAppInfo
import com.anytypeio.anytype.core_utils.tools.DefaultThreadInfo
import com.anytypeio.anytype.core_utils.tools.DefaultUrlValidator
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.core_utils.tools.ThreadInfo
import com.anytypeio.anytype.core_utils.tools.UrlValidator
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.debugging.DebugConfig
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.middleware.interactor.MiddlewareProtobufLogger
import com.anytypeio.anytype.middleware.interactor.ProtobufConverterProvider
import com.anytypeio.anytype.other.BasicLogger
import com.anytypeio.anytype.other.DefaultDebugConfig
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

    @JvmStatic
    @Provides
    @Singleton
    @TogglePrefs
    fun providesSharedPreferences(
        context: Context
    ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @JvmStatic
    @Provides
    @Singleton
    fun provideAppInfo(): AppInfo = DefaultAppInfo(BuildConfig.VERSION_NAME)

    @Module
    interface Bindings {

        @Binds
        @Singleton
        fun bindLogger(logger: BasicLogger): Logger

        @Binds
        @Singleton
        fun bindDebugConfig(config: DefaultDebugConfig): DebugConfig

        @Binds
        @Singleton
        fun bindThreadInfo(info: DefaultThreadInfo): ThreadInfo

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