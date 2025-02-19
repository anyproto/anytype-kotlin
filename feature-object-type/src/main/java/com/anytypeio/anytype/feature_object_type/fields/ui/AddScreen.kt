package com.anytypeio.anytype.feature_object_type.fields.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.dv.DragHandle
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent
import com.anytypeio.anytype.feature_object_type.fields.UiAddFieldScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFieldScreen(
    state: UiAddFieldScreenState,
    fieldEvent: (FieldEvent) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (state is UiAddFieldScreenState.Visible) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxWidth(),
            dragHandle = { DragHandle() },
            scrimColor = colorResource(id = R.color.modal_screen_outside_background),
            containerColor = colorResource(id = R.color.background_primary),
            shape = RoundedCornerShape(16.dp),
            sheetState = bottomSheetState,
            onDismissRequest = {

            },
        ) {

        }
    }
}

@Composable
private fun ColumnScope.AddFieldContainer(
    state: UiAddFieldScreenState.Visible,
    fieldEvent: (FieldEvent) -> Unit
) {

}