package com.agileburo.anytype.core_ui.menu

import android.widget.TextView

sealed class AnytypeContextMenuEvent {
    object Detached : AnytypeContextMenuEvent()
    data class Selected(val view: TextView, val type: AnytypeContextMenuType) :
        AnytypeContextMenuEvent()
}

enum class AnytypeContextMenuType { DEFAULT, HEADER, HIGHLIGHT }