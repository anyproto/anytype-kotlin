package com.agileburo.anytype.core_ui.reactive

import android.view.View
import android.view.ViewTreeObserver
import com.agileburo.anytype.core_utils.ext.dp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


/**
 * Workaround for observing keyboard visibility changes.
 * Should not be considered as reliable implementation that works across all devices.
 */
fun View.keyboardVisibilityObserver(): Flow<Boolean> = callbackFlow {
    val listener = ViewTreeObserver.OnGlobalLayoutListener {
        val diff = rootView.height - height
        if (diff > context.dp(200f)) offer(true) else offer(false)
    }
    viewTreeObserver.addOnGlobalLayoutListener(listener)
    awaitClose {
        viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }
}