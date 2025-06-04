package com.anytypeio.anytype

import com.anytypeio.anytype.device.providers.AppDefaultDateFormatProvider
import com.anytypeio.anytype.device.providers.AppDefaultDateFormatProviderImpl
import com.anytypeio.anytype.device.providers.DateProviderImpl
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.vault.ObserveVaultSettings
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
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
    lateinit var observeVaultSettings: ObserveVaultSettings

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
}