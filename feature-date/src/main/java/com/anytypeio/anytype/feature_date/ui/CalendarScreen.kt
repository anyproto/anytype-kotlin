package com.anytypeio.anytype.feature_date.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.relations.DatePickerContent
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.viewmodel.UiCalendarState
import com.anytypeio.anytype.feature_date.ui.models.DateEvent
import com.anytypeio.anytype.presentation.sets.DateValueView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    uiState: UiCalendarState,
    onDateEvent: (DateEvent) -> Unit
) {

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ModalBottomSheet(
        dragHandle = null,
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetState = bottomSheetState,
        onDismissRequest = { onDateEvent(DateEvent.Calendar.OnCalendarDismiss) },
        content = {
            when (uiState) {
                is UiCalendarState.Calendar -> {
                    DatePickerContent(
                        state = DateValueView(timeInMillis = uiState.timeInMillis),
                        onDateSelected = { onDateEvent(DateEvent.Calendar.OnCalendarDateSelected(it)) },
                        onTodayClicked = { onDateEvent(DateEvent.Calendar.OnTodayClick) },
                        onTomorrowClicked = { onDateEvent(DateEvent.Calendar.OnTomorrowClick) },
                    )
                }

                UiCalendarState.Hidden -> {}
            }
        },
    )
}