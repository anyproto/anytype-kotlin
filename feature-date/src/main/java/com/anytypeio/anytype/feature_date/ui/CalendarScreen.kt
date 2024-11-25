package com.anytypeio.anytype.feature_date.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.relations.DatePickerContent
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.models.UiCalendarState
import com.anytypeio.anytype.presentation.sets.DateValueView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onDismiss: () -> Unit,
    uiCalendarState: UiCalendarState,
    onCalendarDateSelected: (Long?) -> Unit,
    onTodayClicked: () -> Unit,
    onTomorrowClicked: () -> Unit
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
        onDismissRequest = onDismiss,
        content = {
            when (uiCalendarState) {
                is UiCalendarState.Calendar -> {
                    DatePickerContent(
                        state = DateValueView(
                            timeInMillis = uiCalendarState.timeInMillis
                        ),
                        onDateSelected = {
                            onDismiss()
                            onCalendarDateSelected(it)
                        },
                        showHeader = false,
                        onClear = {},
                        onTodayClicked = {
                            onDismiss()
                            onTodayClicked()
                        },
                        onTomorrowClicked = {
                            onDismiss()
                            onTomorrowClicked()
                        }
                    )
                }

                UiCalendarState.Empty -> {}
            }
        },
    )
}