package com.agileburo.anytype.presentation.auth.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.auth.interactor.ObserveAccounts
import com.agileburo.anytype.domain.auth.interactor.StartLoadingAccounts
import com.agileburo.anytype.domain.image.LoadAccountImages

class SelectAccountViewModelFactory(
    private val startLoadingAccounts: StartLoadingAccounts,
    private val observeAccounts: ObserveAccounts,
    private val loadAccountImages: LoadAccountImages
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SelectAccountViewModel(
            startLoadingAccounts = startLoadingAccounts,
            observeAccounts = observeAccounts,
            loadAccountImages = loadAccountImages
        ) as T
    }
}