package com.agileburo.anytype.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.auth.interactor.Logout
import com.agileburo.anytype.domain.image.LoadImage

class ProfileViewModelFactory(
    private val logout: Logout,
    private val getCurrentAccount: GetCurrentAccount,
    private val loadImage: LoadImage
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ProfileViewModel(
            logout = logout,
            getCurrentAccount = getCurrentAccount,
            loadImage = loadImage
        ) as T
    }
}