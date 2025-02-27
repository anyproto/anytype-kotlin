package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject

class DebugViewModel @Inject constructor(
    private val pathProvider: PathProvider,
    private val getAccount: GetAccount
) : BaseViewModel() {

    fun onExportWorkingDirectory() {
        // TODO
    }

    class Factory @Inject constructor(
        private val pathProvider: PathProvider,
        private val getAccount: GetAccount
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DebugViewModel(
                pathProvider = pathProvider,
                getAccount = getAccount
            ) as T
        }
    }
}