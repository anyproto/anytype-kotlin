package com.anytypeio.anytype.ui_settings.space.new_settings

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.presentation.spaces.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsPreferenceSheet(
    currentState: NotificationState,
    uiEvent: (UiEvent) -> Unit,
    onDismiss: () -> Unit
) {
    val contentModifier = if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
        Modifier
            .windowInsetsPadding(WindowInsets.systemBars)
            .fillMaxSize()
    } else {
        Modifier
            .fillMaxSize()
    }
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = contentModifier,
        containerColor = colorResource(R.color.background_secondary),
        onDismissRequest = onDismiss,
        dragHandle = {},
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Dragger(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 6.dp)
            )
            Text(
                text = stringResource(R.string.notifications_title),
                style = Title1,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
            NotificationOption(
                title = stringResource(R.string.notifications_all),
                checked = currentState == NotificationState.ALL,
                onClick = { uiEvent(UiEvent.OnNotificationsSetting.All) }
            )
            Divider(
                paddingStart = 16.dp,
                paddingEnd = 16.dp,
            )
            NotificationOption(
                title = stringResource(R.string.notifications_mentions),
                checked = currentState == NotificationState.MENTIONS,
                onClick = { uiEvent(UiEvent.OnNotificationsSetting.Mentions) }
            )
            Divider(
                paddingStart = 16.dp,
                paddingEnd = 16.dp,
            )
            NotificationOption(
                title = stringResource(R.string.notifications_disable),
                checked = currentState == NotificationState.DISABLE,
                onClick = { uiEvent(UiEvent.OnNotificationsSetting.None) }
            )
        }
    }
}

@Composable
fun NotificationOption(
    title: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = BodyRegular,
            modifier = Modifier.weight(1f)
        )
        if (checked) {
            Image(
                painter = painterResource(id = R.drawable.ic_check_16),
                contentDescription = null,
            )
        }
    }
}

@DefaultPreviews
@Composable
fun NotificationsPreferenceSheetPreview() {
    NotificationsPreferenceSheet(
        currentState = NotificationState.ALL,
        uiEvent = {},
        onDismiss = {}
    )
}