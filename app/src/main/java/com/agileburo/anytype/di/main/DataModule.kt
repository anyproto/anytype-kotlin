package com.agileburo.anytype.di.main

import android.content.Context
import android.content.SharedPreferences
import com.agileburo.anytype.data.auth.repo.*
import com.agileburo.anytype.data.auth.repo.block.BlockDataRepository
import com.agileburo.anytype.data.auth.repo.block.BlockDataStoreFactory
import com.agileburo.anytype.data.auth.repo.block.BlockRemote
import com.agileburo.anytype.data.auth.repo.block.BlockRemoteDataStore
import com.agileburo.anytype.device.DefaultPathProvider
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.config.InfrastructureRepository
import com.agileburo.anytype.domain.database.repo.DatabaseRepository
import com.agileburo.anytype.domain.device.PathProvider
import com.agileburo.anytype.middleware.EventProxy
import com.agileburo.anytype.middleware.auth.AuthMiddleware
import com.agileburo.anytype.middleware.block.BlockMiddleware
import com.agileburo.anytype.middleware.interactor.Middleware
import com.agileburo.anytype.middleware.interactor.MiddlewareFactory
import com.agileburo.anytype.middleware.interactor.MiddlewareMapper
import com.agileburo.anytype.middleware.service.DefaultMiddlewareService
import com.agileburo.anytype.middleware.service.MiddlewareService
import com.agileburo.anytype.persistence.db.AnytypeDatabase
import com.agileburo.anytype.persistence.repo.DefaultAuthCache
import com.agileburo.anytype.persistence.repo.DefaultDebugSettingsCache
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
        factory: AuthDataStoreFactory
    ): AuthRepository {
        return AuthDataRepository(
            factory = factory
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
    fun provideMiddlewareService(): MiddlewareService = DefaultMiddlewareService()

    @JvmStatic
    @Provides
    @Singleton
    fun provideDatabaseRepo(): DatabaseRepository = DatabaseDataRepository()
}