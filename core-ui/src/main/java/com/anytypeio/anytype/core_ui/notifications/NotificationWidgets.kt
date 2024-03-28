package com.anytypeio.anytype.core_ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.AlertIcon
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_GREEN
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.presentation.notifications.NotificationsScreenState


@Composable
fun NotificationsScreen(state: NotificationsScreenState) {
    when (state) {
        is NotificationsScreenState.GalleryInstalled -> {
            NotificationGalleryInstall(
                icon = AlertConfig.Icon(
                    gradient = GRADIENT_TYPE_GREEN,
                    icon = R.drawable.ic_alert_install_gallery
                ),
                title = "Gallery Installed",
                subtitle = "You can now add images from your gallery to your Anytype notes.",
                actionButtonText = "Got it",
                onButtonClick = {}
            )
        }
        NotificationsScreenState.Hidden -> {

        }
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
        AlertIcon(
            icon = AlertConfig.Icon(
                gradient = GRADIENT_TYPE_GREEN,
                icon = R.drawable.ic_alert_install_gallery
            )
        )
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
            color = colorResource(R.color.text_primary)
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