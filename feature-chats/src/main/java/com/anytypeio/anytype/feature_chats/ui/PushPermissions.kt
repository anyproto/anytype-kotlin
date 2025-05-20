package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.feature_chats.R

@Composable
fun NotificationPermissionContent(
    onEnableNotifications: () -> Unit,
    onCancelClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorResource(id = R.color.widget_background)),
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .height(232.dp),
            painter = painterResource(id = R.drawable.push_modal_illustration),
            contentDescription = "Push notifications illustration",
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.notifications_modal_title),
            style = HeadlineHeading,
            textAlign = TextAlign.Center,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.notifications_modal_description),
            style = Title2,
            textAlign = TextAlign.Center,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        ButtonPrimary(
            text = stringResource(R.string.notifications_modal_success_button),
            onClick = onEnableNotifications,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            size = ButtonSize.Large,
        )
        Spacer(modifier = Modifier.height(10.dp))
        ButtonSecondary(
            text = stringResource(id = R.string.notifications_modal_cancel_button),
            onClick = onCancelClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            size = ButtonSize.Large
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@DefaultPreviews
@Composable
fun NotificationPermissionRequestDialogPreview() {
    NotificationPermissionContent(
        onEnableNotifications = {},
        onCancelClicked = {}
    )
}