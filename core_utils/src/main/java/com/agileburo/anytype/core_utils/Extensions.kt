package com.agileburo.anytype.core_utils

import android.content.Context
import android.widget.Toast

fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val tmp = this[index1] // 'this' corresponds to the list
    this[index1] = this[index2]
    this[index2] = tmp
}

fun Context.toast(msg: CharSequence) =
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()