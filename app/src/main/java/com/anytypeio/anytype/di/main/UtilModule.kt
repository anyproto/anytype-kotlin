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
import com.anytypeio.anytype.device.providers.DateProviderImpl
import com.anytypeio.anytype.di.main.ConfigModule.DEFAULT_APP_COROUTINE_SCOPE
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.debugging.DebugConfig
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DateTypeNameProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.FieldParserImpl
import com.anytypeio.anytype.domain.vault.ObserveVaultSettings
import com.anytypeio.anytype.middleware.interactor.MiddlewareProtobufLogger
import com.anytypeio.anytype.middleware.interactor.ProtobufConverterProvider
import com.anytypeio.anytype.other.BasicLogger
import com.anytypeio.anytype.other.DefaultDateTypeNameProvider
import com.anytypeio.anytype.other.DefaultDebugConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import java.time.ZoneId
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

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

    @JvmStatic
    @Provides
    @Singleton
    fun provideDateProvider(
        localeProvider: LocaleProvider,
        observeVaultSettings: ObserveVaultSettings,
        @Named(DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope
    ): DateProvider = DateProviderImpl(
        defaultZoneId = ZoneId.systemDefault(),
        localeProvider = localeProvider,
        vaultSettings = observeVaultSettings,
        scope = scope
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideFieldsProvider(
        dateProvider: DateProvider,
        logger: Logger
    ): FieldParser = FieldParserImpl(dateProvider, logger)

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

        @Binds
        @Singleton
        fun bindDateTypeNameProvider(provider: DefaultDateTypeNameProvider): DateTypeNameProvider
    }
}