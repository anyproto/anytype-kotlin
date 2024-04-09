package com.anytypeio.anytype.core_ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.Caption1Regular

@Composable
fun MemberRequestApprovedNotification(
    spaceName: String,
    isReadOnly: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
//                color = colorResource(id = R.color.background_notification_primary),
                color = colorResource(id = R.color.background_primary),
//                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        val placeholder = stringResource(id = R.string.untitled)
        val msg = if (isReadOnly)
            stringResource(
                id = R.string.multiplayer_notification_member_request_approved_with_read_only_rights,
                spaceName.ifEmpty { placeholder }
            )
        else
            stringResource(
                id = R.string.multiplayer_notification_member_request_approved_with_read_only_rights,
                spaceName.ifEmpty { placeholder }
            )
        Text(
            text = msg,
            modifier = Modifier
                .align(Alignment.CenterStart),
            color = colorResource(id = R.color.text_secondary),
            style = Caption1Regular,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
@Preview
fun MemberRequestApprovedWithAccessRightsNotificationPreview() {
    MemberRequestApprovedNotification(
        spaceName = "Art historians",
        isReadOnly = true
    )
}