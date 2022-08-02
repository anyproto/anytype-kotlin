package com.anytypeio.anytype.core_utils.ext

import com.anytypeio.anytype.core_utils.const.DateConst.DEFAULT_DATE_FORMAT
import java.text.SimpleDateFormat
import java.util.*

fun Calendar.isSameDay(compare: Calendar): Boolean =
    this.get(Calendar.YEAR) == compare.get(Calendar.YEAR)
            && this.get(Calendar.MONTH) == compare.get(Calendar.MONTH)
            && this.get(Calendar.DAY_OF_MONTH) == compare.get(Calendar.DAY_OF_MONTH)

fun Calendar.timeInSeconds() = this.timeInMillis / 1000

fun getTodayTimeUnit(): Calendar = Calendar.getInstance()
fun getTomorrowTimeUnit(): Calendar = Calendar.getInstance().apply { add(Calendar.DATE, 1) }
fun getYesterdayTimeUnit(): Calendar = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
fun getWeekAheadTimeUnit(): Calendar =
    Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, 1) }

fun getWeekAgoTimeUnit(): Calendar = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, -1) }
fun getMonthAheadTimeUnit(): Calendar = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
fun getMonthAgoTimeUnit(): Calendar = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }

fun Long.toTimeSeconds(): Double = (this / 1000).toDouble()
fun Long.toTimeSecondsLong(): Long = (this / 1000)

fun Long.formatTimestamp(isMillis: Boolean, format: String? = null): String {
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
    if (isWeekAgo) return LAST_WEEK
    val isWeekAhead = filterTime.isSameDay(weekForward)
    if (isWeekAhead) return NEXT_WEEK
    val isMonthAgo = filterTime.isSameDay(monthAgo)
    if (isMonthAgo) return MONTH_AGO
    val isMonthAhead = filterTime.isSameDay(monthForward)
    if (isMonthAhead) return MONTH_AHEAD
    val isExactDay = !isToday && !isTomorrow && !isYesterday && !isWeekAgo && !isWeekAhead
            && !isMonthAgo && !isMonthAhead
    return if (isExactDay) {
        val simpleDateFormat = if (format != null) {
            SimpleDateFormat(format, Locale.getDefault())
        } else {
            SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault())
        }
        simpleDateFormat.format(filterTime.time)
    } else {
        ""
    }
}

fun Long.timeInSecondsFormat(pattern: String): String {
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return simpleDateFormat.format(this * 1000)
}

const val NO_DATE = "No date"
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

