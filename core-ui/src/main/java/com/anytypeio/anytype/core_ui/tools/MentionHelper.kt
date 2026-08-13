package com.anytypeio.anytype.core_ui.tools

import com.anytypeio.anytype.core_models.ext.replaceRangeWithWord
import com.anytypeio.anytype.core_ui.tools.MentionTextWatcher.Companion.MENTION_CHAR
import com.anytypeio.anytype.core_ui.tools.MentionTextWatcher.Companion.NO_MENTION_POSITION
import timber.log.Timber


object MentionHelper {

    /**
     * Position of the mention char that should open the suggester,
     * or [NO_MENTION_POSITION] when this text change is not a mention trigger.
     *
     * Only the last char of the freshly inserted run is inspected: soft keyboards
     * re-commit the whole composing word on every keystroke, so the mention char
     * regularly arrives at the tail of a multi-char replacement instead of on its own.
     * Matching a single-char insert only ([count] == 1) made the suggester miss those.
     *
     * The suggester opens at the start of a line or after whitespace only, so that
     * "word@" — an e-mail address, a handle — stays plain text.
     */
    fun getMentionSuggestPosition(
        s: CharSequence,
        start: Int,
        count: Int,
        mentionChar: Char = MENTION_CHAR
    ): Int {
        if (count <= 0) return NO_MENTION_POSITION
        val position = start + count - 1
        if (position !in s.indices || s[position] != mentionChar) {
            return NO_MENTION_POSITION
        }
        val before = position - 1
        return if (before < 0 || s[before].isWhitespace()) position else NO_MENTION_POSITION
    }

    fun isMentionDeleted(text: CharSequence, start: Int, mentionPosition: Int, before: Int, count: Int): Boolean {
        if (mentionPosition == NO_MENTION_POSITION) {
            return false
        }
        if (start == mentionPosition) {
            return !(before == 1 && count == 1 && text.getOrNull(start) == MENTION_CHAR)
        }
        return start < mentionPosition
    }


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
}

/**
 * Within [text], the [count] characters beginning at [start]
 * have just replaced old text that had length [before].
 * So we need to properly update mention [this] with start position [mentionStart]
 */
fun String.updateMentionWhenTextChanged(
    text: CharSequence,
    start: Int,
    before: Int,
    count: Int,
    mentionStart: Int
): String {
    if (text.isTextUpdateInRange(start, count)) {

        val replace = text.substring(startIndex = start, endIndex = start + count)

        if (count < before) {
            val startIndex = start - mentionStart
            val endIndex = startIndex + before
            return if (
                startIndex in 0..length &&
                endIndex in 0..length &&
                endIndex >= startIndex
            ) {
                replaceRange(
                    startIndex = startIndex,
                    endIndex = endIndex,
                    replacement = replace
                )
            } else {
                this
            }
        } else {
            val from = start - mentionStart
            val to = length
            return if (
                from in 0..length &&
                to in 0..length
            ) {
                replaceRangeWithWord(
                    from = from,
                    to = to,
                    replace = replace
                )
            } else {
                this
            }
        }
    } else {
        Timber.e("Text update is not in range, text:$text, start:$start, count:$count")
        return ""
    }
}

private fun CharSequence.isTextUpdateInRange(start: Int, count: Int): Boolean =
    start in 0..length && (start + count) in 0..length