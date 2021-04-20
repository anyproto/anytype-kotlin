package com.anytypeio.anytype.presentation.page.editor.slash

sealed class SlashEvent {
    data class Filter(val filter: CharSequence) : SlashEvent()
    data class Start(val cursorCoordinate: Int, val slashStart: Int) : SlashEvent()
    object Stop : SlashEvent()
}
