package com.anytypeio.anytype.core_ui.features.editor

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.relations.DatePickerContent
import com.anytypeio.anytype.presentation.editor.model.EditorDatePickerState
import com.anytypeio.anytype.presentation.editor.model.EditorDatePickerState.Visible
import com.anytypeio.anytype.presentation.editor.model.OnEditorDatePickerEvent
import com.anytypeio.anytype.presentation.editor.model.OnEditorDatePickerEvent.OnDateSelected
import com.anytypeio.anytype.presentation.editor.model.OnEditorDatePickerEvent.OnTodayClick
import com.anytypeio.anytype.presentation.editor.model.OnEditorDatePickerEvent.OnTomorrowClick
import com.anytypeio.anytype.presentation.sets.DateValueView


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorDatePicker(
    modifier: Modifier = Modifier,
    uiState: EditorDatePickerState,
    onEvent: (OnEditorDatePickerEvent) -> Unit
) {

    if (uiState !is Visible) return

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(uiState) {
        keyboardController?.hide()
    }

    ModalBottomSheet(
        modifier = modifier,
        dragHandle = null,
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetState = bottomSheetState,
        onDismissRequest = { onEvent(OnEditorDatePickerEvent.OnDatePickerDismiss) },
        content = {
            DatePickerContent(
                state = DateValueView(timeInMillis = null),
                showHeader = false,
                onDateSelected = { onEvent(getDateSelectedEvent(uiState, it)) },
                onTodayClicked = { onEvent(getTodayEvent(uiState)) },
                onTomorrowClicked = { onEvent(getTomorrowEvent(uiState)) }
            )
        },
    )
}

private fun getTodayEvent(uiState: Visible): OnTodayClick {
    return when (uiState) {
        is Visible.Link -> OnTodayClick.Link(uiState.targetId)
        is Visible.Mention -> OnTodayClick.Mention(uiState.targetId)
    }
}

private fun getTomorrowEvent(uiState: Visible): OnTomorrowClick {
    return when (uiState) {
        is Visible.Link -> OnTomorrowClick.Link(uiState.targetId)
        is Visible.Mention -> OnTomorrowClick.Mention(uiState.targetId)
    }
}

private fun getDateSelectedEvent(
    uiState: Visible,
    timeInMillis: Long?
): OnDateSelected {
    return when (uiState) {
        is Visible.Link -> OnDateSelected.Link(
            timeInMillis = timeInMillis,
            targetId = uiState.targetId
        )
        is Visible.Mention -> OnDateSelected.Mention(
            timeInMillis = timeInMillis,
            targetId = uiState.targetId
        )
    }
}