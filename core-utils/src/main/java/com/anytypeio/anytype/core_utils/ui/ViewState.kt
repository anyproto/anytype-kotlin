package com.anytypeio.anytype.core_utils.ui

sealed class ViewState<out T : Any> {
    object Init : ViewState<Nothing>()
    object Loading : ViewState<Nothing>()
    data class Success<out T : Any>(val data: T) : ViewState<T>()
    data class Error(val error: String) : ViewState<Nothing>()
}