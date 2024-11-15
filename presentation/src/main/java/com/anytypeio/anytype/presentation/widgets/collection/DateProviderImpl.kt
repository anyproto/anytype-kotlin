package com.anytypeio.anytype.presentation.widgets.collection

import android.text.format.DateUtils
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.TimeInSeconds
import com.anytypeio.anytype.core_models.primitives.AppDateFormat
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DateType
import com.anytypeio.anytype.domain.misc.FormattedDate
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.ZoneProvider
import com.anytypeio.anytype.domain.vault.ObserveVaultSettings
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.MINUTES
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber

class DateProviderImpl @Inject constructor(
    scope: CoroutineScope,
    private val zoneProvider: ZoneProvider,
    private val vaultSettings: ObserveVaultSettings,
    private val localeProvider: LocaleProvider,
    private val logger: Logger
) : DateProvider {

    val defaultZoneId get() = zoneProvider.zone()
    var isRelativeDates: Boolean = false
    var appDatePattern: AppDateFormat = "dd MMM yyyy"


    init {
        scope.launch {
            logger.logInfo("DateProvider initialized")
            vaultSettings.flow()
                .catch {
                    logger.logException(it, "Error while observing vault settings")
                }
                .collect {
                    isRelativeDates = it.isRelativeDates
                    appDatePattern = it.dateFormat
                }
        }
    }

    override fun calculateDateType(date: TimeInSeconds): DateType {
        val dateInstant = Instant.ofEpochSecond(date)
        val givenDate = dateInstant.atZone(zoneProvider.zone()).toLocalDate()
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
            val formatter = SimpleDateFormat(pattern, localeProvider.locale())
            return formatter.format(Date(timestamp))
        } catch (e: Exception) {
            Timber.e(e, "Error formatting timestamp to date string")
            return ""
        }
    }

    override fun formatTimestampToDateAndTime(
        timestamp: TimeInMillis,
        dateStyle: Int,
        timeStyle: Int
    ): Pair<String, String> {
        return try {
            val datePattern = (DateFormat.getDateInstance(
                dateStyle,
                localeProvider.locale()
            ) as SimpleDateFormat).toPattern()
            val timePattern = (DateFormat.getTimeInstance(
                timeStyle,
                localeProvider.locale()
            ) as SimpleDateFormat).toPattern()
            val dateFormatter = SimpleDateFormat(datePattern, localeProvider.locale())
            val timeFormatter = SimpleDateFormat(timePattern, localeProvider.locale())
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

        val truncatedDateTime1 = dateTime1.truncatedTo(MINUTES)
        val truncatedDateTime2 = dateTime2.truncatedTo(MINUTES)

        return truncatedDateTime1 == truncatedDateTime2
    }

    override fun formatDate(
        timestamp: Long,
        format: String
    ): FormattedDate {
        val date = Instant.ofEpochMilli(timestamp).atZone(defaultZoneId).toLocalDate()
        val today = LocalDate.now()
        return if (isRelativeDates) {
            when (date) {
                today -> FormattedDate.Today
                today.minusDays(1) -> FormattedDate.Yesterday
                today.plusDays(1) -> FormattedDate.Tomorrow
                else -> {
                    val formattedDate = date.format(DateTimeFormatter.ofPattern(format))
                    FormattedDate.Other(formattedDate)
                }
            }
        } else {
            val formattedDate = date.format(DateTimeFormatter.ofPattern(format))
            FormattedDate.Other(formattedDate)
        }
    }
}


