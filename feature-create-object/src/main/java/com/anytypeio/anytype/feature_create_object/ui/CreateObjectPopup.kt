package com.anytypeio.anytype.feature_create_object.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import com.anytypeio.anytype.core_ui.menu.StyledDropdownMenu
import com.anytypeio.anytype.core_ui.menu.StyledDropdownMenuDefaults
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectAction
import com.anytypeio.anytype.feature_create_object.presentation.NewCreateObjectState

/**
 * Popup variant of the create object screen.
 * Uses DropdownMenu for a compact, floating menu appearance.
 * Typically used in contexts like the chat attachment menu.
 *
 * @param expanded Whether the popup is currently visible
 * @param onDismissRequest Callback when the popup should be dismissed
 * @param state The current UI state
 * @param onAction Callback for handling user actions
 * @param modifier Optional modifier for the popup
 * @param offset Offset for positioning the popup relative to its anchor
 */
@Composable
fun CreateObjectPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    state: NewCreateObjectState,
    onAction: (CreateObjectAction) -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = StyledDropdownMenuDefaults.DefaultOffset
) {
    StyledDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset
    ) {
        CreateObjectContent(
            state = state,
            onAction = { action ->
                // Dismiss the popup before handling the action
                onDismissRequest()
                onAction(action)
            }
        )
    }
}
