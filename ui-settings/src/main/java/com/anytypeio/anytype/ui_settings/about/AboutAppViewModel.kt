package com.anytypeio.anytype.ui_settings.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.auth.interactor.GetLibraryVersion
import com.anytypeio.anytype.domain.base.BaseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AboutAppViewModel(
    private val getAccount: GetAccount,
    private val getLibraryVersion: GetLibraryVersion
) : ViewModel() {

    val libraryVersion = MutableStateFlow("")
    val userId = MutableStateFlow("")

    init {
        viewModelScope.launch {
            getAccount(BaseUseCase.None).process(
                failure = {},
                success = { acc ->
                    userId.value = acc.id
                }
            )
        }
        viewModelScope.launch {
            getLibraryVersion(BaseUseCase.None).process(
                failure = {},
                success = { version ->
                    libraryVersion.value = version
                }
            )
        }
    }

    class Factory(
        private val getAccount: GetAccount,
        private val getLibraryVersion: GetLibraryVersion
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AboutAppViewModel(
                getAccount = getAccount,
                getLibraryVersion = getLibraryVersion
            ) as T
        }
    }
}