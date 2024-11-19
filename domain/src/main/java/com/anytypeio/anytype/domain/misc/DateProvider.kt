package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.DEFAULT_TIME_STYLE
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.TimeInSeconds
import java.time.ZoneId


/**
 * Time measure in seconds.
 */
interface DateProvider {
    fun getRelativeTimeSpanString(date: Long): CharSequence
    fun calculateDateType(date: TimeInSeconds): DateType
    fun calculateRelativeDates(timeInSeconds: TimeInSeconds?): RelativeDate?
    fun getCurrentTimestampInSeconds(): TimeInSeconds
    fun getTimestampForTodayAtStartOfDay(): TimeInSeconds
    fun getTimestampForTomorrowAtStartOfDay(): TimeInSeconds
    fun getTimestampForYesterdayAtStartOfDay(): TimeInSeconds
    fun getTimestampForWeekAheadAtStartOfDay(): TimeInSeconds
    fun getTimestampForWeekAgoAtStartOfDay(): TimeInSeconds
    fun adjustToStartOfDayInUserTimeZone(timestamp: TimeInSeconds): TimeInMillis
    fun adjustFromStartOfDayInUserTimeZoneToUTC(timestamp: TimeInMillis, zoneId: ZoneId): TimeInSeconds
    fun formatToDateString(timestamp: Long, pattern: String): String
    fun formatTimestampToDateAndTime(
        timestamp: TimeInMillis,
        timeStyle: Int = DEFAULT_TIME_STYLE
    ): Pair<String, String>
    fun isSameMinute(timestamp1: Long, timestamp2: Long): Boolean
}

interface DateTypeNameProvider {
    fun name(type: DateType) : String
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