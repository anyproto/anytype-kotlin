package com.anytypeio.anytype.core_ui.extensions

import androidx.emoji2.text.EmojiCompat

object EmojiUtils {

    fun isReady(): Boolean = runCatching {
        EmojiCompat.get().loadState == EmojiCompat.LOAD_STATE_SUCCEEDED
    }.getOrDefault(false)

    /** Safe process: returns input unchanged if EmojiCompat isn’t ready yet */
    fun processSafe(text: CharSequence): CharSequence {
        return if (isReady()) {
            try {
                EmojiCompat.get().process(text) ?: text
            } catch (_: Throwable) {
                text
            }
        } else text
    }
}