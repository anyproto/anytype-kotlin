package com.anytypeio.anytype.di.main

import android.content.Context
import android.content.SharedPreferences
import com.anytypeio.anytype.app.DefaultAppActionManager
import com.anytypeio.anytype.app.DefaultInitialParamsProvider
import com.anytypeio.anytype.core_utils.tools.ThreadInfo
import com.anytypeio.anytype.data.auth.repo.AuthCache
import com.anytypeio.anytype.data.auth.repo.AuthCacheDataStore
import com.anytypeio.anytype.data.auth.repo.AuthDataRepository
import com.anytypeio.anytype.data.auth.repo.AuthDataStoreFactory
import com.anytypeio.anytype.data.auth.repo.AuthRemote
import com.anytypeio.anytype.data.auth.repo.AuthRemoteDataStore
import com.anytypeio.anytype.data.auth.repo.DebugSettingsCache
import com.anytypeio.anytype.data.auth.repo.InfrastructureDataRepository
import com.anytypeio.anytype.data.auth.repo.UserSettingsCache
import com.anytypeio.anytype.data.auth.repo.UserSettingsDataRepository
import com.anytypeio.anytype.data.auth.repo.block.BlockDataRepository
import com.anytypeio.anytype.data.auth.repo.block.BlockRemote
import com.anytypeio.anytype.data.auth.repo.unsplash.UnsplashDataRepository
import com.anytypeio.anytype.data.auth.repo.unsplash.UnsplashRemote
import com.anytypeio.anytype.data.auth.types.DefaultObjectTypesProvider
import com.anytypeio.anytype.device.AppStateService
import com.anytypeio.anytype.device.BuildProvider
import com.anytypeio.anytype.device.DefaultBuildProvider
import com.anytypeio.anytype.device.DefaultPathProvider
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.InfrastructureRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.DebugConfig
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.platform.InitialParamsProvider
import com.anytypeio.anytype.domain.unsplash.UnsplashRepository
import com.anytypeio.anytype.domain.invite.SpaceInviteLinkStore
import com.anytypeio.anytype.domain.invite.SpaceInviteLinkStoreImpl
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.UnsplashMiddleware
import com.anytypeio.anytype.middleware.auth.AuthMiddleware
import com.anytypeio.anytype.middleware.block.BlockMiddleware
import com.anytypeio.anytype.middleware.interactor.Middleware
import com.anytypeio.anytype.middleware.interactor.MiddlewareFactory
import com.anytypeio.anytype.middleware.interactor.MiddlewareProtobufLogger
import com.anytypeio.anytype.middleware.interactor.ProtobufConverterProvider
import com.anytypeio.anytype.middleware.service.MiddlewareService
import com.anytypeio.anytype.middleware.service.MiddlewareServiceImplementation
import com.anytypeio.anytype.persistence.db.AnytypeDatabase
import com.anytypeio.anytype.device.providers.AppDefaultDateFormatProvider
import com.anytypeio.anytype.device.providers.AppDefaultDateFormatProviderImpl
import com.anytypeio.anytype.di.main.ConfigModule.DEFAULT_APP_COROUTINE_SCOPE
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.device.SetAppState
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.network.NetworkModeProvider
import com.anytypeio.anytype.persistence.repo.DefaultAuthCache
import com.anytypeio.anytype.persistence.repo.DefaultDebugSettingsCache
import com.anytypeio.anytype.persistence.repo.DefaultUserSettingsCache
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import com.anytypeio.anytype.providers.DefaultUriFileProvider
import com.anytypeio.anytype.security.KeystoreManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module(includes = [DataModule.Bindings::class])
object DataModule {

