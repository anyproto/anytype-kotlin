package com.agileburo.anytype.core_ui.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.agileburo.anytype.core_ui.R

fun Context.toast(
    msg: CharSequence,
    gravity: Int? = null,
    duration: Int = Toast.LENGTH_LONG
) = Toast
    .makeText(this, msg, duration)
    .apply {
        if (gravity != null) setGravity(
            gravity,
            0,
            resources.getDimension(R.dimen.default_toast_top_gravity_offset).toInt()
        )
    }
    .show()

fun View.tint(color: Int) {
    backgroundTintList = ColorStateList.valueOf(color)
}

fun LinearLayout.addVerticalDivider(
    alpha: Float,
    height: Int,
    color: Int
) = addView(
    View(context).apply {
        setBackgroundColor(color)
        setAlpha(alpha)
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            height
        )
    }
)