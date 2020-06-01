package com.agileburo.anytype.core_ui.tools

interface ClipboardInterceptor {

    fun onClipboardAction(action: Action)

    sealed class Action {
        data class Copy(val selection: IntRange) : Action()
        data class Paste(val selection: IntRange) : Action()
    }
}