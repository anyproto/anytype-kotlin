package com.agileburo.anytype.presentation.auth.congratulation

sealed class ViewState<out T : Any> {
    data class Success<out T : Any>(val data: T) : ViewState<T>()
    data class Error(val error: String) : ViewState<Nothing>()
    object Loading : ViewState<Nothing>()
}