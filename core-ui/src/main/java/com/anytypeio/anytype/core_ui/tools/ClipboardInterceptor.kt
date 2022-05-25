package com.anytypeio.anytype.core_ui.tools

import com.anytypeio.anytype.core_models.Url

interface ClipboardInterceptor {

    fun onClipboardAction(action: Action)
    fun onUrlPasted(url: Url)

    sealed class Action {
        data class Copy(val selection: IntRange) : Action()
        data class Paste(val selection: IntRange) : Action()
    }
}