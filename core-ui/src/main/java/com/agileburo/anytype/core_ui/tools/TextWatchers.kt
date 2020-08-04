package com.agileburo.anytype.core_ui.tools

import android.text.Editable
import android.text.TextWatcher
import com.agileburo.anytype.core_ui.features.page.MentionEvent
import com.agileburo.anytype.core_ui.tools.MentionHelper.getSubSequenceFromStartWithLimit
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
    private val onMentionEvent: (MentionTextWatcherState) -> Unit
) : TextWatcher {

    private var mentionCharPosition = NO_MENTION_POSITION

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        interceptMentionDeleted(start = start, count = count, after = after)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        interceptMentionTriggered(text = s, start = start, count = count)
    }

    fun onDismiss() {
        Timber.d("Dismiss Mention Watcher $this")
        mentionCharPosition = NO_MENTION_POSITION
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
            Timber.d("interceptMentionDeleted $this")
            mentionCharPosition = NO_MENTION_POSITION
            onMentionEvent(MentionTextWatcherState.Stop)
        }
    }

    /**
     * Check for new added char @ and start [MentionEvent.MentionSuggestStart] event
     * Send all text in range [mentionCharPosition]..[text end] with limit [MENTION_SNIPPET_MAX_LENGTH]
     */
    private fun interceptMentionTriggered(text: CharSequence, start: Int, count: Int) {
        if (isMentionSuggestTriggered(text, start, count)) {
            Timber.d("interceptMentionStarted text:$text, start:$start")
            mentionCharPosition = start
            onMentionEvent(MentionTextWatcherState.Start(mentionCharPosition))
        }
        if (mentionCharPosition != NO_MENTION_POSITION) {
            getSubSequenceFromStartWithLimit(
                s = text,
                startIndex = mentionCharPosition,
                takeNumber = MENTION_SNIPPET_MAX_LENGTH
            ).let { subSequence ->
                onMentionEvent(MentionTextWatcherState.Text(subSequence))
                Timber.d("interceptMentionText text:$text, subSequence:$subSequence")
            }
        }
    }

    sealed class MentionTextWatcherState {
        data class Start(val start: Int) : MentionTextWatcherState()
        object Stop : MentionTextWatcherState()
        data class Text(val text: CharSequence) : MentionTextWatcherState()
    }

    companion object {
        const val SPACE_CHAR = ' '
        const val MENTION_CHAR = '@'
        const val MENTION_SNIPPET_MAX_LENGTH = 50
        const val NO_MENTION_POSITION = -1
    }
}