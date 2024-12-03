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
import com.anytypeio.anytype.presentation.editor.model.OnEditorDatePickerEvent
import com.anytypeio.anytype.presentation.sets.DateValueView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorDatePicker(
    modifier: Modifier = Modifier,
    uiState: EditorDatePickerState,
    onEvent: (OnEditorDatePickerEvent) -> Unit
) {

    if (uiState !is EditorDatePickerState.Visible) return

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
                onDateSelected = {
                    onEvent(
                        OnEditorDatePickerEvent.OnDateSelected(
                            timeInMillis = it
                        )
                    )
                },
                onTodayClicked = { onEvent(OnEditorDatePickerEvent.OnTodayClick) },
                onTomorrowClicked = { onEvent(OnEditorDatePickerEvent.OnTomorrowClick) }
            )
        },
    )
}