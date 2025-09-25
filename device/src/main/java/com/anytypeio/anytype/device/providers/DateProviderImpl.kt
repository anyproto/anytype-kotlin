package com.anytypeio.anytype.device.providers

import android.text.format.DateUtils
import com.anytypeio.anytype.core_models.DayOfWeekCustom
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.TimeInSeconds
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DateType
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.resources.StringResourceProvider
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
import timber.log.Timber

class DateProviderImpl @Inject constructor(
    private val defaultZoneId: ZoneId,
    private val localeProvider: LocaleProvider,
    private val appDefaultDateFormatProvider: AppDefaultDateFormatProvider,
    private val stringResourceProvider: StringResourceProvider
) : DateProvider {

    private val defaultDateFormat get() = appDefaultDateFormatProvider.provide()

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

    override fun getTimestampForLastYearAtStartOfYear(): TimeInSeconds {
        val currentDate = LocalDate.now(defaultZoneId)
        val startOfLastYear = LocalDate.of(currentDate.year - 1, 1, 1)
        return startOfLastYear.atStartOfDay(defaultZoneId).toEpochSecond()
    }

    override fun getTimestampForCurrentYearAtStartOfYear(): TimeInSeconds {
        val currentDate = LocalDate.now(defaultZoneId)
        val startOfCurrentYear = LocalDate.of(currentDate.year, 1, 1)
        return startOfCurrentYear.atStartOfDay(defaultZoneId).toEpochSecond()
    }

    override fun getTimestampForNextYearAtStartOfYear(): TimeInSeconds {
        val currentDate = LocalDate.now(defaultZoneId)
        val startOfNextYear = LocalDate.of(currentDate.year + 1, 1, 1)
        return startOfNextYear.atStartOfDay(defaultZoneId).toEpochSecond()
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
        timeInMillis: TimeInMillis
    ): TimeInSeconds {
        // Convert the timestamp to an Instant
        val instant = Instant.ofEpochMilli(timeInMillis)

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
            formatter.timeZone = java.util.TimeZone.getTimeZone(defaultZoneId)
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
        return try {
            val locale = localeProvider.locale()
            val datePattern = defaultDateFormat
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

    override fun calculateRelativeDates(dateInSeconds: TimeInSeconds?): RelativeDate {

        if (dateInSeconds == null || dateInSeconds == 0L) return RelativeDate.Empty
        val initialTimeInMillis = dateInSeconds * 1000
        val zoneId = defaultZoneId
        val dateInstant = Instant.ofEpochSecond(dateInSeconds)
        val givenDate = dateInstant.atZone(zoneId).toLocalDate()
        val today = LocalDate.now(zoneId)

        val daysDifference = ChronoUnit.DAYS.between(today, givenDate)

        // Convert java.time.DayOfWeek to DayOfWeekCustom
        val dayOfWeekCustom = when (givenDate.dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> DayOfWeekCustom.MONDAY
            java.time.DayOfWeek.TUESDAY -> DayOfWeekCustom.TUESDAY
            java.time.DayOfWeek.WEDNESDAY -> DayOfWeekCustom.WEDNESDAY
            java.time.DayOfWeek.THURSDAY -> DayOfWeekCustom.THURSDAY
            java.time.DayOfWeek.FRIDAY -> DayOfWeekCustom.FRIDAY
            java.time.DayOfWeek.SATURDAY -> DayOfWeekCustom.SATURDAY
            java.time.DayOfWeek.SUNDAY -> DayOfWeekCustom.SUNDAY
        }

        return when (daysDifference) {
            0L -> RelativeDate.Today(
                initialTimeInMillis = initialTimeInMillis,
                dayOfWeek = dayOfWeekCustom
            )

            1L -> RelativeDate.Tomorrow(
                initialTimeInMillis = initialTimeInMillis,
                dayOfWeek = dayOfWeekCustom
            )

            -1L -> RelativeDate.Yesterday(
                initialTimeInMillis = initialTimeInMillis,
                dayOfWeek = dayOfWeekCustom
            )

            else -> {
                val timestampMillis = TimeUnit.SECONDS.toMillis(dateInSeconds)

                val (dateString, timeString) = formatTimestampToDateAndTime(timestampMillis)
                RelativeDate.Other(
                    initialTimeInMillis = initialTimeInMillis,
                    formattedDate = dateString,
                    formattedTime = timeString,
                    dayOfWeek = dayOfWeekCustom
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

    override fun getChatPreviewDate(
        timeInSeconds: TimeInSeconds,
        timeStyle: Int
    ): String {
        val dateType = calculateDateType(timeInSeconds)
        val timestamp = timeInSeconds * 1000 // Convert seconds to milliseconds

        return when (dateType) {
            DateType.TODAY -> {
                // Show "Today"
                stringResourceProvider.getToday()
            }
            DateType.YESTERDAY -> {
                // Show "Yesterday" localized
                stringResourceProvider.getYesterday()
            }
            DateType.PREVIOUS_SEVEN_DAYS -> {
                // Show short weekday format in locale (e.g., "Sun", "Fri")
                formatToDateString(timestamp, "EEE")
            }
            else -> {
                // Check if it's current year
                val currentYear = getLocalDateOfTime(System.currentTimeMillis()).year
                val messageYear = getLocalDateOfTime(timestamp).year

                if (currentYear == messageYear) {
                    // Show short date format in locale (e.g., "02/09")
                    formatToDateString(timestamp, "dd/MM")
                } else {
                    // Show short date + year format (e.g., "29/08/24")
                    formatToDateString(timestamp, "dd/MM/yy")
                }
            }
        }
    }
}


