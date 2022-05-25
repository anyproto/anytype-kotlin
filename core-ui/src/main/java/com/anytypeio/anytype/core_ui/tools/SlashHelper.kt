package com.anytypeio.anytype.core_ui.tools

import com.anytypeio.anytype.core_ui.tools.SlashTextWatcher.Companion.NO_SLASH_POSITION
import com.anytypeio.anytype.core_ui.tools.SlashTextWatcher.Companion.SLASH_CHAR

object SlashHelper {

    fun getSlashPosition(text: CharSequence, start: Int, count: Int): Int {
        val position = start + count - 1
        return if (count > 0 && start < text.length && text.getOrNull(position) == SLASH_CHAR) {
            position
        } else {
            NO_SLASH_POSITION
        }
    }

    fun isSlashDeleted(start: Int, slashPosition: Int): Boolean =
        slashPosition != NO_SLASH_POSITION && start <= slashPosition
}