package com.anytypeio.anytype.presentation.editor.model

sealed class EditorFooter {
    object Note : EditorFooter()
    object None : EditorFooter()
}