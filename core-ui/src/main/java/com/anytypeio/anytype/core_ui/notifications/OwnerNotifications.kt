package com.anytypeio.anytype.core_ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular

@Composable
fun OwnerUserRequestToJoin(
    name: String,
    spaceName: String,
    onManageClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.background_primary),
//                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .noRippleClickable { onManageClicked() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = Color.Red,
                    shape = CircleShape
                )
        )
        val placeholder = stringResource(id = R.string.untitled)
        Text(
            text = stringResource(
                id = R.string.multiplayer_notification_member_user_sends_join_request,
                name.ifEmpty { placeholder },
                spaceName.ifEmpty { placeholder }
            ),
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
            color = colorResource(id = R.color.text_secondary),
            style = Caption1Regular
        )
        Text(
            text = stringResource(id = R.string.multiplayer_notification_view_request),
            modifier = Modifier
                .padding(start = 12.dp),
            color = colorResource(id = R.color.text_primary),
            style = Caption1Medium
        )
    }
}

@Composable
fun OwnerUserRequestToLeave(
    name: String,
    spaceName: String,
    onManageClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.background_primary),
//                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .noRippleClickable { onManageClicked() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = Color.Red,
                    shape = CircleShape
                )
        )
        val placeholder = stringResource(id = R.string.untitled)
        Text(
            text = stringResource(
                id = R.string.multiplayer_notification_member_user_sends_leave_request,
                name.ifEmpty { placeholder },
                spaceName.ifEmpty { placeholder }
            ),
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
            color = colorResource(id = R.color.text_secondary),
            style = Caption1Regular
        )
        Text(
            text = stringResource(id = R.string.multiplayer_notification_view_request),
            modifier = Modifier
                .padding(start = 12.dp),
            color = colorResource(id = R.color.text_primary),
            style = Caption1Medium
        )
    }
}

//@Composable
//fun NewJoinRequestNotification(
//    onManageClicked: () -> Unit
//) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(
//                color = colorResource(id = R.color.background_notification_primary),
//                shape = RoundedCornerShape(8.dp)
//            )
//            .padding(horizontal = 20.dp, vertical = 16.dp)
//            .noRippleClickable { onManageClicked() }
//    ) {
//        Text(
//            text = stringResource(id = R.string.multiplayer_notification_new_join_request),
//            modifier = Modifier.align(Alignment.CenterStart),
//            color = colorResource(id = R.color.text_secondary),
//            style = Caption1Regular
//
//        )
//        Text(
//            text = stringResource(id = R.string.multiplayer_manage),
//            modifier = Modifier.align(Alignment.CenterEnd),
//            color = colorResource(id = R.color.text_white),
//            style = Caption1Medium
//        )
//    }
//}
//
//@Composable
//@Preview
//private fun NewJoinRequestNotificationPreview() {
//    NewJoinRequestNotification(
//        onManageClicked = {}
//    )
//}

@Composable
@Preview
private fun OwnerUserRequestToJoinPreview() {
    OwnerUserRequestToJoin(
        name = "Carl Einstein",
        spaceName = "Art historians",
        onManageClicked = {}
    )
}

@Composable
@Preview
private fun OwnerUserRequestToLeavePreview() {
    OwnerUserRequestToLeave(
        name = "Aby Warburg",
        spaceName = "Art historians",
        onManageClicked = {}
    )
}