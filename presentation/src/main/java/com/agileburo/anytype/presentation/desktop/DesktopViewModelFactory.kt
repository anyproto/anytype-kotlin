package com.agileburo.anytype.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.desktop.interactor.GetAccount
import com.agileburo.anytype.domain.image.LoadImage

class DesktopViewModelFactory(
    private val getAccount: GetAccount,
    private val loadImage: LoadImage
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DesktopViewModel(
            getAccount = getAccount,
            loadImage = loadImage
        ) as T
    }
}