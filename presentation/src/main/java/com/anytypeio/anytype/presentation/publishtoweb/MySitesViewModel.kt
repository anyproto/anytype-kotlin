package com.anytypeio.anytype.presentation.publishtoweb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class MySitesViewModel(
    private val vmParams: VmParams
) : BaseViewModel() {

    private val _viewState = MutableStateFlow<MySitesViewState>(MySitesViewState.Init)
    val viewState = _viewState.asStateFlow()

    val commands = MutableSharedFlow<Command>()

    init {
        // TODO: Add initialization logic when needed
    }

    class Factory @Inject constructor(
        private val params: VmParams
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MySitesViewModel(
                vmParams = params
            ) as T
        }
    }

    data object VmParams

    sealed class Command {
        // TODO: Add commands when needed
    }
}

sealed class MySitesViewState {
    data object Init : MySitesViewState()
    // TODO: Add more states when needed
}