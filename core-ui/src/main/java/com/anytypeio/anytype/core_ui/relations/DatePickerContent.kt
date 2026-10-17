package com.anytypeio.anytype.core_ui.relations

import android.text.format.DateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.anytypeio.anytype.core_models.DATE_PICKER_YEAR_RANGE
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.presentation.sets.DateValueView
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerContent(
    state: DateValueView,
    showHeader: Boolean = false,
    showOpenSelectDate: Boolean = false,
    onDateSelected: (Long?) -> Unit,
    onClear: () -> Unit = {},
    onTodayClicked: () -> Unit,
    onTomorrowClicked: () -> Unit,
    openSelectedDate: (Long) -> Unit = {},
    onTimeSelected: (hour: Int, minute: Int) -> Unit = { _, _ -> }
) {

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.timeInMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return state.isEditable
            }
        },
        yearRange = DATE_PICKER_YEAR_RANGE
    )

    val isFirstLoad = remember { mutableStateOf(true) }

    LaunchedEffect(datePickerState.selectedDateMillis) {
        if (!isFirstLoad.value) {
            onDateSelected(datePickerState.selectedDateMillis)
        } else {
            isFirstLoad.value = false
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 32.dp)
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
    ) {
        CalendarDragger()

        if (showHeader) {
            Header(state = state, onClear = onClear)
        }

        val todayContentColor = if (state.isEditable) {
            colorResource(id = R.color.glyph_accent)
        } else {
            colorResource(id = R.color.glyph_inactive)
        }

        val navigationContentColor = if (state.isEditable) {
            colorResource(id = R.color.glyph_accent)
        } else {
            colorResource(id = R.color.glyph_inactive)
        }

        val datePickerColors = DatePickerDefaults.colors(
            dividerColor = colorResource(id = R.color.shape_primary),
            containerColor = colorResource(id = R.color.background_secondary),
            yearContentColor = colorResource(id = R.color.text_primary),
            dayContentColor = colorResource(id = R.color.text_primary),
            weekdayContentColor = colorResource(id = R.color.text_tertiary),
            todayContentColor = todayContentColor,
            todayDateBorderColor = Color.Transparent,
            selectedDayContainerColor = colorResource(id = R.color.date_selected_container_color),
            selectedDayContentColor = colorResource(id = R.color.glyph_accent),
            disabledDayContentColor = colorResource(id = R.color.text_primary),
            disabledSelectedDayContentColor = colorResource(id = R.color.text_primary),
            disabledSelectedDayContainerColor = colorResource(id = R.color.date_selected_inactive_color),
            navigationContentColor = navigationContentColor
        )
        //https://issuetracker.google.com/issues/281859606
        DatePicker(
            state = datePickerState,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            title = null,
            headline = null,
            showModeToggle = false,
            colors = datePickerColors,
        )
        if (state.includeTime) {
            TimeRow(state = state, onTimeSelected = onTimeSelected)
        }
        if (state.isEditable) {
            CalendarShortcuts(
                showOpenSelectDate = showOpenSelectDate,
                datePickerState = datePickerState,
                onTodayClicked = onTodayClicked,
                onTomorrowClicked = onTomorrowClicked,
                openSelectedDate = openSelectedDate
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.CalendarShortcuts(
    showOpenSelectDate: Boolean,
    datePickerState: DatePickerState,
    onTodayClicked: () -> Unit,
    onTomorrowClicked: () -> Unit,
    openSelectedDate: (Long) -> Unit
) {
    Divider(paddingStart = 16.dp, paddingEnd = 0.dp)
    Text(
        modifier = Modifier
            .height(44.dp)
            .fillMaxWidth()
            .padding(start = 16.dp, top = 11.dp, bottom = 11.dp)
            .noRippleClickable { onTodayClicked() },
        color = colorResource(id = R.color.text_primary),
        text = stringResource(id = R.string.today),
        style = UXBody
    )
    Divider(paddingStart = 16.dp, paddingEnd = 0.dp)
    Text(
        modifier = Modifier
            .height(44.dp)
            .fillMaxWidth()
            .padding(start = 16.dp, top = 11.dp, bottom = 11.dp)
            .noRippleClickable { onTomorrowClicked() },
        color = colorResource(id = R.color.text_primary),
        text = stringResource(id = R.string.tomorrow),
        style = UXBody
    )
    if (datePickerState.selectedDateMillis != null && showOpenSelectDate) {
        Divider(paddingStart = 16.dp, paddingEnd = 0.dp)
        Row(
            modifier = Modifier
                .height(44.dp)
                .noRippleThrottledClickable {
                    val timeInMillis = datePickerState.selectedDateMillis
                    if (timeInMillis != null) {
                        openSelectedDate(timeInMillis)
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 16.dp),
                color = colorResource(id = R.color.text_primary),
                text = stringResource(id = R.string.open_selected_date),
                style = UXBody
            )
            Image(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(end = 16.dp),
                painter = painterResource(id = R.drawable.ic_arrow_forward),
                contentDescription = "Open selected date"
            )
        }
        Divider(paddingStart = 16.dp, paddingEnd = 0.dp)
    }
}

@Composable
private fun TimeRow(
    state: DateValueView,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val isDialogVisible = remember { mutableStateOf(false) }
    val hour = state.hour
    val minute = state.minute
    Divider(paddingStart = 16.dp, paddingEnd = 0.dp)
    Row(
        modifier = Modifier
            .height(44.dp)
            .fillMaxWidth()
            .noRippleClickable {
                if (state.isEditable) isDialogVisible.value = true
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            color = colorResource(id = R.color.text_primary),
            text = stringResource(id = R.string.date_picker_time),
            style = UXBody
        )
        if (hour != null && minute != null) {
            Text(
                modifier = Modifier.padding(end = 16.dp),
                color = colorResource(id = R.color.text_secondary),
                text = formatTime(hour = hour, minute = minute, is24Hour = is24Hour),
                style = UXBody
            )
        }
    }
    if (isDialogVisible.value) {
        TimePickerDialog(
            initialHour = hour ?: 0,
            initialMinute = minute ?: 0,
            is24Hour = is24Hour,
            onDismiss = { isDialogVisible.value = false },
            onConfirm = { selectedHour, selectedMinute ->
                isDialogVisible.value = false
                onTimeSelected(selectedHour, selectedMinute)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    is24Hour: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
    )
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(
                    color = colorResource(id = R.color.background_secondary),
                    shape = RoundedCornerShape(16.dp)
                )
                .verticalScroll(rememberScrollState())
                .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = colorResource(id = R.color.shape_tertiary),
                    clockDialSelectedContentColor = colorResource(id = R.color.text_white),
                    clockDialUnselectedContentColor = colorResource(id = R.color.text_primary),
                    selectorColor = colorResource(id = R.color.glyph_accent),
                    containerColor = colorResource(id = R.color.background_secondary),
                    periodSelectorBorderColor = colorResource(id = R.color.shape_primary),
                    periodSelectorSelectedContainerColor = colorResource(id = R.color.date_selected_container_color),
                    periodSelectorUnselectedContainerColor = Color.Transparent,
                    periodSelectorSelectedContentColor = colorResource(id = R.color.glyph_accent),
                    periodSelectorUnselectedContentColor = colorResource(id = R.color.text_primary),
                    timeSelectorSelectedContainerColor = colorResource(id = R.color.date_selected_container_color),
                    timeSelectorUnselectedContainerColor = colorResource(id = R.color.shape_tertiary),
                    timeSelectorSelectedContentColor = colorResource(id = R.color.glyph_accent),
                    timeSelectorUnselectedContentColor = colorResource(id = R.color.text_primary)
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(16.dp),
                    color = colorResource(id = R.color.text_secondary),
                    text = stringResource(id = R.string.cancel),
                    style = UXBody
                )
                Text(
                    modifier = Modifier
                        .clickable {
                            onConfirm(timePickerState.hour, timePickerState.minute)
                        }
                        .padding(16.dp),
                    color = colorResource(id = R.color.glyph_accent),
                    text = stringResource(id = R.string.done),
                    style = UXBody
                )
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int, is24Hour: Boolean): String {
    val pattern = if (is24Hour) "HH:mm" else "h:mm a"
    return LocalTime.of(hour, minute).format(
        DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    )
}

@Composable
private fun CalendarDragger(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Dragger(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun Header(state: DateValueView, onClear: () -> Unit) {
    // Main content box
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (state.isEditable) {
            // Left-aligned CLEAR button
            Box(modifier = Modifier
                .wrapContentWidth()
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .clickable { onClear() }
            ) {
                androidx.compose.material.Text(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp, end = 16.dp),
                    text = stringResource(id = R.string.clear),
                    style = UXBody,
                    color = colorResource(R.color.glyph_active),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Centered, ellipsized RELATION name
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 74.dp),
            text = state.title.orEmpty(),
            style = Title1.copy(),
            color = colorResource(R.color.text_primary),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@DefaultPreviews
@Composable
private fun CalendarShortcutsPreview() {
    Column {
        CalendarShortcuts(
            showOpenSelectDate = true,
            onTodayClicked = {},
            onTomorrowClicked = {},
            openSelectedDate = {},
            datePickerState = rememberDatePickerState()
        )
    }
}