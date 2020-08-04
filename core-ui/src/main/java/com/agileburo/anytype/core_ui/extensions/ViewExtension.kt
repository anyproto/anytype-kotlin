package com.agileburo.anytype.core_ui.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.PopupExtensions
import com.agileburo.anytype.core_utils.ext.PopupExtensions.calculateRectInWindow

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

fun EditText.cursorYBottomCoordinate(): Int {
    with(this.layout) {
        val pos = selectionStart
        val line = getLineForOffset(pos)
        val baseLine = getLineBaseline(line)
        val ascent = getLineAscent(line)
        val rect = calculateRectInWindow(this@cursorYBottomCoordinate)

        return baseLine + ascent + rect.bottom - scrollY
    }
}

fun TextView.range() : IntRange = selectionStart..selectionEnd