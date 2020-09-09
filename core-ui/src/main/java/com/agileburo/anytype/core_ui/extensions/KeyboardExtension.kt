package com.agileburo.anytype.core_ui.extensions

import android.view.ViewGroup
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.suspendCancellableCoroutine

@Deprecated("Not yet stable.")
suspend fun ViewGroup.awaitKeyboadHidden(
    doAfterSetup: () -> Unit
) = suspendCancellableCoroutine<Unit> { process ->

    val listener = OnApplyWindowInsetsListener { view, insets ->
        val height = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        if (height == 0) {
            if (process.isActive) {
                process.resume(Unit) {
                    ViewCompat.setOnApplyWindowInsetsListener(view, null)
                }
            }
        }
        insets
    }

    process.invokeOnCancellation {
        ViewCompat.setOnApplyWindowInsetsListener(this, null)
    }

    ViewCompat.setOnApplyWindowInsetsListener(this, listener)

    doAfterSetup()
}

fun ViewGroup.isKeyboardVisible(): Boolean {
    val insets = ViewCompat.getRootWindowInsets(this)
    checkNotNull(insets)
    return insets.isVisible(WindowInsetsCompat.Type.ime())
}