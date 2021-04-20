package com.anytypeio.anytype.core_ui.tools

import android.text.Editable
import android.text.TextWatcher
import timber.log.Timber


class SlashTextWatcher(
    private val onSlashEvent: (SlashTextWatcherState) -> Unit
) : TextWatcher {

    /**
     * Position of [SLASH_CHAR] character
     */
    private var slashCharPosition = NO_SLASH_POSITION
    private var filter: String = ""

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

    fun onDismiss() {
        slashCharPosition = NO_SLASH_POSITION
        filter = ""
    }

    /**
     * Check for new added char is [SLASH_CHAR] and send [SlashTextWatcherState.Start] event
     */
    private fun proceedWithStart(text: CharSequence, start: Int, count: Int) {
        if (SlashHelper.isSlashCharAdded(text = text, start = start, count = count)) {
            Timber.d("Send Start event")
            slashCharPosition = start
            filter = ""
            onSlashEvent(SlashTextWatcherState.Start(start = start))
        }
    }

    private fun proceedWithFilter(text: CharSequence, start: Int, count: Int) {
        if (isSlashCharVisible()) {
            if (isStartPositionBeforeSlash(start = start, slashPos = slashCharPosition)) {
                stopSlashWatcher()
            } else {
                filter += text.subSequence(start, start + count)
                Timber.d("Send Filter event:$filter")
                onSlashEvent(SlashTextWatcherState.Filter(filter))
            }
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
            Timber.d("Send Stop event")
            stopSlashWatcher()
        }
    }

    private fun stopSlashWatcher() {
        onDismiss()
        onSlashEvent(SlashTextWatcherState.Stop)
    }

    private fun isSlashCharVisible(): Boolean = slashCharPosition != NO_SLASH_POSITION

    private fun isStartPositionBeforeSlash(start: Int, slashPos: Int): Boolean =
        start - slashPos < 0

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