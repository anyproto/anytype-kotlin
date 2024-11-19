package com.anytypeio.anytype

import com.anytypeio.anytype.device.providers.DateProviderImpl
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.vault.ObserveVaultSettings
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class DateProviderImplTest {

    @Mock
    lateinit var localeProvider: LocaleProvider

    @Mock
    lateinit var observeVaultSettings: ObserveVaultSettings

    lateinit var dateProviderImpl: DateProvider

    private val defaultZoneId get() = ZoneId.systemDefault()

    @Before
    fun setUp() = runTest {
        MockitoAnnotations.openMocks(this)
        dateProviderImpl = DateProviderImpl(
            defaultZoneId = defaultZoneId,
            localeProvider = localeProvider,
            vaultSettings = observeVaultSettings,
            scope = backgroundScope
        )
        Mockito.`when`(localeProvider.locale()).thenReturn(Locale.getDefault())
    }

    @Test
    fun adjustToStartOfDayInUserTimeZoneWithPastStart() {

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
            val startOfDayInLocalZone =
                dateProviderImpl.adjustFromStartOfDayInUserTimeZoneToUTC(utcTimestamp, zoneId)

            assertEquals(expected, startOfDayInLocalZone)
        }
    }

    @Test
    fun adjustToStartOfDayInUserTimeZoneWithPastMidday() {

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
        tests.forEach { (utcTimestamp, zoneId, expected) ->
            val startOfDayInLocalZone =
                dateProviderImpl.adjustFromStartOfDayInUserTimeZoneToUTC(utcTimestamp, zoneId)

            assertEquals(expected, startOfDayInLocalZone)
        }
    }

    @Test
    fun adjustToStartOfDayInUserTimeZoneWithPastEnd() {

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
            val startOfDayInLocalZone =
                dateProviderImpl.adjustFromStartOfDayInUserTimeZoneToUTC(utcTimestamp, zoneId)

            assertEquals(expected, startOfDayInLocalZone)
        }
    }

    @Test
    fun adjustToStartOfDayInUserTimeZoneWithFuture() {

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
        tests.forEach { (utcTimestamp, zoneId, expected) ->
            val startOfDayInLocalZone =
                dateProviderImpl.adjustFromStartOfDayInUserTimeZoneToUTC(utcTimestamp, zoneId)

            assertEquals(expected, startOfDayInLocalZone)
        }
    }
}