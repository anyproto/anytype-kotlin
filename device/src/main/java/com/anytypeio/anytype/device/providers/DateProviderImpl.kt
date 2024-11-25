package com.anytypeio.anytype.device.providers

import android.text.format.DateUtils
import com.anytypeio.anytype.core_models.FALLBACK_DATE_PATTERN
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.TimeInSeconds
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DateType
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.vault.ObserveVaultSettings
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DateProviderImpl @Inject constructor(
    private val defaultZoneId: ZoneId,
    private val localeProvider: LocaleProvider,
    private val vaultSettings: ObserveVaultSettings,
    scope: CoroutineScope
) : DateProvider {

    private val defaultDateFormat = MutableStateFlow(FALLBACK_DATE_PATTERN)

    init {
        scope.launch {
            vaultSettings.flow().collect { settings ->
                defaultDateFormat.value = settings.dateFormat
            }
        }
    }

    override fun calculateDateType(date: TimeInSeconds): DateType {
        val dateInstant = Instant.ofEpochSecond(date)
        val givenDate = dateInstant.atZone(defaultZoneId).toLocalDate()
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
        return todayWithZeroTime.atStartOfDay(defaultZoneId).toEpochSecond()
    }

    override fun getTimestampForTomorrowAtStartOfDay(): TimeInSeconds {
        val tomorrow = LocalDate.now().plusDays(1)
        val tomorrowWithZeroTime = tomorrow.atStartOfDay().toLocalDate()
        return tomorrowWithZeroTime.atStartOfDay(defaultZoneId).toEpochSecond()
    }

    override fun getTimestampForYesterdayAtStartOfDay(): TimeInSeconds {
        val yesterday = LocalDate.now().minusDays(1)
        val yesterdayWithZeroTime = yesterday.atStartOfDay().toLocalDate()
        return yesterdayWithZeroTime.atStartOfDay(defaultZoneId).toEpochSecond()
    }

    override fun getTimestampForWeekAheadAtStartOfDay(): TimeInSeconds {
        val weekAfter = LocalDate.now().plusWeeks(1)
        val weekAfterWithZeroTime = weekAfter.atStartOfDay().toLocalDate()
        return weekAfterWithZeroTime.atStartOfDay(defaultZoneId).toEpochSecond()
    }

    override fun getTimestampForWeekAgoAtStartOfDay(): TimeInSeconds {
        val weekAgo = LocalDate.now().minusWeeks(1)
        val weekAgoWithZeroTime = weekAgo.atStartOfDay().toLocalDate()
        return weekAgoWithZeroTime.atStartOfDay(defaultZoneId).toEpochSecond()
    }

    override fun getRelativeTimeSpanString(date: TimeInSeconds): CharSequence =
        DateUtils.getRelativeTimeSpanString(
            date,
            System.currentTimeMillis(),
            DateUtils.DAY_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        )

    override fun adjustToStartOfDayInUserTimeZone(timestamp: TimeInSeconds): TimeInMillis {
        val instant = Instant.ofEpochSecond(timestamp)
        val localDate = instant.atZone(defaultZoneId).toLocalDate()
        val startOfDay = localDate.atStartOfDay()
        return (startOfDay.toEpochSecond(ZoneOffset.UTC) * 1000)
    }

    override fun adjustFromStartOfDayInUserTimeZoneToUTC(
        timestamp: TimeInMillis
    ): TimeInSeconds {
        // Convert the timestamp to an Instan
        val instant = Instant.ofEpochSecond(timestamp)

        // Convert the Instant to a ZonedDateTime in UTC
        val utcDateTime = instant.atZone(ZoneOffset.UTC)

        // Convert the UTC ZonedDateTime to the local time zone
        val localDateTime = utcDateTime.withZoneSameInstant(defaultZoneId)

        // Get the local date and the start of the day in the local time zone
        val localDate = localDateTime.toLocalDate()
        val startOfDay = localDate.atStartOfDay(defaultZoneId)

        // Check if the UTC timestamp is at the boundary of the day in the local time zone
        return when {
            utcDateTime.toLocalDate().isAfter(startOfDay.toLocalDate()) -> {
                // If the UTC timestamp is after the start of the day in the local time zone, return the start of the next day
                startOfDay.plusDays(1).toEpochSecond()
            }

            utcDateTime.toLocalDate().isBefore(startOfDay.toLocalDate()) -> {
                // If the UTC timestamp is before the start of the day in the local time zone, return the start of the previous day
                startOfDay.minusDays(1).toEpochSecond()
            }

            else -> {
                // Otherwise, return the start of the day
                startOfDay.toEpochSecond()
            }
        }
    }

    override fun formatToDateString(timestamp: Long, pattern: String): String {
        try {
            val locale = localeProvider.locale()
            val formatter = SimpleDateFormat(pattern, locale)
            return formatter.format(Date(timestamp))
        } catch (e: Exception) {
            Timber.e(e, "Error formatting timestamp to date string")
            return ""
        }
    }

    override fun formatTimestampToDateAndTime(
        timestamp: TimeInMillis,
        timeStyle: Int
    ): Pair<String, String> {
        Timber.d("Formatting timestamp [$timestamp] to date and time")
        return try {
            val locale = localeProvider.locale()
            val datePattern = defaultDateFormat.value
            val timePattern =
                (DateFormat.getTimeInstance(timeStyle, locale) as SimpleDateFormat).toPattern()
            val dateFormatter = SimpleDateFormat(datePattern, locale)
            val timeFormatter = SimpleDateFormat(timePattern, locale)
            val date = Date(timestamp)
            val dateString = dateFormatter.format(date)
            val timeString = timeFormatter.format(date)
            Pair(dateString, timeString)
        } catch (e: Exception) {
            Timber.e(e, "Error formatting timestamp to date and time string")
            Pair("", "")
        }
    }

    override fun isSameMinute(timestamp1: Long, timestamp2: Long): Boolean {
        val dateTime1 = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp1), defaultZoneId)
        val dateTime2 = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp2), defaultZoneId)

        val truncatedDateTime1 = dateTime1.truncatedTo(ChronoUnit.MINUTES)
        val truncatedDateTime2 = dateTime2.truncatedTo(ChronoUnit.MINUTES)

        return truncatedDateTime1 == truncatedDateTime2
    }

    override fun calculateRelativeDates(dateInSeconds: TimeInSeconds?): RelativeDate? {

        if (dateInSeconds == null || dateInSeconds == 0L) return null
        val zoneId = defaultZoneId
        val dateInstant = Instant.ofEpochSecond(dateInSeconds)
        val givenDate = dateInstant.atZone(zoneId).toLocalDate()
        val today = LocalDate.now(zoneId)

        val daysDifference = ChronoUnit.DAYS.between(today, givenDate)

        return when (daysDifference) {
            0L -> RelativeDate.Today
            1L -> RelativeDate.Tomorrow
            -1L -> RelativeDate.Yesterday
            else -> {
                val timestampMillis = TimeUnit.SECONDS.toMillis(dateInSeconds)

                val (dateString, timeString) = formatTimestampToDateAndTime(timestampMillis)
                RelativeDate.Other(
                    formattedDate = dateString,
                    formattedTime = timeString
                )
            }
        }
    }

    override fun getLocalDateOfTime(epochMilli: Long): LocalDate {
        val instant = Instant.ofEpochMilli(epochMilli)
        return instant.atZone(defaultZoneId).toLocalDate()
    }

    override fun isTimestampWithinYearRange(timeStampInMillis: Long, yearRange: IntRange): Boolean {
        // Convert the timestamp in milliseconds to an Instant object
        val instant = Instant.ofEpochMilli(timeStampInMillis)

        // Convert the Instant to a LocalDate object in default time zone
        val date = instant.atZone(defaultZoneId).toLocalDate()

        // Extract the year from the LocalDate object
        val year = date.year

        // Check if the year is within the desired range
        return year in yearRange.first()..yearRange.last()
    }
}


