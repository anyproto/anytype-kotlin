package com.anytypeio.anytype.ui_settings.space.new_settings

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.BUTTON_PRIMARY
import com.anytypeio.anytype.core_ui.foundation.BUTTON_SECONDARY
import com.anytypeio.anytype.core_ui.foundation.GenericAlert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeTypeConfirmationDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        GenericAlert(
            onFirstButtonClicked = onCancel,
            onSecondButtonClicked = onConfirm,
            config = AlertConfig.WithTwoButtons(
                title = stringResource(id = R.string.alert_change_space_type_title),
                description = stringResource(R.string.alert_change_space_type_subtitle),
                firstButtonText = stringResource(R.string.cancel),
                secondButtonText = stringResource(R.string.alert_change_space_type_button),
                firstButtonType = BUTTON_SECONDARY,
                secondButtonType = BUTTON_PRIMARY,
                icon = R.drawable.ic_popup_question_56,
                isSecondButtonLoading = false,
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@DefaultPreviews
@Composable
fun ChangeTypeConfirmationDialogPreview() {
    ChangeTypeConfirmationDialog(
        onConfirm = {},
        onCancel = {},
        onDismiss = {}
    )
}
