package com.anytypeio.anytype.domain.misc

interface DateProvider {

    fun getFormattedDateType(date: Long): DateType
    fun getNowInSeconds(): Long

    fun getTimestampForToday(): Long
    fun getTimestampForTomorrow(): Long
    fun getTimestampForYesterday(): Long
    fun getTimestampForWeekAhead(): Long
    fun getTimestampForWeekAgo(): Long

    fun getRelativeTimeSpanString(date: Long): CharSequence
}

enum class DateType {
    TODAY,
    TOMORROW,
    YESTERDAY,
    EXACT_DAY
}