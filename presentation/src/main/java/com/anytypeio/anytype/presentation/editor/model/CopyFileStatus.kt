package com.anytypeio.anytype.presentation.editor.model

sealed class CopyFileStatus {
    data class Error(val msg: String) : CopyFileStatus()
    object Started : CopyFileStatus()
    data class Completed(val result: String?) : CopyFileStatus()
}