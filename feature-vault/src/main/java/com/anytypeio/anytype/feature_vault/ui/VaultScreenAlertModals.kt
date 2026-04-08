package com.anytypeio.anytype.feature_vault.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Prompt
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingSecondaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.feature_vault.R
import com.anytypeio.anytype.core_ui.R as CoreR

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedSpaceLimitModal(
    limit: Int,
    onUpgradeClicked: () -> Unit,
    onManageChannelsClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ModalBottomSheet(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .navigationBarsPadding(),
        onDismissRequest = onDismiss,
        dragHandle = null,
        containerColor = Color.Transparent,
        sheetState = bottomSheetState
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = colorResource(id = R.color.background_primary),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Dragger(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 6.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.vault_shared_space_limit_title),
                style = Title1,
                color = colorResource(id = CoreR.color.text_primary),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    id = R.string.vault_shared_space_limit_description,
                    limit
                ),
                style = BodyCalloutRegular,
                color = colorResource(id = CoreR.color.text_primary),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            ButtonOnboardingPrimaryLarge(
                modifierBox = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                size = ButtonSize.Large,
                text = stringResource(id = R.string.multiplayer_upgrade_button),
                onClick = onUpgradeClicked
            )
            Spacer(modifier = Modifier.height(8.dp))
            ButtonOnboardingSecondaryLarge(
                modifierBox = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                size = ButtonSize.Large,
                text = stringResource(id = R.string.vault_manage_channels),
                onClick = onManageChannelsClicked
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@DefaultPreviews
@Composable
private fun SharedSpaceLimitModalPreview() {
    SharedSpaceLimitModal(
        limit = 3,
        onUpgradeClicked = {},
        onManageChannelsClicked = {},
        onDismiss = {}
    )
}
