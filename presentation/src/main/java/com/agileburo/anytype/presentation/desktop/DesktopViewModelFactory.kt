package com.agileburo.anytype.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.desktop.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.image.LoadImage

class DesktopViewModelFactory(
    private val getCurrentAccount: GetCurrentAccount,
    private val loadImage: LoadImage
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DesktopViewModel(
            getCurrentAccount = getCurrentAccount,
            loadImage = loadImage
        ) as T
    }
}