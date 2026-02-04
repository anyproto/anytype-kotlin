package com.anytypeio.anytype.feature_create_object.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectAction
import com.anytypeio.anytype.feature_create_object.presentation.NewCreateObjectState

/**
 * Bottom sheet variant of the create object screen.
 * Uses Material3 ModalBottomSheet for a full-screen modal appearance.
 * Typically used in contexts like widgets or object creation from the main screen.
 *
 * @param visible Whether the bottom sheet is currently visible
 * @param onDismissRequest Callback when the bottom sheet should be dismissed
 * @param state The current UI state
 * @param onAction Callback for handling user actions
 * @param modifier Optional modifier for the bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateObjectBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    state: NewCreateObjectState,
    onAction: (CreateObjectAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (visible) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = false
        )

        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = colorResource(id = R.color.background_secondary),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = {
                // Custom drag handle matching the design
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(5.dp)
                            .background(
                                color = colorResource(id = R.color.shape_primary),
                                shape = RoundedCornerShape(2.5.dp)
                            )
                    )
                }
            },
            modifier = modifier
        ) {
            CreateObjectContent(
                state = state,
                onAction = onAction
            )
        }
    }
}
