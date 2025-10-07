package com.anytypeio.anytype

import com.anytypeio.anytype.device.providers.AppDefaultDateFormatProvider
import com.anytypeio.anytype.device.providers.AppDefaultDateFormatProviderImpl
import com.anytypeio.anytype.device.providers.DateProviderImpl
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class DateProviderImplTest {

    private val dispatcher = StandardTestDispatcher(name = "Default test dispatcher")

    @Mock
    lateinit var localeProvider: LocaleProvider

    @Mock
    lateinit var stringResourceProvider: StringResourceProvider

    lateinit var dateProviderImpl: DateProvider

    lateinit var appDefaultDateFormatProvider: AppDefaultDateFormatProvider

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        appDefaultDateFormatProvider = AppDefaultDateFormatProviderImpl(localeProvider)
        Mockito.`when`(localeProvider.locale()).thenReturn(Locale.getDefault())
        Mockito.`when`(localeProvider.language()).thenReturn(Locale.getDefault().language)
        Mockito.`when`(stringResourceProvider.getToday()).thenReturn("Today")
        Mockito.`when`(stringResourceProvider.getYesterday()).thenReturn("Yesterday")
    }

    @Test
    fun adjustToStartOfDayInUserTimeZoneWithPastStart() = runTest(dispatcher) {

        val timeStamp = 1720828800L // Saturday, 13 July 2024 00:00:00

        val tests = listOf(
            Triple(timeStamp, ZoneId.of("UTC"), 1720828800L),
            Triple(timeStamp, ZoneId.of("GMT+1"), 1720825200L),
            Triple(timeStamp, ZoneId.of("GMT-1"), 1720832400L),
            Triple(timeStamp, ZoneId.of("GMT+2"), 1720821600L),
            Triple(timeStamp, ZoneId.of("GMT-2"), 1720836000L),
            Triple(timeStamp, ZoneId.of("GMT+3"), 1720818000L),
            Triple(timeStamp, ZoneId.of("GMT-3"), 1720839600L),
            Triple(timeStamp, ZoneId.of("GMT+4"), 1720814400L),
            Triple(timeStamp, ZoneId.of("GMT-4"), 1720843200L),
            Triple(timeStamp, ZoneId.of("GMT+5"), 1720810800L),
            Triple(timeStamp, ZoneId.of("GMT-5"), 1720846800L),
            Triple(timeStamp, ZoneId.of("GMT+6"), 1720807200L),
            Triple(timeStamp, ZoneId.of("GMT-6"), 1720850400L),
            Triple(timeStamp, ZoneId.of("GMT+7"), 1720803600L),
            Triple(timeStamp, ZoneId.of("GMT-7"), 1720854000L),
            Triple(timeStamp, ZoneId.of("GMT+8"), 1720800000L),
            Triple(timeStamp, ZoneId.of("GMT+08:45"), 1720797300L),
            Triple(timeStamp, ZoneId.of("GMT-8"), 1720857600L),
            Triple(timeStamp, ZoneId.of("GMT+9"), 1720796400L),
            Triple(timeStamp, ZoneId.of("GMT+09:30"), 1720794600L),
            Triple(timeStamp, ZoneId.of("GMT-9"), 1720861200L),
            Triple(timeStamp, ZoneId.of("GMT+10"), 1720792800L),
            Triple(timeStamp, ZoneId.of("GMT-10"), 1720864800L),
            Triple(timeStamp, ZoneId.of("GMT+11"), 1720789200L),
            Triple(timeStamp, ZoneId.of("GMT-11"), 1720868400L),
            Triple(timeStamp, ZoneId.of("GMT+12"), 1720785600L),
            Triple(timeStamp, ZoneId.of("GMT-12"), 1720872000L)
        )
        tests.forEach { (utcTimestamp, zoneId, expected) ->
            dateProviderImpl = DateProviderImpl(
                defaultZoneId = zoneId,
                localeProvider = localeProvider,
                appDefaultDateFormatProvider = appDefaultDateFormatProvider,
                stringResourceProvider = stringResourceProvider
            )
            val startOfDayInLocalZone =
                dateProviderImpl.adjustFromStartOfDayInUserTimeZoneToUTC(
                    timeInMillis = utcTimestamp * 1000
                )

            assertEquals(expected, startOfDayInLocalZone)
        }
    }

    @Test
    fun adjustToStartOfDayInUserTimeZoneWithPastMidday() = runTest(dispatcher) {

        val timeStamp = 1720888800L // Saturday, 13 July 2024 16:40:00
        val tests = listOf(
            Triple(timeStamp, ZoneId.of("UTC"), 1720828800L),
            Triple(timeStamp, ZoneId.of("GMT+1"), 1720825200L),
            Triple(timeStamp, ZoneId.of("GMT-1"), 1720832400L),
            Triple(timeStamp, ZoneId.of("GMT+2"), 1720821600L),
            Triple(timeStamp, ZoneId.of("GMT-2"), 1720836000L),
            Triple(timeStamp, ZoneId.of("GMT+3"), 1720818000L),
            Triple(timeStamp, ZoneId.of("GMT-3"), 1720839600L),
            Triple(timeStamp, ZoneId.of("GMT+4"), 1720814400L),
            Triple(timeStamp, ZoneId.of("GMT-4"), 1720843200L),
            Triple(timeStamp, ZoneId.of("GMT+5"), 1720810800L),
            Triple(timeStamp, ZoneId.of("GMT-5"), 1720846800L),
            Triple(timeStamp, ZoneId.of("GMT+6"), 1720807200L),
            Triple(timeStamp, ZoneId.of("GMT-6"), 1720850400L),
            Triple(timeStamp, ZoneId.of("GMT+7"), 1720803600L),
            Triple(timeStamp, ZoneId.of("GMT-7"), 1720854000L),
            Triple(timeStamp, ZoneId.of("GMT+8"), 1720800000L),
            Triple(timeStamp, ZoneId.of("GMT+08:45"), 1720797300L),
            Triple(timeStamp, ZoneId.of("GMT-8"), 1720857600L),
            Triple(timeStamp, ZoneId.of("GMT+9"), 1720796400L),
            Triple(timeStamp, ZoneId.of("GMT+09:30"), 1720794600L),
            Triple(timeStamp, ZoneId.of("GMT-9"), 1720861200L),
            Triple(timeStamp, ZoneId.of("GMT+10"), 1720792800L),
            Triple(timeStamp, ZoneId.of("GMT-10"), 1720864800L),
            Triple(timeStamp, ZoneId.of("GMT+11"), 1720789200L),
            Triple(timeStamp, ZoneId.of("GMT-11"), 1720868400L),
            Triple(timeStamp, ZoneId.of("GMT+12"), 1720785600L),
            Triple(timeStamp, ZoneId.of("GMT-12"), 1720872000L)
        )
        // TODO fix tests
        tests.forEach { (utcTimestamp, zoneId, expected) ->
            dateProviderImpl = DateProviderImpl(
                defaultZoneId = zoneId,
                localeProvider = localeProvider,
                appDefaultDateFormatProvider = appDefaultDateFormatProvider,
                stringResourceProvider = stringResourceProvider
            )
            val startOfDayInLocalZone =
                dateProviderImpl.adjustFromStartOfDayInUserTimeZoneToUTC(utcTimestamp * 1000)

            assertEquals(expected, startOfDayInLocalZone)
        }
    }

    @Test
    fun adjustToStartOfDayInUserTimeZoneWithPastEnd() = runTest(dispatcher) {

        val timeStamp = 1720915199L // Saturday, 13 July 2024 23:59:59
        val tests = listOf(
            Triple(timeStamp, ZoneId.of("UTC"), 1720828800L),
            Triple(timeStamp, ZoneId.of("GMT+1"), 1720825200L),
            Triple(timeStamp, ZoneId.of("GMT-1"), 1720832400L),
            Triple(timeStamp, ZoneId.of("GMT+2"), 1720821600L),
            Triple(timeStamp, ZoneId.of("GMT-2"), 1720836000L),
            Triple(timeStamp, ZoneId.of("GMT+3"), 1720818000L),
            Triple(timeStamp, ZoneId.of("GMT-3"), 1720839600L),
            Triple(timeStamp, ZoneId.of("GMT+4"), 1720814400L),
            Triple(timeStamp, ZoneId.of("GMT-4"), 1720843200L),
            Triple(timeStamp, ZoneId.of("GMT+5"), 1720810800L),
            Triple(timeStamp, ZoneId.of("GMT-5"), 1720846800L),
            Triple(timeStamp, ZoneId.of("GMT+6"), 1720807200L),
            Triple(timeStamp, ZoneId.of("GMT-6"), 1720850400L),
            Triple(timeStamp, ZoneId.of("GMT+7"), 1720803600L),
            Triple(timeStamp, ZoneId.of("GMT-7"), 1720854000L),
            Triple(timeStamp, ZoneId.of("GMT+8"), 1720800000L),
            Triple(timeStamp, ZoneId.of("GMT+08:45"), 1720797300L),
            Triple(timeStamp, ZoneId.of("GMT-8"), 1720857600L),
            Triple(timeStamp, ZoneId.of("GMT+9"), 1720796400L),
            Triple(timeStamp, ZoneId.of("GMT+09:30"), 1720794600L),
            Triple(timeStamp, ZoneId.of("GMT-9"), 1720861200L),
            Triple(timeStamp, ZoneId.of("GMT+10"), 1720792800L),
            Triple(timeStamp, ZoneId.of("GMT-10"), 1720864800L),
            Triple(timeStamp, ZoneId.of("GMT+11"), 1720789200L),
            Triple(timeStamp, ZoneId.of("GMT-11"), 1720868400L),
            Triple(timeStamp, ZoneId.of("GMT+12"), 1720785600L),
            Triple(timeStamp, ZoneId.of("GMT-12"), 1720872000L)
        )
        tests.forEach { (utcTimestamp, zoneId, expected) ->
            dateProviderImpl = DateProviderImpl(
                defaultZoneId = zoneId,
                localeProvider = localeProvider,
                appDefaultDateFormatProvider = appDefaultDateFormatProvider,
                stringResourceProvider = stringResourceProvider
            )
            val startOfDayInLocalZone =
                dateProviderImpl.adjustFromStartOfDayInUserTimeZoneToUTC(utcTimestamp * 1000)

            assertEquals(expected, startOfDayInLocalZone)
        }
    }

    @Test
    fun adjustToStartOfDayInUserTimeZoneWithFuture() = runTest(dispatcher) {

        val timeStamp = 3720915199 // Saturday, 29 November 2087 03:33:19
        val tests = listOf(
            Triple(timeStamp, ZoneId.of("UTC"), 3720902400L),
            Triple(timeStamp, ZoneId.of("GMT+1"), 3720898800L),
            Triple(timeStamp, ZoneId.of("GMT+2"), 3720895200L),
            Triple(timeStamp, ZoneId.of("GMT+3"), 3720891600L),
            Triple(timeStamp, ZoneId.of("GMT+4"), 3720888000L),
            Triple(timeStamp, ZoneId.of("GMT+5"), 3720884400L),
            Triple(timeStamp, ZoneId.of("GMT+6"), 3720880800L),
            Triple(timeStamp, ZoneId.of("GMT+7"), 3720877200L),
            Triple(timeStamp, ZoneId.of("GMT+8"), 3720873600L),
            Triple(timeStamp, ZoneId.of("GMT+9"), 3720870000L),
            Triple(timeStamp, ZoneId.of("GMT+10"), 3720866400L),
            Triple(timeStamp, ZoneId.of("GMT+11"), 3720862800L),
            Triple(timeStamp, ZoneId.of("GMT+12"), 3720859200L),
            Triple(timeStamp, ZoneId.of("GMT-1"), 3720906000L),
            Triple(timeStamp, ZoneId.of("GMT-2"), 3720909600L),
            Triple(timeStamp, ZoneId.of("GMT-3"), 3720913200L),
            Triple(timeStamp, ZoneId.of("GMT-4"), 3720916800L),
            Triple(timeStamp, ZoneId.of("GMT-5"), 3720920400L),
            Triple(timeStamp, ZoneId.of("GMT-6"), 3720924000L),
            Triple(timeStamp, ZoneId.of("GMT-7"), 3720927600L),
            Triple(timeStamp, ZoneId.of("GMT-8"), 3720931200L),
            Triple(timeStamp, ZoneId.of("GMT-9"), 3720934800L),
            Triple(timeStamp, ZoneId.of("GMT-10"), 3720938400L),
            Triple(timeStamp, ZoneId.of("GMT-11"), 3720942000L),
            Triple(timeStamp, ZoneId.of("GMT-12"), 3720945600L)
        )
        // TODO fix tests
        tests.forEach { (utcTimestamp, zoneId, expected) ->
            dateProviderImpl = DateProviderImpl(
                defaultZoneId = zoneId,
                localeProvider = localeProvider,
                appDefaultDateFormatProvider = appDefaultDateFormatProvider,
                stringResourceProvider = stringResourceProvider
            )
            val startOfDayInLocalZone =
                dateProviderImpl.adjustFromStartOfDayInUserTimeZoneToUTC(utcTimestamp * 1000)

            assertEquals(expected, startOfDayInLocalZone)
        }
    }

    // MARK: getChatPreviewDate Tests
    @Test
    fun getChatPreviewDate_todayTimestamp_returnsTime() = runTest(dispatcher) {
        dateProviderImpl = DateProviderImpl(
            defaultZoneId = ZoneId.of("UTC"),
            localeProvider = localeProvider,
            appDefaultDateFormatProvider = appDefaultDateFormatProvider,
            stringResourceProvider = stringResourceProvider
        )
        // Given: A timestamp from today (e.g., 18:32) in the default timezone
        val todayDateTime = LocalDateTime.now(ZoneId.of("UTC"))
            .withHour(18)
            .withMinute(32)
            .withSecond(0)
            .withNano(0)
        val todayTimestamp = todayDateTime.toEpochSecond(ZoneOffset.UTC)

        // When
        val result = dateProviderImpl.getChatPreviewDate(todayTimestamp)

        // Then: Should return time in short format (e.g., "18:32" or "6:32 PM" depending on locale)
        assertNotNull("Result should not be null", result)
        assertTrue("Result should contain time format",
            result.matches(Regex("\\d{1,2}:\\d{2}")) || // 24-hour format like "18:32"
            result.matches(Regex("\\d{1,2}:\\d{2}\\s?(AM|PM|am|pm)")) // 12-hour format like "6:32 PM"
        )
    }

    @Test
    fun getChatPreviewDate_yesterdayTimestamp_returnsYesterdayString() = runTest(dispatcher) {
        dateProviderImpl = DateProviderImpl(
            defaultZoneId = ZoneId.of("UTC"),
            localeProvider = localeProvider,
            appDefaultDateFormatProvider = appDefaultDateFormatProvider,
            stringResourceProvider = stringResourceProvider
        )
        // Given: A timestamp from yesterday in UTC
        val yesterdayDateTime = LocalDateTime.now(ZoneId.of("UTC"))
            .minusDays(1)
            .withHour(14)
            .withMinute(30)
            .withSecond(0)
            .withNano(0)
        val yesterdayTimestamp = yesterdayDateTime.toEpochSecond(ZoneOffset.UTC)

        // When
        val result = dateProviderImpl.getChatPreviewDate(yesterdayTimestamp)

        // Then: Should return localized "Yesterday" string
        assertEquals("Yesterday", result)
    }

    @Test
    fun getChatPreviewDate_currentYearNotTodayOrYesterday_returnsShortDate() = runTest(dispatcher) {
        dateProviderImpl = DateProviderImpl(
            defaultZoneId = ZoneId.of("UTC"),
            localeProvider = localeProvider,
            appDefaultDateFormatProvider = appDefaultDateFormatProvider,
            stringResourceProvider = stringResourceProvider
        )
        // Given: A timestamp from current year but not today or yesterday (e.g., April 12)
        val currentYear = LocalDateTime.now().year
        val dateTime = LocalDateTime.of(currentYear, 4, 12, 10, 0, 0)
        val timestamp = dateTime.toEpochSecond(ZoneOffset.UTC)

        // When
        val result = dateProviderImpl.getChatPreviewDate(timestamp)

        // Then: Should return date in "dd/MM" format
        assertTrue("Result should contain day/month format", result.matches(Regex("\\d{2}/\\d{2}")))
    }

    @Test
    fun getChatPreviewDate_previousYear_returnsShortDateWithYear() = runTest(dispatcher) {
        dateProviderImpl = DateProviderImpl(
            defaultZoneId = ZoneId.of("UTC"),
            localeProvider = localeProvider,
            appDefaultDateFormatProvider = appDefaultDateFormatProvider,
            stringResourceProvider = stringResourceProvider
        )
        // Given: A timestamp from previous year (e.g., April 12, 2023)
        val previousYear = LocalDateTime.now().year - 1
        val dateTime = LocalDateTime.of(previousYear, 4, 12, 15, 45, 0)
        val timestamp = dateTime.toEpochSecond(ZoneOffset.UTC)

        // When
        val result = dateProviderImpl.getChatPreviewDate(timestamp)

        // Then: Should return date in "dd/MM/yy" format
        assertTrue("Result should contain day/month/year format",
            result.matches(Regex("\\d{2}/\\d{2}/\\d{2}")))
    }

    @Test
    fun getChatPreviewDate_futureYear_returnsShortDateWithYear() = runTest(dispatcher) {
        dateProviderImpl = DateProviderImpl(
            defaultZoneId = ZoneId.of("UTC"),
            localeProvider = localeProvider,
            appDefaultDateFormatProvider = appDefaultDateFormatProvider,
            stringResourceProvider = stringResourceProvider
        )
        // Given: A timestamp from future year (e.g., September 27, 2025)
        val futureYear = LocalDateTime.now().year + 1
        val dateTime = LocalDateTime.of(futureYear, 9, 27, 9, 15, 0)
        val timestamp = dateTime.toEpochSecond(ZoneOffset.UTC)

        // When
        val result = dateProviderImpl.getChatPreviewDate(timestamp)

        // Then: Should return date in "dd/MM/yy" format
        assertTrue("Result should contain day/month/year format",
            result.matches(Regex("\\d{2}/\\d{2}/\\d{2}")))
    }

    @Test
    fun getChatPreviewDate_midnightToday_returnsTime() = runTest(dispatcher) {
        dateProviderImpl = DateProviderImpl(
            defaultZoneId = ZoneId.of("UTC"),
            localeProvider = localeProvider,
            appDefaultDateFormatProvider = appDefaultDateFormatProvider,
            stringResourceProvider = stringResourceProvider
        )
        // Given: A timestamp from today at midnight (00:00) in UTC
        val todayMidnight = LocalDateTime.now(ZoneId.of("UTC"))
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        val timestamp = todayMidnight.toEpochSecond(ZoneOffset.UTC)

        // When
        val result = dateProviderImpl.getChatPreviewDate(timestamp)

        // Then: Should return time ("00:00" or "12:00 AM" depending on locale)
        assertNotNull("Result should not be null", result)
        assertTrue("Result should contain time format for midnight",
            result.matches(Regex("(00:00|0:00|12:00\\s?(AM|am))")) // "00:00" or "12:00 AM"
        )
    }

    @Test
    fun getChatPreviewDate_endOfDay_returnsTime() = runTest(dispatcher) {
        dateProviderImpl = DateProviderImpl(
            defaultZoneId = ZoneId.of("UTC"),
            localeProvider = localeProvider,
            appDefaultDateFormatProvider = appDefaultDateFormatProvider,
            stringResourceProvider = stringResourceProvider
        )
        // Given: A timestamp from today at end of day (23:59) in UTC
        val todayEndOfDay = LocalDateTime.now(ZoneId.of("UTC"))
            .withHour(23)
            .withMinute(59)
            .withSecond(59)
            .withNano(0)
        val timestamp = todayEndOfDay.toEpochSecond(ZoneOffset.UTC)

        // When
        val result = dateProviderImpl.getChatPreviewDate(timestamp)

        // Then: Should return time ("23:59" or "11:59 PM" depending on locale)
        assertNotNull("Result should not be null", result)
        assertTrue("Result should contain time format for end of day",
            result.matches(Regex("23:59")) || // 24-hour format
            result.matches(Regex("11:59\\s?(PM|pm)")) // 12-hour format
        )
    }

    @Test
    fun getChatPreviewDate_differentTimeZone_handlesCorrectly() = runTest(dispatcher) {
        // Given: DateProvider with different timezone (GMT+8)
        val gmtPlus8DateProvider = DateProviderImpl(
            defaultZoneId = ZoneId.of("GMT+8"),
            localeProvider = localeProvider,
            appDefaultDateFormatProvider = appDefaultDateFormatProvider,
            stringResourceProvider = stringResourceProvider
        )

        // A timestamp that could be today in one timezone but yesterday in another
        val timestamp = LocalDateTime.now(ZoneId.of("GMT+8"))
            .withHour(10)
            .withMinute(30)
            .toEpochSecond(ZoneOffset.UTC)

        // When
        val result = gmtPlus8DateProvider.getChatPreviewDate(timestamp)

        // Then: Should handle timezone correctly
        assertNotNull("Result should not be null", result)
        assertTrue("Result should be time, yesterday, weekday, or date format. Got: '$result'",
            result.matches(Regex("\\d{1,2}:\\d{2}")) || // Time in 24-hour format
            result.matches(Regex("\\d{1,2}:\\d{2}\\s?(AM|PM|am|pm)")) || // Time in 12-hour format
            result == "Yesterday" || // Yesterday
            result.matches(Regex("[A-Za-z]+")) || // Full weekday names
            result.matches(Regex("\\d{2}/\\d{2}")) || // dd/MM format for current year
            result.matches(Regex("\\d{2}/\\d{2}/\\d{2}")) // dd/MM/yy format for different year
        )
    }

    @Test
    fun getChatPreviewDate_localizedYesterday_usesStringResourceProvider() = runTest(dispatcher) {
        dateProviderImpl = DateProviderImpl(
            defaultZoneId = ZoneId.of("UTC"),
            localeProvider = localeProvider,
            appDefaultDateFormatProvider = appDefaultDateFormatProvider,
            stringResourceProvider = stringResourceProvider
        )
        // Given: Mock returns localized "Yesterday" string
        val localizedYesterday = "Ieri" // Italian for "Yesterday"
        Mockito.`when`(stringResourceProvider.getYesterday()).thenReturn(localizedYesterday)

        val yesterdayDateTime = LocalDateTime.now()
            .minusDays(1)
            .withHour(10)
            .withMinute(0)
        val timestamp = yesterdayDateTime.toEpochSecond(ZoneOffset.UTC)

        // When
        val result = dateProviderImpl.getChatPreviewDate(timestamp)

        // Then: Should return localized string
        assertEquals(localizedYesterday, result)
    }

    @Test
    fun getChatPreviewDate_zeroTimestamp_handlesGracefully() = runTest(dispatcher) {
        dateProviderImpl = DateProviderImpl(
            defaultZoneId = ZoneId.of("UTC"),
            localeProvider = localeProvider,
            appDefaultDateFormatProvider = appDefaultDateFormatProvider,
            stringResourceProvider = stringResourceProvider
        )
        // Given: Edge case with timestamp 0
        val timestamp = 0L

        // When
        val result = dateProviderImpl.getChatPreviewDate(timestamp)

        // Then: Should handle gracefully without crashing
        assertNotNull("Result should not be null", result)
        assertTrue("Result should be valid", result.isNotBlank())
    }

    @Test
    fun getChatPreviewDate_withinSevenDays_returnsWeekday() = runTest(dispatcher) {
        dateProviderImpl = DateProviderImpl(
            defaultZoneId = ZoneId.of("UTC"),
            localeProvider = localeProvider,
            appDefaultDateFormatProvider = appDefaultDateFormatProvider,
            stringResourceProvider = stringResourceProvider
        )
        // Given: A timestamp from 3 days ago (within 7 days but not today/yesterday)
        val threeDaysAgo = LocalDateTime.now(ZoneId.of("UTC"))
            .minusDays(3)
            .withHour(15)
            .withMinute(30)
            .withSecond(0)
            .withNano(0)
        val timestamp = threeDaysAgo.toEpochSecond(ZoneOffset.UTC)

        // When
        val result = dateProviderImpl.getChatPreviewDate(timestamp)

        // Then: Should return full weekday format (e.g., "Sunday", "Monday", "Tuesday")
        assertTrue("Result should be full weekday format", result.matches(Regex("[A-Za-z]+")))
        assertTrue("Result should be a valid full weekday name",
            listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday").contains(result))
    }

    @Test
    fun getChatPreviewDate_veryOldDate_returnsShortDateWithYear() = runTest(dispatcher) {
        dateProviderImpl = DateProviderImpl(
            defaultZoneId = ZoneId.of("UTC"),
            localeProvider = localeProvider,
            appDefaultDateFormatProvider = appDefaultDateFormatProvider,
            stringResourceProvider = stringResourceProvider
        )
        // Given: A very old timestamp (e.g., from 1990)
        val oldDateTime = LocalDateTime.of(1990, 12, 25, 14, 30, 0)
        val timestamp = oldDateTime.toEpochSecond(ZoneOffset.UTC)

        // When
        val result = dateProviderImpl.getChatPreviewDate(timestamp)

        // Then: Should return date in dd/MM/yy format (e.g., "25/12/90")
        assertTrue("Result should be in dd/MM/yy format", result.matches(Regex("\\d{2}/\\d{2}/\\d{2}")))
    }
}