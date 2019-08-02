package com.agileburo.anytype.core_utils

import android.content.Context
import android.widget.Toast

fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val tmp = this[index1] // 'this' corresponds to the list
    this[index1] = this[index2]
    this[index2] = tmp
}

inline fun <reified T> MutableList<T>.shift(srcIndex: Int, dstIndex: Int) =
    if (srcIndex < dstIndex) shiftUp(srcIndex, dstIndex) else shiftDown(srcIndex, dstIndex)


inline fun <reified T> MutableList<T>.shiftUp(srcIndex: Int, dstIndex: Int) =
    MutableList(size) { i ->
        when {
            i < srcIndex -> this[i]
            i < dstIndex -> this[i + 1]
            i == dstIndex -> this[srcIndex]
            else -> this[i]
        }
    }

inline fun <reified T> MutableList<T>.shiftDown(srcIndex: Int, dstIndex: Int) =
    MutableList(size) { i ->
        when {
            i < dstIndex -> this[i]
            i == dstIndex -> this[srcIndex]
            i <= srcIndex -> this[i - 1]
            else -> this[i]
        }
    }

fun Context.toast(msg: CharSequence) =
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()