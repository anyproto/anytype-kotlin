package com.anytypeio.anytype.feature_create_object.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_ui.menu.StyledDropdownMenu
import com.anytypeio.anytype.core_ui.menu.StyledDropdownMenuDefaults
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectAction
import com.anytypeio.anytype.feature_create_object.presentation.NewCreateObjectState
import com.anytypeio.anytype.feature_create_object.presentation.ObjectTypeItem

/**
 * Popup variant of the create object screen.
 * Uses DropdownMenu for a compact, floating menu appearance.
 * Typically used in contexts like the chat attachment menu and the
 * Home / Widgets global create FAB.
 *
 * Dismissal is controlled by the host — this composable does not
 * auto-dismiss on action, so interactive actions like search updates
 * keep the popup open.
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
    offset: DpOffset = StyledDropdownMenuDefaults.DefaultOffset,
    onBack: (() -> Unit)? = null
) {
    StyledDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
        focusable = true
    ) {
        CreateObjectContent(
            state = state,
            onAction = onAction,
            onBack = onBack
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewCreateObjectPopup() {
    // First entry intentionally uses a very long name to exercise the
    // ellipsis path in ObjectTypeMenuItem (maxLines = 1).
    val sampleTypes = listOf(
        ObjectTypeItem(
            typeKey = "ot-page",
            name = "Page dlskflfjkjsdklfjklasjdkfjsjfiljefjiejijeljslfjilasdjflijialsjfiljas",
            icon = ObjectIcon.None
        ),
        ObjectTypeItem(
            typeKey = "ot-note",
            name = "Note",
            icon = ObjectIcon.None
        )
    )
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            CreateObjectPopup(
                expanded = true,
                onDismissRequest = {},
                state = NewCreateObjectState(
                    objectTypes = sampleTypes,
                    filteredObjectTypes = sampleTypes,
                    searchQuery = "",
                    isLoading = false,
                    error = null,
                    showMediaSection = true,
                    showAttachExisting = true
                ),
                onAction = {}
            )
        }
    }
}

