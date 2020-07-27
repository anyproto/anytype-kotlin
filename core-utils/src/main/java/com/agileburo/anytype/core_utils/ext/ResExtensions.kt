package com.agileburo.anytype.core_utils.ext

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment

fun Fragment.drawable(@DrawableRes res: Int): Drawable {
    return requireContext().getDrawable(res)
        ?: throw IllegalStateException("Could not get drawable for res: $res")
}