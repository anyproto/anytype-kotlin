package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.PerFeature
import com.agileburo.anytype.feature_login.ui.login.data.AuthCacheDataStore
import com.agileburo.anytype.feature_login.ui.login.data.AuthCacheImpl
import com.agileburo.anytype.feature_login.ui.login.data.AuthDataRepository
import com.agileburo.anytype.feature_login.ui.login.data.UserDataRepository
import com.agileburo.anytype.feature_login.ui.login.device.DefaultPathProvider
import com.agileburo.anytype.feature_login.ui.login.domain.common.PathProvider
import com.agileburo.anytype.feature_login.ui.login.domain.common.Session
import com.agileburo.anytype.feature_login.ui.login.domain.repository.AuthRepository
import com.agileburo.anytype.feature_login.ui.login.domain.repository.UserRepository
import dagger.Module
import dagger.Provides

@Module
class LoginFeatureModule {

    @PerFeature
    @Provides
    fun provideAuthRepository(): AuthRepository {
        return AuthDataRepository(
            cache = AuthCacheDataStore(
                authCache = AuthCacheImpl()
            )
        )
    }

    @PerFeature
    @Provides
    fun provideUserRepository(): UserRepository {
        return UserDataRepository()
    }

    @PerFeature
    @Provides
    fun provideSession(): Session {
        return Session()
    }

    @PerFeature
    @Provides
    fun providePathProvider(): PathProvider {
        return DefaultPathProvider()
    }

}