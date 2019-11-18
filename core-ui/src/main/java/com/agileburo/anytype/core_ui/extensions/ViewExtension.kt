package com.agileburo.anytype.core_ui.extensions

import android.content.Context
import android.view.View
import android.widget.Toast
import com.agileburo.anytype.core_ui.R

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

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