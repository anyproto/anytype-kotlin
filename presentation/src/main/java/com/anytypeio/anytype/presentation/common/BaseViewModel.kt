package com.anytypeio.anytype.presentation.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

open class BaseViewModel : ViewModel() {
    internal val _toasts = MutableSharedFlow<String>(replay = 0)
    val toasts: Flow<String> get() = _toasts
}