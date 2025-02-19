package com.anytypeio.anytype.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    internal val _toasts = MutableSharedFlow<String>(replay = 0)
    val toasts: Flow<String> get() = _toasts
    fun sendToast(msg: String) = viewModelScope.launch { _toasts.emit(msg) }

    companion object {
        const val DEFAULT_STOP_TIMEOUT_LIMIT = 5000L
    }

    data class DefaultParams(
        val space: SpaceId,
        val ctx: Id
    )
}