package com.anytypeio.anytype.core_utils.insets

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.*

fun View.doOnApplyWindowInsets(
    windowInsetsListener: (
        insetView: View,
        windowInsets: WindowInsetsCompat,
        initialPadding: Insets,
        initialMargins: Insets
    ) -> Unit
) {
    val initialPadding = Insets.of(paddingLeft, paddingTop, paddingRight, paddingBottom)
    val initialMargins = Insets.of(marginLeft, marginTop, marginRight, marginBottom)

    ViewCompat.setOnApplyWindowInsetsListener(this) { insetView, windowInsets ->
        windowInsets.also {
            windowInsetsListener(insetView, windowInsets, initialPadding, initialMargins)
        }
    }

    // Whenever a view is detached and then re-attached to the screen, we need to apply insets again.
    //
    // In particular, it is not enough to apply insets only on the first attach:
    //
    // doOnAttach { requestApplyInsets() }
    //
    // For example, considering the following scenario:
    // - A RecyclerView lays out items while in landscape.
    // - Some items that depend on the insets are laid out, and are then detached because they go off-screen.
    // - The user rotates the device 180 degrees. This is still landscape, so no configuration change occurs, but an
    // inset change _does_ occur.
    // - The detached items are reattached because they come back on-screen.
    //
    // At this point, the insets applied to the view would be out of date, and they wouldn't be updated, since the view
    // was already attached once, and the callback for the new insets caused by the rotation would have already been
    // applied, and skipped updating the detached view.
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            v.requestApplyInsets()
        }

        override fun onViewDetachedFromWindow(v: View) = Unit
    })

    // If the view is already attached, immediately request insets be applied.
    if (isAttachedToWindow) {
        requestApplyInsets()
    }
}