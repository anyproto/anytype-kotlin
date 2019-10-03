package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.congratulation

sealed class ViewState<out T : Any> {
    data class Success<out T : Any>(val data: T) : ViewState<T>()
    data class Error(val error: String) : ViewState<Nothing>()
    object Loading : ViewState<Nothing>()
}