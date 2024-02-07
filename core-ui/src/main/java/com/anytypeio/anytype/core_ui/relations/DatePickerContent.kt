package com.anytypeio.anytype.core_ui.relations

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerContent(state: DateValueView) {

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.timeInSeconds,
        initialDisplayedMonthMillis = state.timeInSeconds
    )
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
        Header(title = state.title.orEmpty())
        val datePickerColors = DatePickerDefaults.colors(
            todayContentColor = colorResource(id = R.color.glyph_accent),
            selectedDayContainerColor = colorResource(id = R.color.palette_very_light_orange),
            selectedDayContentColor = colorResource(id = R.color.glyph_accent),
            subheadContentColor = colorResource(id = R.color.amp_blue),
            headlineContentColor = colorResource(id = R.color.palette_system_green),
        )
        CompositionLocalProvider(LocalContentColor provides colorResource(id = R.color.glyph_accent)) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                title = null,
                headline = null,
                showModeToggle = false,
                colors = datePickerColors,
                dateFormatter = DatePickerFormatter(
                    selectedDateSkeleton = "MMMM d, yyyy"
                )
            )
        }
        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
        Text(
            color = colorResource(id = R.color.text_primary),
            text = stringResource(id = R.string.today),
            style = UXBody,
            modifier = Modifier.padding(start = 16.dp, top = 11.dp, bottom = 11.dp)
        )
        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
        Text(
            color = colorResource(id = R.color.text_primary),
            text = stringResource(id = R.string.tomorrow),
            style = UXBody,
            modifier = Modifier.padding(start = 16.dp, top = 11.dp, bottom = 11.dp)
        )
    }
}

@Composable
private fun Header(title : String) {

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
        if (true) {
            // Left-aligned CLEAR button
            Box(modifier = Modifier
                .wrapContentWidth()
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .noRippleClickable { }
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
            //text = getTitle(state = state),
            text = title,
            style = Title1.copy(),
            color = colorResource(R.color.text_primary),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}