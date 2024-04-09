package com.anytypeio.anytype.core_ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ImportErrorCode
import com.anytypeio.anytype.core_models.ImportErrorCode.BAD_INPUT
import com.anytypeio.anytype.core_models.ImportErrorCode.FILE_LOAD_ERROR
import com.anytypeio.anytype.core_models.ImportErrorCode.IMPORT_IS_CANCELED
import com.anytypeio.anytype.core_models.ImportErrorCode.INSUFFICIENT_PERMISSIONS
import com.anytypeio.anytype.core_models.ImportErrorCode.INTERNAL_ERROR
import com.anytypeio.anytype.core_models.ImportErrorCode.LIMIT_OF_ROWS_OR_RELATIONS_EXCEEDED
import com.anytypeio.anytype.core_models.ImportErrorCode.NO_OBJECTS_TO_IMPORT
import com.anytypeio.anytype.core_models.ImportErrorCode.NULL
import com.anytypeio.anytype.core_models.ImportErrorCode.UNKNOWN_ERROR
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.AlertIcon
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_GREEN
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_RED
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.presentation.notifications.NotificationsScreenState


@Composable
fun NotificationsScreen(
    state: NotificationsScreenState,
    onActionButtonClick: (SpaceId) -> Unit,
    onErrorButtonClick: () -> Unit
) {
    when (state) {
        is NotificationsScreenState.GalleryInstalled -> {
            NotificationGalleryInstall(
                icon = AlertConfig.Icon(
                    gradient = GRADIENT_TYPE_GREEN,
                    icon = R.drawable.ic_alert_install_gallery
                ),
                title = stringResource(id = R.string.gallery_experience_alert_title_success),
                subtitle = stringResource(
                    id = R.string.gallery_experience_alert_subtitle_success,
                    state.galleryName
                ),
                actionButtonText = stringResource(id = R.string.gallery_experience_alert_button_space),
                onButtonClick = { onActionButtonClick(state.spaceId) }
            )
        }

        is NotificationsScreenState.GalleryInstalledError -> {
            NotificationGalleryInstallError(
                icon = AlertConfig.Icon(
                    gradient = GRADIENT_TYPE_RED,
                    icon = R.drawable.ic_alert_error
                ),
                title = stringResource(id = R.string.gallery_experience_alert_title_error),
                subtitle = ImportErrorText(state.errorCode),
                actionButtonText = stringResource(id = R.string.gallery_experience_alert_button_error),
                onButtonClick = onErrorButtonClick
            )
        }
        is NotificationsScreenState.Multiplayer.RequestToJoin -> {
            OwnerUserRequestToJoin(
                name = state.name,
                spaceName = state.space.id,
                onManageClicked = {}
            )
        }
        is NotificationsScreenState.Multiplayer.RequestToLeave -> {
            OwnerUserRequestToLeave(
                name = state.name,
                spaceName = state.space.id,
                onManageClicked = {}
            )
        }
        is NotificationsScreenState.Multiplayer.MemberRequestApproved -> {
            MemberRequestApprovedWithAccessRightsNotification(
                spaceName = state.spaceName,
                isReadOnly = state.isReadOnly
            )
        }
        NotificationsScreenState.Hidden -> {}
    }
}

@Composable
private fun ImportErrorText(error: ImportErrorCode): String {
    return when (error) {
        NULL -> ""
        UNKNOWN_ERROR -> stringResource(id = R.string.notifications_alert_error_unknown)
        BAD_INPUT -> stringResource(id = R.string.notifications_alert_error_bad_input)
        INTERNAL_ERROR -> stringResource(id = R.string.notifications_alert_error_internal)
        NO_OBJECTS_TO_IMPORT -> stringResource(id = R.string.notifications_alert_error_no_objects_to_import)
        IMPORT_IS_CANCELED -> stringResource(id = R.string.notifications_alert_error_import_canceled)
        LIMIT_OF_ROWS_OR_RELATIONS_EXCEEDED -> stringResource(id = R.string.notifications_alert_error_limit_exceeded)
        FILE_LOAD_ERROR -> stringResource(id = R.string.notifications_alert_error_file_load)
        INSUFFICIENT_PERMISSIONS -> stringResource(id = R.string.notifications_alert_error_insufficient_permissions)
    }
}


@Composable
private fun NotificationGalleryInstall(
    icon: AlertConfig.Icon,
    title: String,
    subtitle: String,
    actionButtonText: String,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, bottom = 32.dp)
            .fillMaxWidth()
            .background(
                shape = RoundedCornerShape(8.dp),
                color = colorResource(R.color.background_secondary)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))
        AlertIcon(icon = icon)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = HeadlineSubheading,
            color = colorResource(R.color.text_primary)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = BodyCalloutRegular,
            color = colorResource(R.color.text_primary),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        ButtonPrimary(
            onClick = onButtonClick,
            size = ButtonSize.LargeSecondary,
            text = actionButtonText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun NotificationGalleryInstallError(
    icon: AlertConfig.Icon,
    title: String,
    subtitle: String,
    actionButtonText: String,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, bottom = 32.dp)
            .fillMaxWidth()
            .background(
                shape = RoundedCornerShape(8.dp),
                color = colorResource(R.color.background_secondary)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))
        AlertIcon(icon = icon)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = HeadlineSubheading,
            color = colorResource(R.color.text_primary)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = BodyCalloutRegular,
            color = colorResource(R.color.text_primary),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        ButtonSecondary(
            onClick = onButtonClick,
            size = ButtonSize.LargeSecondary,
            text = actionButtonText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview
@Composable
fun NotificationsScreenPreview() {
    NotificationsScreen(
        state = NotificationsScreenState.GalleryInstalled(
            spaceId = SpaceId("spaceId"),
            galleryName = "Strategic Writing"
        ),
        onActionButtonClick = {},
        onErrorButtonClick = {}
    )
}

@Preview
@Composable
fun NotificationsScreenPreviewError() {
    NotificationsScreen(
        state = NotificationsScreenState.GalleryInstalledError(IMPORT_IS_CANCELED),
        onActionButtonClick = {},
        onErrorButtonClick = {}
    )
}

@Composable
fun UserJoinedSpaceWithAccessRightsNotification(
    name: String,
    spaceName: String,
    onManageClicked: () -> Unit,
    isReadOnly: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.background_notification_primary),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .noRippleClickable { onManageClicked() }
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
        val msg = if (isReadOnly)
            stringResource(
                id = R.string.multiplayer_notification_member_joined_space_with_read_only_rights,
                name.ifEmpty { placeholder },
                spaceName.ifEmpty { placeholder }
            )
        else
            stringResource(
                id = R.string.multiplayer_notification_member_joined_space_with_read_only_rights,
                name.ifEmpty { placeholder },
                spaceName.ifEmpty { placeholder }
            )
        Text(
            text = msg,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 44.dp),
            color = colorResource(id = R.color.text_secondary),
            style = Caption1Regular
        )
    }
}

@Composable
@Preview
private fun UserJoinedSpaceWithAccessRightsNotificationPreview() {
    UserJoinedSpaceWithAccessRightsNotification(
        name = "Carl Einstein",
        spaceName = "Art historians",
        isReadOnly = true,
        onManageClicked = {}
    )
}