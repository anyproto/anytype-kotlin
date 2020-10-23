package com.anytypeio.anytype.core_ui.tools

import com.anytypeio.anytype.core_ui.tools.MentionTextWatcher.Companion.MENTION_CHAR
import com.anytypeio.anytype.core_ui.tools.MentionTextWatcher.Companion.NO_MENTION_POSITION


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

    fun isMentionDeleted(start: Int, mentionPosition: Int): Boolean =
        mentionPosition != NO_MENTION_POSITION && start <= mentionPosition

    /**
     * return subsequence from [startIndex] to end of sequence with limit [takeNumber]
     */
    fun getSubSequenceFromStartWithLimit(
        s: CharSequence,
        predicate: Char,
        startIndex: Int,
        takeNumber: Int
    ): CharSequence = s.indexOf(predicate, startIndex = startIndex).let { pos ->
        if (pos != -1)
            s.subSequence(startIndex = startIndex, endIndex = pos).take(takeNumber)
        else
            s.subSequence(startIndex = startIndex, endIndex = s.length).take(takeNumber)
    }

    /**
     * Replace char sequence range in [mention] with start index [replacementStart]
     * by new [replacement] char sequence
     * @property before See TextWatcher.onTextChanged property before
     */
    fun getUpdatedMention(
        replacement: CharSequence,
        replacementStart: Int,
        before: Int,
        mention: String
    ): String = try {
        val range = replacementStart until replacementStart + before
        if (range.first > mention.length) {
            mention
        } else {
            mention.replaceRange(range = range, replacement = replacement)
        }
    } catch (e: Exception) {
        mention
    }
}