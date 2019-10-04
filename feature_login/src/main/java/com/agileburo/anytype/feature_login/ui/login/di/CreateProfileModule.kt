package com.agileburo.anytype.feature_login.ui.login.di

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_login.ui.login.domain.common.Session
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile.CreateProfileViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class CreateProfileModule {

    @PerScreen
    @Provides
    fun provideCreateProfileViewModelFactory(
        session: Session
    ): CreateProfileViewModelFactory {
        return CreateProfileViewModelFactory(
            session = session
        )
    }

}