package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.TimeInSeconds
import java.text.DateFormat.MEDIUM
import java.text.DateFormat.SHORT


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
    fun adjustFromStartOfDayInUserTimeZoneToUTC(timestamp: TimeInMillis): TimeInSeconds
    fun formatToDateString(timestamp: Long, pattern: String): String
    fun formatTimestampToDateAndTime(
        timestamp: TimeInMillis,
        dateStyle: Int = MEDIUM,
        timeStyle: Int = SHORT
    ): Pair<String, String>
    fun isSameMinute(timestamp1: Long, timestamp2: Long): Boolean
    fun formatDate(timestamp: Long, format: String): FormattedDate
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

sealed class FormattedDate {
    object Today : FormattedDate()
    object Yesterday : FormattedDate()
    object Tomorrow : FormattedDate()
    data class Other(val formattedDate: String) : FormattedDate()
}