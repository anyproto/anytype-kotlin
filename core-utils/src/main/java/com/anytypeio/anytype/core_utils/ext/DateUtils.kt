package com.anytypeio.anytype.core_utils.ext

import java.text.SimpleDateFormat
import java.util.*

fun Calendar.timeInSeconds() = this.timeInMillis / 1000

fun Long.toTimeSeconds(): Double = (this / 1000).toDouble()

fun Long.formatTimeInMillis(pattern: String): String {
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return simpleDateFormat.format(this)
}

const val TODAY = "Today"
const val TOMORROW = "Tomorrow"
const val YESTERDAY = "Yesterday"
const val LAST_WEEK = "One week ago"
const val CURRENT_WEEK = "Current week"
const val NEXT_WEEK = "One week from now"
const val MONTH_AGO = "One month ago"
const val CURRENT_MONTH = "Current month"
const val MONTH_AHEAD = "One month from now"
const val EXACT_DAY = "Exact day"
const val NUMBER_OF_DAYS_AGO = "Number of days ago"
const val NUMBER_OF_DAYS_FROM_NOW = "Number of days from now"
const val LAST_YEAR = "Last year"
const val CURRENT_YEAR = "Current year"
const val NEXT_YEAR = "Next year"

