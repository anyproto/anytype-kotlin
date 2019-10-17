package com.agileburo.anytype.feature_login.ui.login.di

import android.content.Context
import android.content.SharedPreferences
import com.agileburo.anytype.core_utils.data.UserCache
import com.agileburo.anytype.core_utils.di.scope.PerFeature
import com.agileburo.anytype.feature_login.ui.login.data.AuthCacheDataStore
import com.agileburo.anytype.feature_login.ui.login.data.AuthCacheImpl
import com.agileburo.anytype.feature_login.ui.login.data.AuthDataRepository
import com.agileburo.anytype.feature_login.ui.login.data.UserDataRepository
import com.agileburo.anytype.feature_login.ui.login.device.DefaultPathProvider
import com.agileburo.anytype.feature_login.ui.login.domain.common.PathProvider
import com.agileburo.anytype.feature_login.ui.login.domain.common.Session
import com.agileburo.anytype.feature_login.ui.login.domain.repository.AuthRepository
import com.agileburo.anytype.feature_login.ui.login.domain.repository.UserRepository
import com.agileburo.anytype.middleware.EventProxy
import com.agileburo.anytype.middleware.interactor.Handler
import com.agileburo.anytype.middleware.interactor.Middleware
import dagger.Module
import dagger.Provides

@Module
class LoginFeatureModule {

    @PerFeature
    @Provides
    fun provideAuthRepository(
        prefs: SharedPreferences,
        middleware: Middleware
    ): AuthRepository {
        return AuthDataRepository(
            cache = AuthCacheDataStore(
                authCache = AuthCacheImpl(prefs)
            ),
            middleware = middleware
        )
    }

    @PerFeature
    @Provides
    fun provideUserRepository(
        middleware: Middleware,
        proxy: EventProxy,
        cache: UserCache
    ): UserRepository {
        return UserDataRepository(
            middleware = middleware,
            proxy = proxy,
            cache = cache
        )
    }

    @PerFeature
    @Provides
    fun provideSession(): Session {
        return Session()
    }

    @PerFeature
    @Provides
    fun providePathProvider(context: Context): PathProvider {
        return DefaultPathProvider(context)
    }

    @PerFeature
    @Provides
    fun provideMiddleware(): Middleware {
        return Middleware()
    }

    @PerFeature
    @Provides
    fun provideEventProxy(): EventProxy {
        return Handler()
    }

}