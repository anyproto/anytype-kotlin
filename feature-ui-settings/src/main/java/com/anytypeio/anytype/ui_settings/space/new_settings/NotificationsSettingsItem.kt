package com.anytypeio.anytype.ui_settings.space.new_settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsItem.Notifications.NotificationState

@Composable
fun NotificationsSettingsItem(
    state: NotificationState,
    onClick: () -> Unit
) {
    val (iconRes, endText) = when (state) {
        NotificationState.ALL -> R.drawable.ic_bell_24 to stringResource(R.string.notifications_all)
        NotificationState.MENTIONS -> R.drawable.ic_bell_24 to stringResource(R.string.notifications_mentions)
        NotificationState.DISABLE -> R.drawable.ic_bell_24 to stringResource(R.string.notifications_disable)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.notifications_title),
            style = PreviewTitle1Regular,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = endText,
            style = BodyRegular,
            color = colorResource(id = R.color.text_secondary),
        )
        Image(
            painter = painterResource(id = R.drawable.ic_arrow_forward_24),
            contentDescription = "Arrow forward"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsPreferenceSheet(
    currentState: NotificationState,
    onSelect: (NotificationState) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.padding(top = 48.dp),
        containerColor = colorResource(R.color.background_secondary),
        onDismissRequest = onDismiss,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Dragger(
                modifier = Modifier.padding(vertical = 6.dp)
            )
            Text(
                text = stringResource(R.string.notifications_title),
                style = Title1,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            NotificationOption(
                title = stringResource(R.string.notifications_all),
                checked = currentState == NotificationState.ALL,
                onClick = { onSelect(NotificationState.ALL) }
            )
            NotificationOption(
                title = stringResource(R.string.notifications_mentions),
                checked = currentState == NotificationState.MENTIONS,
                onClick = { onSelect(NotificationState.MENTIONS) }
            )
            NotificationOption(
                title = stringResource(R.string.notifications_disable),
                checked = currentState == NotificationState.DISABLE,
                onClick = { onSelect(NotificationState.DISABLE) }
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