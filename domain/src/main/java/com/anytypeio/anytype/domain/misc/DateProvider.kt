package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.TimeInSeconds


/**
 * Time measure in seconds.
 */
interface DateProvider {
    fun getRelativeTimeSpanString(date: Long): CharSequence
    fun calculateDateType(date: TimeInSeconds): DateType
    fun getCurrentTimestampInSeconds(): TimeInSeconds
    fun getTimestampForTodayAtStartOfDay(): TimeInSeconds
    fun getTimestampForTomorrowAtStartOfDay(): TimeInSeconds
    fun getTimestampForYesterdayAtStartOfDay(): TimeInSeconds
    fun getTimestampForWeekAheadAtStartOfDay(): TimeInSeconds
    fun getTimestampForWeekAgoAtStartOfDay(): TimeInSeconds
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