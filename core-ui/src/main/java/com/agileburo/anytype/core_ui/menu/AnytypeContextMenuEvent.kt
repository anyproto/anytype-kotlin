package com.agileburo.anytype.core_ui.menu

import android.widget.TextView

sealed class AnytypeContextMenuEvent {
    object Detached : AnytypeContextMenuEvent()
    object MarkupChanged: AnytypeContextMenuEvent()
    data class Create(val view: TextView, val type: ContextMenuType) :
        AnytypeContextMenuEvent()

    data class Selected(val view: TextView, val type: ContextMenuType) :
        AnytypeContextMenuEvent()
}