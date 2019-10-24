package com.agileburo.anytype.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.desktop.interactor.GetAccount

class DesktopViewModelFactory(
    private val getAccount: GetAccount
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DesktopViewModel(
            getAccount = getAccount
        ) as T
    }
}