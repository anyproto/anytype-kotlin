package com.agileburo.anytype.feature_profile.di

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.feature_profile.domain.UpdateProfileSettings
import com.agileburo.anytype.feature_profile.presentation.mvvm.ProfileViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class ProfileModule {

    @Provides
    @PerScreen
    fun provideProfileViewModelFactory(
        updateProfileSettings: UpdateProfileSettings
    ): ProfileViewModelFactory {
        return ProfileViewModelFactory(
            updateProfileSettings = updateProfileSettings
        )
    }

    @Provides
    @PerScreen
    fun updateProfileSettingsUseCase(): UpdateProfileSettings {
        return UpdateProfileSettings()
    }
}