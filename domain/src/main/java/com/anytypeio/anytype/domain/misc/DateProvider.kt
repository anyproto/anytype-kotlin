package com.anytypeio.anytype.domain.misc

/**
 * Time measure in seconds.
 */
interface DateProvider {
    fun getRelativeTimeSpanString(date: Long): CharSequence
    fun calculateDateType(date: Long): DateType
    fun getCurrentTimestampInSeconds(): Long
    fun getTimestampForTodayAtStartOfDay(): Long
    fun getTimestampForTomorrowAtStartOfDay(): Long
    fun getTimestampForYesterdayAtStartOfDay(): Long
    fun getTimestampForWeekAheadAtStartOfDay(): Long
    fun getTimestampForWeekAgoAtStartOfDay(): Long
}

enum class DateType {
    TOMORROW,
    TODAY,
    YESTERDAY,
    PREVIOUS_SEVEN_DAYS,
    PREVIOUS_THIRTY_DAYS,
    OLDER,
    UNDEFINED
}