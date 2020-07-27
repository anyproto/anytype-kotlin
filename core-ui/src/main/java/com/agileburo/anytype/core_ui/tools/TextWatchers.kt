package com.agileburo.anytype.core_ui.tools

import android.text.Editable
import android.text.TextWatcher
import com.agileburo.anytype.core_ui.features.page.MentionEvent
import com.agileburo.anytype.core_ui.tools.MentionHelper.getSubSequenceBeforePredicate
import com.agileburo.anytype.core_ui.tools.MentionHelper.isMentionDeleted
import com.agileburo.anytype.core_ui.tools.MentionHelper.isMentionSuggestTriggered
import timber.log.Timber

class DefaultTextWatcher(val onTextChanged: (Editable) -> Unit) : TextWatcher {

    private var locked: Boolean = false

    override fun afterTextChanged(s: Editable) {
        Timber.d("OnTextChanged: $s")
        if (!locked)
            onTextChanged(s)
        else
            Timber.d("Locked text watcher. Skipping text update...")
    }
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    fun lock() {
        locked = true
    }

    fun unlock() {
        locked = false
    }
}

class MentionTextWatcher(
    private val onMentionEvent: (MentionEvent) -> Unit
) : TextWatcher {

    private var mentionCharPosition = NO_MENTION_POSITION

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        interceptMentionDeleted(start = start, count = count, after = after)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        interceptMentionTriggered(text = s, start = start, count = count)
    }

    /**
     * If char @ on position [mentionCharPosition] was deleted then send MentionStop event
     */
    private fun interceptMentionDeleted(start: Int, count: Int, after: Int) {
        if (isMentionDeleted(
                start = start,
                count = count,
                after = after,
                mentionPosition = mentionCharPosition
            )
        ) {
            mentionCharPosition = NO_MENTION_POSITION
            onMentionEvent(MentionEvent.MentionSuggestStop)
        }
    }

    /**
     * Check for new added char @ and start [MentionEvent.MentionSuggestStart] event
     * Send all text in range [mentionCharPosition]..[first space or text end] with limit [MENTION_SNIPPET_MAX_LENGTH]
     */
    private fun interceptMentionTriggered(text: CharSequence, start: Int, count: Int) {
        if (isMentionSuggestTriggered(text, start, count)) {
            mentionCharPosition = start
            onMentionEvent(MentionEvent.MentionSuggestStart)
        }
        if (mentionCharPosition != NO_MENTION_POSITION) {
            onMentionEvent(
                MentionEvent.MentionSuggestText(
                    getSubSequenceBeforePredicate(
                        s = text,
                        startIndex = mentionCharPosition,
                        takeNumber = MENTION_SNIPPET_MAX_LENGTH,
                        predicate = SPACE_CHAR
                    )
                )
            )
        }
    }

    companion object {
        const val SPACE_CHAR = ' '
        const val MENTION_CHAR = '@'
        const val MENTION_SNIPPET_MAX_LENGTH = 50
        const val NO_MENTION_POSITION = -1
    }
}