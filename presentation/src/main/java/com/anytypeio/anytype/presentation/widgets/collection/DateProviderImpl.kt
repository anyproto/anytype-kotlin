package com.anytypeio.anytype.presentation.widgets.collection

import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DateType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class DateProviderImpl @Inject constructor() : DateProvider {

    override fun getFormattedDateType(date: Long): DateType {
        val dateInstant = Instant.ofEpochSecond(date)
        val currentDate = dateInstant.atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDateWithZeroTime = currentDate.atStartOfDay().toLocalDate()

        return when (currentDateWithZeroTime) {
            LocalDate.now() -> DateType.TODAY
            LocalDate.now().plusDays(1) -> DateType.TOMORROW
            LocalDate.now().minusDays(1) -> DateType.YESTERDAY
            else -> DateType.EXACT_DAY
//            else -> DateUtils.getRelativeTimeSpanString(
//                date,
//                currentTime,
//                DateUtils.DAY_IN_MILLIS,
//                DateUtils.FORMAT_ABBREV_RELATIVE
//            )
        }
    }

    override fun getNowInSeconds(): Long {
        return System.currentTimeMillis() / 1000
    }

    override fun getTimestampForToday(): Long {
        val today = LocalDate.now()
        val todayWithZeroTime = today.atStartOfDay().toLocalDate()
        return todayWithZeroTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }

    override fun getTimestampForTomorrow(): Long {
        val tomorrow = LocalDate.now().plusDays(1)
        val tomorrowWithZeroTime = tomorrow.atStartOfDay().toLocalDate()
        return tomorrowWithZeroTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }

    override fun getTimestampForYesterday(): Long {
        val yesterday = LocalDate.now().minusDays(1)
        val yesterdayWithZeroTime = yesterday.atStartOfDay().toLocalDate()
        return yesterdayWithZeroTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }

    override fun getTimestampForWeekAhead(): Long {
        val weekAfter = LocalDate.now().plusWeeks(1)
        val weekAfterWithZeroTime = weekAfter.atStartOfDay().toLocalDate()
        return weekAfterWithZeroTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }

    override fun getTimestampForWeekAgo(): Long {
        val weekAgo = LocalDate.now().minusWeeks(1)
        val weekAgoWithZeroTime = weekAgo.atStartOfDay().toLocalDate()
        return weekAgoWithZeroTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }
}