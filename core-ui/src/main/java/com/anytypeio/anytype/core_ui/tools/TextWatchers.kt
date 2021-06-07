package com.anytypeio.anytype.core_ui.tools

import android.text.Editable
import android.text.TextWatcher
import com.anytypeio.anytype.core_ui.tools.MentionHelper.isMentionDeleted
import com.anytypeio.anytype.core_ui.tools.MentionHelper.isMentionSuggestTriggered
import com.anytypeio.anytype.presentation.page.editor.mention.MentionEvent
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

    /**
     * Position of "@" character
     */
    private var mentionCharPosition = NO_MENTION_POSITION
    private var mention: String = ""

    override fun afterTextChanged(s: Editable?) {}
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        interceptMentionDeleted(text = s, start = start, before = before, count = count)
        interceptMentionTriggered(text = s, start = start, count = count, before = before)
    }

    fun onDismiss() {
        Timber.d("Dismiss Mention Watcher $this")
        mentionCharPosition = NO_MENTION_POSITION
        mention = ""
    }

    /**
     * If text changed with start [start] position smaller
     * then char @ position [mentionCharPosition] then send MentionStop event
     */
    private fun interceptMentionDeleted(text: CharSequence, start: Int, before: Int, count: Int) {
        if (isMentionDeleted(
                text = text,
                start = start,
                mentionPosition = mentionCharPosition,
                before = before,
                count = count
            )
        ) {
            stopMentionWatcher()
        }
    }

    /**
     * Check for new added char @ and start [MentionEvent.MentionSuggestStart] event
     * Send all text added after mention start position [mentionCharPosition]
     */
    private fun interceptMentionTriggered(text: CharSequence, start: Int, before: Int, count: Int) {
        if (isMentionSuggestTriggered(text, start, count)) {
            mentionCharPosition = start
            mention = ""
            proceedWithMentionEvent(MentionTextWatcherState.Start(mentionCharPosition))
        }

        if (isMentionCharVisible()) {
            if (isStartPositionBeforeMention(start = start, mentionPos = mentionCharPosition)) {
                stopMentionWatcher()
            } else {
                mention = mention.updateMentionWhenTextChanged(
                    text = text,
                    start = start,
                    before = before,
                    count = count,
                    mentionStart = mentionCharPosition
                )
                proceedWithMentionEvent(MentionTextWatcherState.Text(mention))
            }
        }
    }

    private fun isMentionCharVisible(): Boolean = mentionCharPosition != NO_MENTION_POSITION

    private fun stopMentionWatcher() {
        onDismiss()
        proceedWithMentionEvent(MentionTextWatcherState.Stop)
    }

    private fun proceedWithMentionEvent(event: MentionTextWatcherState) {
        Timber.d("proceedWithMentionEvent, event:[$event]")
        onMentionEvent(event)
    }

    private fun isStartPositionBeforeMention(start: Int, mentionPos: Int): Boolean =
        start - mentionPos < 0

    sealed class MentionTextWatcherState {
        data class Start(val start: Int) : MentionTextWatcherState()
        object Stop : MentionTextWatcherState()
        data class Text(val text: CharSequence) : MentionTextWatcherState()
    }

    companion object {
        const val MENTION_CHAR = '@'
        const val NO_MENTION_POSITION = -1
    }
}