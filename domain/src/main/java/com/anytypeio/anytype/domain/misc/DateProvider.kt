package com.anytypeio.anytype.domain.misc

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
    TODAY,
    TOMORROW,
    YESTERDAY,
    EXACT_DAY
}