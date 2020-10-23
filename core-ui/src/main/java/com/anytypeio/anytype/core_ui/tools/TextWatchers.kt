package com.anytypeio.anytype.core_ui.tools

import android.text.Editable
import android.text.TextWatcher
import com.anytypeio.anytype.core_ui.features.page.MentionEvent
import com.anytypeio.anytype.core_ui.tools.MentionHelper.getUpdatedMention
import com.anytypeio.anytype.core_ui.tools.MentionHelper.isMentionDeleted
import com.anytypeio.anytype.core_ui.tools.MentionHelper.isMentionSuggestTriggered
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
        interceptMentionDeleted(start = start)
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
    private fun interceptMentionDeleted(start: Int) {
        if (isMentionDeleted(
                start = start,
                mentionPosition = mentionCharPosition
            )
        ) {
            Timber.d("interceptMentionDeleted $this")
            stopMentionWatcher()
        }
    }

    /**
     * Check for new added char @ and start [MentionEvent.MentionSuggestStart] event
     * Send all text added after mention start position [mentionCharPosition]
     */
    private fun interceptMentionTriggered(text: CharSequence, start: Int, before: Int, count: Int) {
        if (isMentionSuggestTriggered(text, start, count)) {
            Timber.d("interceptMentionStarted text:$text, start:$start")
            mentionCharPosition = start
            mention = ""
            onMentionEvent(MentionTextWatcherState.Start(mentionCharPosition))
        }

        if (isMentionCharVisible()) {
            if (isStartPositionBeforeMention(start = start, mentionPos = mentionCharPosition)) {
                stopMentionWatcher()
            } else {
                mention = getUpdatedMention(
                    mention = mention,
                    replacement = text.subSequence(startIndex = start, endIndex = start + count),
                    replacementStart = start - mentionCharPosition,
                    before = before
                )
                Timber.d("Send mention text:$mention")
                onMentionEvent(MentionTextWatcherState.Text(mention))
            }
        }
    }

    private fun isMentionCharVisible(): Boolean = mentionCharPosition != NO_MENTION_POSITION

    private fun stopMentionWatcher() {
        onDismiss()
        onMentionEvent(MentionTextWatcherState.Stop)
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