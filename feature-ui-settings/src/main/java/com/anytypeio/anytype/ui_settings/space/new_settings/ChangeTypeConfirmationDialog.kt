package com.anytypeio.anytype.ui_settings.space.new_settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BaseTwoButtonsDarkThemeAlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeTypeConfirmationDialog(
    selectedType: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    BaseTwoButtonsDarkThemeAlertDialog(
        dialogText = "You are about to change the type of this space to $selectedType. This action may affect how content is organized and displayed.",
        actionButtonText = "Ok",
        dismissButtonText = "Cancel",
        onActionButtonClick = onConfirm,
        onDismissButtonClick = onCancel,
        onDismissRequest = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@DefaultPreviews
@Composable
fun ChangeTypeConfirmationDialogPreview() {
    ChangeTypeConfirmationDialog(
        selectedType = "Chat",
        onConfirm = {},
        onCancel = {},
        onDismiss = {}
    )
}
