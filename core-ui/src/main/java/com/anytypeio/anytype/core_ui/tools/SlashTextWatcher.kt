package com.anytypeio.anytype.core_ui.tools

import android.text.Editable
import android.text.TextWatcher
import timber.log.Timber


class SlashTextWatcher(
    private val onSlashEvent: (SlashTextWatcherState) -> Unit
) : TextWatcher {

    private var locked: Boolean = false

    fun lock() {
        locked = true
    }

    fun unlock() {
        locked = false
    }

    /**
     * Position of [SLASH_CHAR] character
     */
    private var slashCharPosition = NO_SLASH_POSITION
    private var filter: CharSequence = ""

    override fun afterTextChanged(s: Editable?) {}
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        proceedWithStop(start)
        proceedWithStart(
            text = s,
            start = start,
            count = count
        )
        proceedWithFilter(
            text = s,
            start = start,
            count = count
        )
    }

    private fun onDismiss() {
        slashCharPosition = NO_SLASH_POSITION
        filter = ""
    }

    /**
     * Check for new added char is [SLASH_CHAR] and send [SlashTextWatcherState.Start] event
     */
    private fun proceedWithStart(text: CharSequence, start: Int, count: Int) {
        val slashPosition = SlashHelper.getSlashPosition(text = text, start = start, count = count)
        if (slashPosition != NO_SLASH_POSITION) {
            slashCharPosition = slashPosition
            filter = ""
            proceedWithState(SlashTextWatcherState.Start(start = slashCharPosition))
        }
    }

    private fun proceedWithFilter(text: CharSequence, start: Int, count: Int) {
        if (isSlashCharVisible() && slashCharPosition < text.length && start + count <= text.length) {
            filter = text.subSequence(startIndex = slashCharPosition, endIndex = start + count)
            proceedWithState(SlashTextWatcherState.Filter(filter))
        }
    }

    /**
     * If text changed with start [start] position smaller
     * then [SLASH_CHAR] position [slashCharPosition]
     * and [slashCharPosition] is not [NO_SLASH_POSITION]
     * then send Stop event
     */
    private fun proceedWithStop(start: Int) {
        if (SlashHelper.isSlashDeleted(
                start = start,
                slashPosition = slashCharPosition
            )
        ) {
            stopSlashWatcher()
        }
    }

    private fun stopSlashWatcher() {
        onDismiss()
        proceedWithState(SlashTextWatcherState.Stop)
    }

    private fun proceedWithState(state: SlashTextWatcherState) {
        if (!locked) {
            Timber.d("proceedWithState, state:[$state]")
            onSlashEvent(state)
        } else {
            Timber.d("proceedWithState, Locked slash text watcher. Skipping event[$state]...")
        }
    }

    private fun isSlashCharVisible(): Boolean = slashCharPosition != NO_SLASH_POSITION

    companion object {
        const val SLASH_CHAR = '/'
        const val NO_SLASH_POSITION = -1
    }
}

sealed class SlashTextWatcherState {
    data class Start(val start: Int) : SlashTextWatcherState()
    data class Filter(val text: CharSequence) : SlashTextWatcherState()
    object Stop : SlashTextWatcherState()
}