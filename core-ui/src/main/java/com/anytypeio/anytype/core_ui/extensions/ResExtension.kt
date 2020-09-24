package com.anytypeio.anytype.core_ui.extensions

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.anytypeio.anytype.core_ui.R

fun Context.drawable(
    @DrawableRes id: Int
) = ContextCompat.getDrawable(this, id) ?: throw IllegalArgumentException("Resource not found")

fun Context.color(
    @ColorRes id: Int
) = ContextCompat.getColor(this, id)

fun Context.avatarColor(
    position: Int
): Int {
    val colors = resources.obtainTypedArray(R.array.avatar_colors)
    val color = colors.getColor(position, 0)
    colors.recycle()
    return color
}