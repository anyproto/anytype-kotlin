package com.agileburo.anytype.presentation.page.editor

sealed class ErrorViewState {
    data class Toast(val msg: String) : ErrorViewState()
    object AlertDialog : ErrorViewState()
}