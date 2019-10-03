package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChooseProfileViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChooseProfileViewModel() as T
    }
}