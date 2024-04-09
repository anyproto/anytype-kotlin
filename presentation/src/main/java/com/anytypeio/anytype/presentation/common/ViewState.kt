package com.anytypeio.anytype.presentation.common

sealed class ViewState<out T : Any> {
    data class Success<out T : Any>(val data: T) : ViewState<T>()
    data class Error(val error: String) : ViewState<Nothing>()
    data object Loading : ViewState<Nothing>()
}

sealed class TypedViewState<out T : Any, out E: Any> {
    data class Success<out T : Any, out E: Any>(val data: T) : TypedViewState<T, E>()
    data class Error<out T : Any, out E: Any>(val error: E) : TypedViewState<T, E>()
    data object Loading : TypedViewState<Nothing, Nothing>()
}