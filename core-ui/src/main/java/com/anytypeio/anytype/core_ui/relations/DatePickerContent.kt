package com.anytypeio.anytype.core_ui.relations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.presentation.sets.DateValueView


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerContent(
    state: DateValueView,
    onDateSelected: (Long?) -> Unit,
    onClear: () -> Unit,
    onTodayClicked: () -> Unit,
    onTomorrowClicked: () -> Unit
) {

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.timeInMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return state.isEditable
            }
        }
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
        Header(state = state, onClear = onClear)

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
        if (state.isEditable) {
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 11.dp, bottom = 11.dp)
                    .noRippleClickable { onTodayClicked() },
                color = colorResource(id = R.color.text_primary),
                text = stringResource(id = R.string.today),
                style = UXBody
            )
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 11.dp, bottom = 11.dp)
                    .noRippleClickable { onTomorrowClicked() },
                color = colorResource(id = R.color.text_primary),
                text = stringResource(id = R.string.tomorrow),
                style = UXBody
            )
        }
    }
}

@Composable
private fun Header(state: DateValueView, onClear: () -> Unit) {

    // Dragger at the top, centered
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Dragger(modifier = Modifier.align(Alignment.Center))
    }

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