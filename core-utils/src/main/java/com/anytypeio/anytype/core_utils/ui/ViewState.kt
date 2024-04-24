package com.anytypeio.anytype.core_utils.ui

sealed class ViewState<out T : Any> {
    data object Init : ViewState<Nothing>()
    data object Loading : ViewState<Nothing>()
    data class Success<out T : Any>(val data: T) : ViewState<T>()
    data class Error(val error: String) : ViewState<Nothing>()
}