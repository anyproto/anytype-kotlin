package com.anytypeio.anytype.feature_vault.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.anytypeio.anytype.core_ui.foundation.Prompt
import com.anytypeio.anytype.feature_vault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertScreenModals(
    title: String,
    description: String,
    firstButtonText: String,
    icon: Int = R.drawable.ic_popup_alert_56,
    secondButtonText: String = stringResource(id = R.string.cancel),
    onAction: () -> Unit = { },
    onDismiss: () -> Unit = { },
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ModalBottomSheet(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        onDismissRequest = onDismiss,
        dragHandle = null,
        containerColor = colorResource(id = R.color.background_secondary),
        sheetState = bottomSheetState
    ) {
        Prompt(
            showDragger = false,
            title = title,
            icon = icon,
            description = description,
            primaryButtonText = firstButtonText,
            secondaryButtonText = secondButtonText,
            onPrimaryButtonClicked = onAction,
            onSecondaryButtonClicked = onDismiss
        )
    }
}
