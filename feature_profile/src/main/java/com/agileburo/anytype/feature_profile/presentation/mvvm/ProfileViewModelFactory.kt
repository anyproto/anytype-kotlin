package com.agileburo.anytype.feature_profile.presentation.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.feature_profile.domain.UpdateProfileSettings

class ProfileViewModelFactory(
    private val updateProfileSettings: UpdateProfileSettings
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ProfileViewModel(
            updateProfileSettings = updateProfileSettings
        ) as T
    }
}