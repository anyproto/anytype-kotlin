package com.anytypeio.anytype.ui.sets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.sets.model.Viewer
import java.time.DateTimeException
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

private const val MAX_ENTRIES_PER_CELL = 3

private val YearMonthSaver: Saver<YearMonth, IntArray> = Saver(
    save = { intArrayOf(it.year, it.monthValue) },
    restore = { YearMonth.of(it[0], it[1]) }
)

@Composable
fun CalendarBoard(
    viewer: Viewer.CalendarView,
    onEntryClicked: (Id) -> Unit
) {
    var currentYearMonth by rememberSaveable(stateSaver = YearMonthSaver) {
        mutableStateOf(YearMonth.now())
    }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val locale = Locale.getDefault()
    val firstDayOfWeek = remember(locale) { WeekFields.of(locale).firstDayOfWeek }
    val dayLabels = remember(firstDayOfWeek, locale) {
        (0 until 7).map { offset ->
            val dow = DayOfWeek.of(((firstDayOfWeek.value - 1 + offset) % 7) + 1)
            dow.getDisplayName(TextStyle.SHORT, locale)
        }
    }

    val entriesByDate: Map<LocalDate, List<Viewer.CalendarView.Entry>> = remember(viewer.entries) {
        viewer.entries
            .mapNotNull { entry ->
                try {
                    val date = Instant.ofEpochSecond(entry.dateInSeconds)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    date to entry
                } catch (_: DateTimeException) {
                    null
                }
            }
            .groupBy({ it.first }, { it.second })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background_primary))
    ) {
        CalendarHeader(
            currentYearMonth = currentYearMonth,
            onPrevMonth = { currentYearMonth = currentYearMonth.minusMonths(1) },
            onNextMonth = { currentYearMonth = currentYearMonth.plusMonths(1) }
        )
        CalendarDayOfWeekRow(dayLabels = dayLabels)
        CalendarGrid(
            yearMonth = currentYearMonth,
            firstDayOfWeek = firstDayOfWeek,
            entriesByDate = entriesByDate,
            onEntryClicked = onEntryClicked,
            onDateClicked = { date -> selectedDate = date }
        )
    }

    selectedDate?.let { date ->
        DayDetailBottomSheet(
            date = date,
            entries = entriesByDate[date].orEmpty(),
            onDismiss = { selectedDate = null },
            onEntryClicked = { id ->
                selectedDate = null
                onEntryClicked(id)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailBottomSheet(
    date: LocalDate,
    entries: List<Viewer.CalendarView.Entry>,
    onDismiss: () -> Unit,
    onEntryClicked: (Id) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val locale = Locale.getDefault()
    val title = remember(date, locale) {
        date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorResource(R.color.background_primary)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.text_primary),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = entries.size.toString(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.text_secondary)
                )
            }
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(items = entries, key = { it.objectId }) { entry ->
                    DayDetailEntryRow(
                        entry = entry,
                        onClick = { onEntryClicked(entry.objectId) }
                    )
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun DayDetailEntryRow(
    entry: Viewer.CalendarView.Entry,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ListWidgetObjectIcon(
            icon = entry.icon,
            iconSize = 20.dp,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = entry.name,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = colorResource(R.color.text_primary),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CalendarHeader(
    currentYearMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val locale = Locale.getDefault()
    val monthName = currentYearMonth.month.getDisplayName(TextStyle.FULL, locale)
    val year = currentYearMonth.year

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevMonth, modifier = Modifier.size(36.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward_24),
                contentDescription = "Previous month",
                tint = colorResource(R.color.text_primary),
                modifier = Modifier.graphicsLayer { scaleX = -1f }
            )
        }
        Text(
            text = "$monthName $year",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = colorResource(R.color.text_primary),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(onClick = onNextMonth, modifier = Modifier.size(36.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward_24),
                contentDescription = "Next month",
                tint = colorResource(R.color.text_primary)
            )
        }
    }
}

@Composable
private fun CalendarDayOfWeekRow(dayLabels: List<String>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        dayLabels.forEach { day ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.text_secondary),
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    firstDayOfWeek: DayOfWeek,
    entriesByDate: Map<LocalDate, List<Viewer.CalendarView.Entry>>,
    onEntryClicked: (Id) -> Unit,
    onDateClicked: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val weeks = remember(yearMonth, firstDayOfWeek) { yearMonth.weeks(firstDayOfWeek) }

    Column(modifier = Modifier.fillMaxSize()) {
        weeks.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                week.forEach { date ->
                    val entries = date?.let { entriesByDate[it] } ?: emptyList()
                    val isCurrentMonth = date?.month == yearMonth.month
                    CalendarDayCell(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        date = date,
                        isCurrentMonth = isCurrentMonth,
                        isToday = date == today,
                        entries = entries,
                        onEntryClicked = onEntryClicked,
                        onCellClicked = if (date != null && isCurrentMonth && entries.isNotEmpty()) {
                            { onDateClicked(date) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    modifier: Modifier = Modifier,
    date: LocalDate?,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    entries: List<Viewer.CalendarView.Entry>,
    onEntryClicked: (Id) -> Unit,
    onCellClicked: (() -> Unit)?
) {
    val baseModifier = modifier
        .padding(1.dp)
        .background(colorResource(R.color.background_primary))
        .border(
            width = if (isToday) 2.dp else 0.5.dp,
            color = if (isToday)
                colorResource(R.color.palette_system_sky)
            else
                colorResource(R.color.shape_primary)
        )
    val clickableModifier = if (onCellClicked != null) {
        baseModifier.clickable(onClick = onCellClicked)
    } else {
        baseModifier
    }

    Column(
        modifier = clickableModifier.padding(4.dp)
    ) {
        if (date != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = if (isToday) {
                        Modifier
                            .size(22.dp)
                            .background(colorResource(R.color.palette_system_sky), CircleShape)
                    } else {
                        Modifier.size(22.dp)
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        fontSize = 11.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isToday -> colorResource(R.color.background_primary)
                            isCurrentMonth -> colorResource(R.color.text_primary)
                            else -> colorResource(R.color.text_tertiary)
                        }
                    )
                }
                if (isCurrentMonth && entries.isNotEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = entries.size.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.text_secondary)
                    )
                }
            }

            if (isCurrentMonth) {
                val visibleEntries = entries.take(MAX_ENTRIES_PER_CELL)
                val overflow = entries.size - visibleEntries.size

                visibleEntries.forEach { entry ->
                    CalendarEntryChip(entry = entry, onClick = { onEntryClicked(entry.objectId) })
                }

                if (overflow > 0) {
                    Text(
                        text = "+$overflow more",
                        fontSize = 9.sp,
                        color = colorResource(R.color.text_secondary),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarEntryChip(
    entry: Viewer.CalendarView.Entry,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
            .background(
                color = colorResource(R.color.shape_tertiary),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!entry.hideIcon) {
            ListWidgetObjectIcon(
                icon = entry.icon,
                iconSize = 12.dp,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.size(3.dp))
        }
        if (!entry.hideName) {
            Text(
                text = entry.name,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colorResource(R.color.text_primary)
            )
        }
    }
}

private fun YearMonth.weeks(firstDayOfWeek: DayOfWeek): List<List<LocalDate?>> {
    val firstDay = atDay(1)
    val offset = ((firstDay.dayOfWeek.value - firstDayOfWeek.value) + 7) % 7
    val daysInMonth = lengthOfMonth()
    val totalCells = offset + daysInMonth
    val rows = (totalCells + 6) / 7

    return (0 until rows).map { week ->
        (0 until 7).map { col ->
            val dayIndex = week * 7 + col - offset + 1
            if (dayIndex in 1..daysInMonth) atDay(dayIndex) else null
        }
    }
}
