package com.anytypeio.anytype.core_ui.extensions

import android.graphics.drawable.Drawable
import android.widget.TextView

fun TextView.setDrawable(
    left: Drawable? = null,
    top: Drawable? = null,
    right: Drawable? = null,
    bottom: Drawable? = null
) = this.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)

fun TextView.clearDrawable() {
    this.setCompoundDrawables(null, null, null, null)
}