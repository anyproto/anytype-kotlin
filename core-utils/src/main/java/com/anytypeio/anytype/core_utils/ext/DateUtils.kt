package com.anytypeio.anytype.core_utils.ext

import java.text.SimpleDateFormat
import java.util.*

fun Calendar.timeInSeconds() = this.timeInMillis / 1000

fun Long.toTimeSeconds(): Double = (this / 1000).toDouble()

fun Long.formatTimeInMillis(pattern: String): String {
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return simpleDateFormat.format(this)
}


