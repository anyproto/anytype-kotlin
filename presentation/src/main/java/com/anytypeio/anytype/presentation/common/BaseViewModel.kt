package com.anytypeio.anytype.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    internal val _toasts = MutableSharedFlow<String>(replay = 0)
    val toasts: Flow<String> get() = _toasts
    fun sendToast(msg: String) = viewModelScope.launch { _toasts.emit(msg) }
}