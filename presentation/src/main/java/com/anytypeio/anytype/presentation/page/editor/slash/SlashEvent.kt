package com.anytypeio.anytype.presentation.page.editor.slash

/**
 * Events coming from slash text watchers
 */
sealed class SlashEvent {

    /**
     * This event is triggered by entering text, starting from / char
     */
    data class Filter(val filter: CharSequence, val viewType: Int) : SlashEvent()

    /**
     * This event is triggered by / char
     */
    data class Start(val cursorCoordinate: Int, val slashStart: Int) : SlashEvent()

    /**
     * This event is triggered when char / is deleted, or text is changed
     * with start position that is before position of /
     */
    object Stop : SlashEvent()
}