    @JvmStatic
    @Provides
    fun providePathProvider(context: Context): PathProvider {
        return DefaultPathProvider(context)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideAuthRepository(
        factory: AuthDataStoreFactory,
        debugConfig: DebugConfig
    ): AuthRepository {
        return AuthDataRepository(
            factory = factory,
            debugConfig = debugConfig
        )
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideAuthDataStoreFactory(
        authCacheDataStore: AuthCacheDataStore,
        authRemoteDataStore: AuthRemoteDataStore
    ): AuthDataStoreFactory {
        return AuthDataStoreFactory(
            cache = authCacheDataStore,
            remote = authRemoteDataStore
        )
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideAuthCacheDataStore(
        authCache: AuthCache
    ): AuthCacheDataStore {
        return AuthCacheDataStore(
            cache = authCache
        )
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideAuthCache(
        db: AnytypeDatabase,
        @Named("default") defaultPrefs: SharedPreferences,
        @Named("encrypted") encryptedPrefs: SharedPreferences,
        networkModeProvider: NetworkModeProvider
    ): AuthCache {
        return DefaultAuthCache(
            db = db,
            defaultPrefs = defaultPrefs,
            encryptedPrefs = encryptedPrefs,
            networkModeProvider = networkModeProvider
        )
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideDebugSettingsCache(
        @Named("default") prefs: SharedPreferences,
    ): DebugSettingsCache {
        return DefaultDebugSettingsCache(
            prefs = prefs
        )
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideInfrastructureRepository(
        cache: DebugSettingsCache
    ): InfrastructureRepository {
        return InfrastructureDataRepository(
            cache = cache
        )
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideAnytypeDatabase(context: Context): AnytypeDatabase {
        return AnytypeDatabase.get(context)
    }

    @JvmStatic
    @Provides
    @Singleton
    @Named("default")
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    }

    @JvmStatic
    @Provides
    @Singleton
    @Named("encrypted")
    fun provideEncryptedSharedPreferences(
        keystoreManager: KeystoreManager
    ): SharedPreferences = keystoreManager.initializeEncryptedPreferences()

    @JvmStatic
    @Provides
    @Singleton
    fun provideAuthRemoteDataStore(
        authRemote: AuthRemote
    ): AuthRemoteDataStore {
        return AuthRemoteDataStore(
            authRemote = authRemote
        )
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideAuthRemote(
        middleware: Middleware,
        proxy: EventProxy
    ): AuthRemote {
        return AuthMiddleware(
            middleware = middleware,
            events = proxy
        )
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideBlockRepository(
        blockRemote: BlockRemote,
        debugConfig: DebugConfig,
        logger: Logger
    ): BlockRepository {
        return BlockDataRepository(
            remote = blockRemote,
            debugConfig = debugConfig,
            logger = logger
        )
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideBlockRemote(
        middleware: Middleware
    ): BlockRemote = BlockMiddleware(middleware = middleware)

    @JvmStatic
    @Provides
    @Singleton
    fun provideMiddleware(
        service: MiddlewareService,
        factory: MiddlewareFactory,
        logger: MiddlewareProtobufLogger,
        protobufConverter: ProtobufConverterProvider,
        threadInfo: ThreadInfo
    ): Middleware = Middleware(
        service = service,
        factory = factory,
        logger = logger,
        protobufConverter = protobufConverter,
        threadInfo = threadInfo
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideMiddlewareFactory(): MiddlewareFactory = MiddlewareFactory()

    @JvmStatic
    @Provides
    @Singleton
    fun provideObjectTypesProvider(): ObjectTypesProvider {
        return DefaultObjectTypesProvider()
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideUserSettingsCache(
        @Named("default") prefs: SharedPreferences,
        context: Context,
        appDefaultDateFormatProvider: AppDefaultDateFormatProvider
    ): UserSettingsCache = DefaultUserSettingsCache(
        prefs = prefs,
        context = context,
        appDefaultDateFormatProvider = appDefaultDateFormatProvider
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideUserSettingsRepo(
        cache: UserSettingsCache
    ): UserSettingsRepository = UserSettingsDataRepository(cache)

    @JvmStatic
    @Provides
    @Singleton
    fun provideAppActionManager(context: Context): AppActionManager = DefaultAppActionManager(
        context = context
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideObjectStore(): ObjectStore = DefaultObjectStore()

    //region Unsplash

    @JvmStatic
    @Provides
    @Singleton
    fun provideUnsplashRepo(
        remote: UnsplashRemote
    ): UnsplashRepository = UnsplashDataRepository(
        remote = remote
    )
    //endregion

    @JvmStatic
    @Provides
    @Singleton
    fun provideBuildProvider(): BuildProvider = DefaultBuildProvider()

    @JvmStatic
    @Provides
    @Singleton
    fun provideMetricsProvider(context: Context): InitialParamsProvider =
        DefaultInitialParamsProvider(context)

    @JvmStatic
    @Provides
    @Singleton
    fun provideFileProvider(
        context: Context,
        logger: Logger
    ): UriFileProvider = DefaultUriFileProvider(context, logger)

    @JvmStatic
    @Provides
    @Singleton
    fun provideAppDefaultDateFormatProvider(
        localeProvider: LocaleProvider
    ): AppDefaultDateFormatProvider = AppDefaultDateFormatProviderImpl(
        localeProvider = localeProvider
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideSetDeviceState(
        blockRepository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetAppState = SetAppState(blockRepository, dispatchers)

    @JvmStatic
    @Provides
    @Singleton
    fun provideAppStateService(
        setAppState: SetAppState,
        @Named(DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope
    ): AppStateService = AppStateService(
        setAppState = setAppState,
        coroutineScope = scope
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideSpaceInviteLinkStore(
    ): SpaceInviteLinkStore = SpaceInviteLinkStoreImpl()

    @Provides
    @Singleton
    fun provideKeystoreManager(context: Context): KeystoreManager = KeystoreManager(context)

    @Module
    interface Bindings {

        @Binds
        @Singleton
        fun bindUnsplashRemote(middleware: UnsplashMiddleware): UnsplashRemote

        @Binds
        @Singleton
        fun bindMiddlewareService(middleware: MiddlewareServiceImplementation): MiddlewareService
    }
}

const val ENCRYPTED_PREFS_NAME = "encrypted_prefs"