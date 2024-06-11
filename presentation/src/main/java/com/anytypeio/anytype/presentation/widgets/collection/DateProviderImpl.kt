package com.anytypeio.anytype.presentation.widgets.collection

import android.text.format.DateUtils
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.TimeInSeconds
import com.anytypeio.anytype.core_utils.ext.formatToDateString
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DateType
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import timber.log.Timber

class DateProviderImpl @Inject constructor() : DateProvider {

    override fun calculateDateType(date: TimeInSeconds): DateType {
        val dateInstant = Instant.ofEpochSecond(date)
        val givenDate = dateInstant.atZone(ZoneId.systemDefault()).toLocalDate()
        val givenDateWithZeroTime = givenDate.atStartOfDay().toLocalDate()

        return when (givenDateWithZeroTime) {
            LocalDate.now() -> DateType.TODAY
            LocalDate.now().plusDays(1) -> DateType.TOMORROW
            LocalDate.now().minusDays(1) -> DateType.YESTERDAY
            else -> {
                val nowAtStartOfDay = LocalDate.now().atStartOfDay().toLocalDate()
                val sevenDaysAgo = nowAtStartOfDay.minusDays(7)
                val thirtyDaysAgo = nowAtStartOfDay.minusDays(30)

                if (givenDateWithZeroTime > nowAtStartOfDay)
                    DateType.UNDEFINED
                else {
                    if (givenDateWithZeroTime < thirtyDaysAgo) {
                        DateType.OLDER
                    } else {
                        if (givenDateWithZeroTime < sevenDaysAgo) {
                            DateType.PREVIOUS_THIRTY_DAYS
                        } else {
                            DateType.PREVIOUS_SEVEN_DAYS
                        }
                    }
                }
            }
        }
    }

    override fun getCurrentTimestampInSeconds(): TimeInSeconds {
        return System.currentTimeMillis() / 1000
    }

    override fun getTimestampForTodayAtStartOfDay(): TimeInSeconds {
        val today = LocalDate.now()
        val todayWithZeroTime = today.atStartOfDay().toLocalDate()
        return todayWithZeroTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }

    override fun getTimestampForTomorrowAtStartOfDay(): TimeInSeconds {
        val tomorrow = LocalDate.now().plusDays(1)
        val tomorrowWithZeroTime = tomorrow.atStartOfDay().toLocalDate()
        return tomorrowWithZeroTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }

    override fun getTimestampForYesterdayAtStartOfDay(): TimeInSeconds {
        val yesterday = LocalDate.now().minusDays(1)
        val yesterdayWithZeroTime = yesterday.atStartOfDay().toLocalDate()
        return yesterdayWithZeroTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }

    override fun getTimestampForWeekAheadAtStartOfDay(): TimeInSeconds {
        val weekAfter = LocalDate.now().plusWeeks(1)
        val weekAfterWithZeroTime = weekAfter.atStartOfDay().toLocalDate()
        return weekAfterWithZeroTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }

    override fun getTimestampForWeekAgoAtStartOfDay(): TimeInSeconds {
        val weekAgo = LocalDate.now().minusWeeks(1)
        val weekAgoWithZeroTime = weekAgo.atStartOfDay().toLocalDate()
        return weekAgoWithZeroTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }

    override fun getRelativeTimeSpanString(date: TimeInSeconds): CharSequence = DateUtils.getRelativeTimeSpanString(
        date,
        System.currentTimeMillis(),
        DateUtils.DAY_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    )

    override fun adjustToStartOfDayInUserTimeZone(timestamp: TimeInSeconds): TimeInMillis {
        val instant = Instant.ofEpochSecond(timestamp)
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        val startOfDay = localDate.atStartOfDay()
        return (startOfDay.toEpochSecond(ZoneOffset.UTC) * 1000)
    }

    override fun adjustFromStartOfDayInUserTimeZoneToUTC(timestamp: TimeInMillis): TimeInSeconds {
        // Create a Calendar instance for UTC with the given timestamp
        val calendarUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = timestamp
        }

        // Create a Calendar instance for the local time zone, setting the time to the UTC calendar's time
        val calendarLocal = Calendar.getInstance().apply {
            timeInMillis = calendarUTC.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Convert the local start of the day back to seconds
        return calendarLocal.timeInMillis / 1000
    }

    override fun formatToDateString(timestamp: Long, pattern: String, locale: Locale): String {
        try {
            val formatter = SimpleDateFormat(pattern, locale)
            return formatter.format(Date(timestamp))
        } catch (e: Exception) {
            Timber.e(e,"Error formatting timestamp to date string")
            return ""
        }
    }
}


