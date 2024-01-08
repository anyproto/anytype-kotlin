package com.anytypeio.anytype.presentation.widgets.collection

import android.text.format.DateUtils
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DateType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class DateProviderImpl @Inject constructor() : DateProvider {

    override fun calculateDateType(date: Long): DateType {
        val dateInstant = Instant.ofEpochSecond(date)
        val currentDate = dateInstant.atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDateWithZeroTime = currentDate.atStartOfDay().toLocalDate()

        return when (currentDateWithZeroTime) {
            LocalDate.now() -> DateType.TODAY
            LocalDate.now().plusDays(1) -> DateType.TOMORROW
            LocalDate.now().minusDays(1) -> DateType.YESTERDAY
            else -> DateType.EXACT_DAY
        }
    }

    override fun getCurrentTimestampInSeconds(): Long {
        return System.currentTimeMillis() / 1000
    }

    override fun getTimestampForTodayAtStartOfDay(): Long {
        val today = LocalDate.now()
        val todayWithZeroTime = today.atStartOfDay().toLocalDate()
        return todayWithZeroTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }

    override fun getTimestampForTomorrowAtStartOfDay(): Long {
        val tomorrow = LocalDate.now().plusDays(1)
        val tomorrowWithZeroTime = tomorrow.atStartOfDay().toLocalDate()
        return tomorrowWithZeroTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }

    override fun getTimestampForYesterdayAtStartOfDay(): Long {
        val yesterday = LocalDate.now().minusDays(1)
        val yesterdayWithZeroTime = yesterday.atStartOfDay().toLocalDate()
        return yesterdayWithZeroTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }

    override fun getTimestampForWeekAheadAtStartOfDay(): Long {
        val weekAfter = LocalDate.now().plusWeeks(1)
        val weekAfterWithZeroTime = weekAfter.atStartOfDay().toLocalDate()
        return weekAfterWithZeroTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }

    override fun getTimestampForWeekAgoAtStartOfDay(): Long {
        val weekAgo = LocalDate.now().minusWeeks(1)
        val weekAgoWithZeroTime = weekAgo.atStartOfDay().toLocalDate()
        return weekAgoWithZeroTime.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }

    override fun getRelativeTimeSpanString(date: Long): CharSequence = DateUtils.getRelativeTimeSpanString(
        date,
        System.currentTimeMillis(),
        DateUtils.DAY_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    )
}