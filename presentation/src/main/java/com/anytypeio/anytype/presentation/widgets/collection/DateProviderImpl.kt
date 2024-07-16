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

    override fun adjustFromStartOfDayInUserTimeZoneToUTC(timestamp: TimeInMillis, zoneId: ZoneId): TimeInSeconds {
        // Преобразуем timestamp в Instant
        val instant = Instant.ofEpochSecond(timestamp)

        // Преобразуем Instant в ZonedDateTime в UTC
        val utcDateTime = instant.atZone(ZoneOffset.UTC)

        // Преобразуем UTC ZonedDateTime в локальную временную зону
        val localDateTime = utcDateTime.withZoneSameInstant(zoneId)

        // Получаем локальную дату и начало дня в локальной временной зоне
        val localDate = localDateTime.toLocalDate()
        val startOfDay = localDate.atStartOfDay(zoneId)

        // Проверяем, если UTC timestamp находится на границе дня в локальной временной зоне
        if (utcDateTime.toLocalDate() != startOfDay.toLocalDate()) {
            // Если разница в днях есть, возвращаем начало следующего дня
            return startOfDay.plusDays(1).toEpochSecond()
        }

        return startOfDay.toEpochSecond()
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


