package com.anytypeio.anytype.ui_settings.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.ui_settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    isDisabled: Boolean = true,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ModalBottomSheet(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars)
            .fillMaxSize(),
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = colorResource(id = R.color.background_secondary),
        dragHandle = null,
        sheetState = sheetState
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        Dragger(modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(R.string.notification_settings_title),
            style = Title1,
            color = colorResource(id = R.color.text_primary)
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (isDisabled) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
                    .background(
                        color = colorResource(id = R.color.transparent_tertiary),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_notifications_56),
                    contentDescription = "Notifications are disabled",
                    modifier = Modifier
                        .size(56.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.notification_settings_disabled),
                    style = BodySemiBold,
                    color = colorResource(id = R.color.text_primary)
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.notification_settings_disabled_subtitle),
                    style = PreviewTitle2Regular,
                    color = colorResource(id = R.color.text_primary),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                ButtonPrimary(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = onOpenSettings,
                    text = stringResource(R.string.notification_settings_button_open),
                    size = ButtonSize.Small
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        val (state, color) = if (isDisabled) {
            stringResource(R.string.notification_settings_state_disabled) to colorResource(R.color.palette_system_red)
        } else {
            stringResource(R.string.notification_settings_state_enabled) to colorResource(R.color.palette_system_green)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp)
                .noRippleThrottledClickable(onClick = onOpenSettings)
        ) {
            Text(
                text = stringResource(R.string.notification_settings_state_title),
                color = colorResource(R.color.text_primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart),
                style = PreviewTitle1Regular
            )
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(10.dp)
                        .background(
                            color = color,
                            shape = CircleShape
                        )
                )
                Text(
                    text = state,
                    color = colorResource(R.color.text_secondary),
                    modifier = Modifier.padding(
                        start = 0.dp
                    ),
                    style = PreviewTitle1Regular
                )
                if (isDisabled) {
                    Image(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(12.dp),
                        painter = painterResource(id = R.drawable.ic_arrow_top_end),
                        contentDescription = "Open notification settings",
                    )
                }
            }
        }
        Divider(paddingStart = 16.dp, paddingEnd = 16.dp)
    }
}

@DefaultPreviews
@Composable
fun NotificationSettingsScreenPreview() {
    NotificationSettingsScreen(
        isDisabled = false,
        onDismiss = {},
        onOpenSettings = {}
    )
}