package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.TimeInSeconds
import java.text.DateFormat
import java.time.ZoneId
import java.util.Locale


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
    fun adjustToStartOfDayInUserTimeZone(timestamp: TimeInSeconds): TimeInMillis
    fun adjustFromStartOfDayInUserTimeZoneToUTC(timestamp: TimeInMillis, zoneId: ZoneId): TimeInSeconds
    fun formatToDateString(timestamp: Long, pattern: String, locale: Locale): String
    fun formatTimestampToDateAndTime(
        timestamp: TimeInMillis,
        locale: Locale,
        dateStyle: Int = DateFormat.MEDIUM,
        timeStyle: Int = DateFormat.SHORT
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