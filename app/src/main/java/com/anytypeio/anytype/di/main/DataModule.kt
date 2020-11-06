package com.anytypeio.anytype.di.main

import android.content.Context
import android.content.SharedPreferences
import com.anytypeio.anytype.data.auth.repo.*
import com.anytypeio.anytype.data.auth.repo.block.BlockDataRepository
import com.anytypeio.anytype.data.auth.repo.block.BlockDataStoreFactory
import com.anytypeio.anytype.data.auth.repo.block.BlockRemote
import com.anytypeio.anytype.data.auth.repo.block.BlockRemoteDataStore
import com.anytypeio.anytype.data.auth.repo.config.Configurator
import com.anytypeio.anytype.device.DefaultPathProvider
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.InfrastructureRepository
import com.anytypeio.anytype.domain.database.repo.DatabaseRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.auth.AuthMiddleware
import com.anytypeio.anytype.middleware.block.BlockMiddleware
import com.anytypeio.anytype.middleware.interactor.Middleware
import com.anytypeio.anytype.middleware.interactor.MiddlewareFactory
import com.anytypeio.anytype.middleware.interactor.MiddlewareMapper
import com.anytypeio.anytype.middleware.service.MiddlewareService
import com.anytypeio.anytype.middleware.service.MiddlewareServiceImplementation
import com.anytypeio.anytype.persistence.db.AnytypeDatabase
import com.anytypeio.anytype.persistence.repo.DefaultAuthCache
import com.anytypeio.anytype.persistence.repo.DefaultDebugSettingsCache
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
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
        configurator: Configurator
    ): AuthRepository {
        return AuthDataRepository(
            factory = factory,
            configurator = configurator
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
        prefs: SharedPreferences
    ): AuthCache {
        return DefaultAuthCache(
            db = db,
            prefs = prefs
        )
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideDebugSettingsCache(
        prefs: SharedPreferences
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
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    }

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
        factory: BlockDataStoreFactory
    ): BlockRepository {
        return BlockDataRepository(
            factory = factory
        )
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideBlockDataStoreFactory(
        blockRemoteDataStore: BlockRemoteDataStore
    ): BlockDataStoreFactory {
        return BlockDataStoreFactory(
            remote = blockRemoteDataStore
        )
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideBlockRemoteDataStore(
        remote: BlockRemote
    ): BlockRemoteDataStore {
        return BlockRemoteDataStore(
            remote = remote
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
        mapper: MiddlewareMapper
    ): Middleware = Middleware(service, factory, mapper)

    @JvmStatic
    @Provides
    @Singleton
    fun provideMiddlewareFactory(): MiddlewareFactory = MiddlewareFactory()

    @JvmStatic
    @Provides
    @Singleton
    fun provideMiddlewareMapper(): MiddlewareMapper = MiddlewareMapper()

    @JvmStatic
    @Provides
    @Singleton
    fun provideMiddlewareService(): MiddlewareService = MiddlewareServiceImplementation()

    @JvmStatic
    @Provides
    @Singleton
    fun provideDatabaseRepo(): DatabaseRepository = DatabaseDataRepository()
}