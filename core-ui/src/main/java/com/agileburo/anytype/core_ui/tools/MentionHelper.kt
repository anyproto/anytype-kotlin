package com.agileburo.anytype.core_ui.tools

import com.agileburo.anytype.core_ui.tools.MentionTextWatcher.Companion.MENTION_CHAR
import com.agileburo.anytype.core_ui.tools.MentionTextWatcher.Companion.NO_MENTION_POSITION


object MentionHelper {

    fun isMentionSuggestTriggered(
        s: CharSequence,
        start: Int,
        count: Int,
        mentionChar: Char = MENTION_CHAR
    ): Boolean {
        if (count == 1 && start < s.length && s[start] == mentionChar) {
            if (start == 0) {
                return true
            }
            val before = start - 1
            if (before in 0..s.length && s[before] == ' ') {
                return true
            }
        }
        return false
    }

    fun isMentionDeleted(start: Int, count: Int, after: Int, mentionPosition: Int): Boolean =
        mentionPosition != NO_MENTION_POSITION && start <= mentionPosition && after < count

    /**
     * return subsequence from [startIndex] to end of sequence with limit [takeNumber]
     */
    fun getSubSequenceFromStartWithLimit(
        s: CharSequence,
        startIndex: Int,
        takeNumber: Int
    ): CharSequence = s.subSequence(startIndex = startIndex, endIndex = s.length).take(takeNumber)
}