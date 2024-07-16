package com.anytypeio.anytype.presentation.widgets.collection

import java.time.ZoneId
import org.junit.Assert.*

import org.junit.Test

class DateProviderImplTest {

    val dateProviderImpl = DateProviderImpl()

    @Test
    fun adjustToStartOfDayInUserTimeZone() {

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
            val startOfDayInLocalZone
            = dateProviderImpl.adjustFromStartOfDayInUserTimeZoneToUTC(utcTimestamp, zoneId)

            assertEquals(expected, startOfDayInLocalZone)
        }
    }

    @Test
    fun adjustToStartOfDayInUserTimeZone2() {

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
            val startOfDayInLocalZone
                    = dateProviderImpl.adjustFromStartOfDayInUserTimeZoneToUTC(utcTimestamp, zoneId)

            assertEquals(expected, startOfDayInLocalZone)
        }
    }

    @Test
    fun adjustToStartOfDayInUserTimeZone3() {

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
            val startOfDayInLocalZone
                    = dateProviderImpl.adjustFromStartOfDayInUserTimeZoneToUTC(utcTimestamp, zoneId)

            assertEquals(expected, startOfDayInLocalZone)
        }
    }

    @Test
    fun adjustToStartOfDayInUserTimeZone4() {

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
            val startOfDayInLocalZone
                    = dateProviderImpl.adjustFromStartOfDayInUserTimeZoneToUTC(utcTimestamp, zoneId)

            assertEquals(expected, startOfDayInLocalZone)
        }
    }
}