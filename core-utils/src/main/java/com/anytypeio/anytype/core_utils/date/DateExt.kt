package com.anytypeio.anytype.core_utils.date

import android.text.format.DateUtils

typealias Milliseconds = Long

fun Milliseconds.isToday() : Boolean {
    return DateUtils.isToday(this)
}

fun Milliseconds.isTomorrow() : Boolean {
    return DateUtils.isToday(
        this - DateUtils.DAY_IN_MILLIS
    )
}

fun Milliseconds.isYesterday() : Boolean {
    return DateUtils.isToday(
        this + DateUtils.DAY_IN_MILLIS
    )
}