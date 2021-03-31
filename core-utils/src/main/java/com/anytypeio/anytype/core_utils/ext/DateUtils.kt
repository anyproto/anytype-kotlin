package com.anytypeio.anytype.core_utils.ext

import java.text.SimpleDateFormat
import java.util.*

fun Calendar.isSameDay(compare: Calendar): Boolean =
    this.get(Calendar.YEAR) == compare.get(Calendar.YEAR)
            && this.get(Calendar.MONTH) == compare.get(Calendar.MONTH)
            && this.get(Calendar.DAY_OF_MONTH) == compare.get(Calendar.DAY_OF_MONTH)

fun Calendar.timeInSeconds() = this.timeInMillis / 1000

fun getTodayTimeUnit() = Calendar.getInstance()
fun getTomorrowTimeUnit(): Calendar = Calendar.getInstance().apply { add(Calendar.DATE, 1) }
fun getYesterdayTimeUnit(): Calendar = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
fun getWeekAheadTimeUnit(): Calendar =
    Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, 1) }

fun getWeekAgoTimeUnit(): Calendar = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, -1) }
fun getMonthAheadTimeUnit(): Calendar = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
fun getMonthAgoTimeUnit(): Calendar = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }

fun Long.toTimeSeconds(): Double = (this / 1000).toDouble()

fun Long.formatTimestamp(isMillis: Boolean): String {
    val filterTime = Calendar.getInstance()
    if (isMillis) {
        filterTime.timeInMillis = this
    } else {
        filterTime.timeInMillis = this * 1000
    }

    val today = getTodayTimeUnit()
    val tomorrow = getTomorrowTimeUnit()
    val yesterday = getYesterdayTimeUnit()
    val weekAgo = getWeekAgoTimeUnit()
    val weekForward = getWeekAheadTimeUnit()
    val monthAgo = getMonthAgoTimeUnit()
    val monthForward = getMonthAheadTimeUnit()

    val isToday = filterTime.isSameDay(today)
    if (isToday) return TODAY
    val isTomorrow = filterTime.isSameDay(tomorrow)
    if (isTomorrow) return TOMORROW
    val isYesterday = filterTime.isSameDay(yesterday)
    if (isYesterday) return YESTERDAY
    val isWeekAgo = filterTime.isSameDay(weekAgo)
    if (isWeekAgo) return WEEK_AGO
    val isWeekAhead = filterTime.isSameDay(weekForward)
    if (isWeekAhead) return WEEK_AHEAD
    val isMonthAgo = filterTime.isSameDay(monthAgo)
    if (isMonthAgo) return MONTH_AGO
    val isMonthAhead = filterTime.isSameDay(monthForward)
    if (isMonthAhead) return MONTH_AHEAD
    val isExactDay = !isToday && !isTomorrow && !isYesterday && !isWeekAgo && !isWeekAhead
            && !isMonthAgo && !isMonthAhead
    return if (isExactDay) {
        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        simpleDateFormat.format(filterTime.time)
    } else {
        ""
    }
}

fun Long.timeInSecondsFormat(pattern: String): String {
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return simpleDateFormat.format(this * 1000)
}

const val TODAY = "Today"
const val TOMORROW = "Tomorrow"
const val YESTERDAY = "Yesterday"
const val WEEK_AGO = "One week ago"
const val WEEK_AHEAD = "One week from now"
const val MONTH_AGO = "One month ago"
const val MONTH_AHEAD = "One month from now"
const val EXACT_DAY = "Exact day"

const val EMPTY_TIMESTAMP = 0L
