package com.anytypeio.anytype.core_ui.tools

import com.anytypeio.anytype.core_ui.tools.SlashTextWatcher.Companion.NO_SLASH_POSITION
import com.anytypeio.anytype.core_ui.tools.SlashTextWatcher.Companion.SLASH_CHAR

object SlashHelper {

    fun isSlashCharAdded(text: CharSequence, start: Int, count: Int): Boolean =
        count == 1 && start < text.length && text[start] == SLASH_CHAR

    fun isSlashDeleted(start: Int, slashPosition: Int): Boolean =
        slashPosition != NO_SLASH_POSITION && start <= slashPosition

    /**
     * Replace char sequence range in [filter] with start index [replacementStart]
     * by new [replacement] char sequence
     * @property before See TextWatcher.onTextChanged property before
     */
    fun getUpdatedFilter(
        replacement: CharSequence,
        replacementStart: Int,
        before: Int,
        filter: String
    ): String = try {
        val range = replacementStart until replacementStart + before
        if (range.first > filter.length) {
            filter
        } else {
            filter.replaceRange(range = range, replacement = replacement)
        }
    } catch (e: Exception) {
        filter
    }
}