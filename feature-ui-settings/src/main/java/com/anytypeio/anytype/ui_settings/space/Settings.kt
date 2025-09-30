package com.anytypeio.anytype.ui_settings.space

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonUpgrade
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.views.Title3
import com.anytypeio.anytype.core_ui.views.UxSmallTextMedium
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel
import com.anytypeio.anytype.ui_settings.R

@Composable
fun PrivateSpaceSharing(
    onSharePrivateSpaceClicked: () -> Unit,
    onAddMoreSpacesClicked: () -> Unit,
    shareLimitStateState: SpaceSettingsViewModel.ShareLimitsState
) {
    Column {
        Box(
            modifier = Modifier
                .height(52.dp)
                .fillMaxWidth()
                .noRippleClickable(
                    onClick = throttledClick(
                        onClick = { onSharePrivateSpaceClicked() }
                    )
                )
        ) {
            Text(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .align(Alignment.CenterStart),
                text = stringResource(id = R.string.space_type_private_space),
                color = if (shareLimitStateState.shareLimitReached)
                    colorResource(id = R.color.text_secondary)
                else
                    colorResource(id = R.color.text_primary),
                style = BodyRegular
            )
            Row(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = stringResource(id = R.string.multiplayer_share),
                    color = colorResource(id = R.color.text_secondary),
                    style = BodyRegular
                )
                Spacer(Modifier.width(10.dp))
                Image(
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    contentDescription = "Arrow forward",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 20.dp)
                )
            }
        }
        if (shareLimitStateState.shareLimitReached) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp),
                text = stringResource(
                    id = R.string.membership_space_settings_share_limit,
                    shareLimitStateState.sharedSpacesLimit
                ),
                color = colorResource(id = R.color.text_primary),
                style = Caption1Regular
            )
            ButtonUpgrade(
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp)
                    .height(32.dp),
                onClick = { onAddMoreSpacesClicked() },
                text = stringResource(id = R.string.multiplayer_upgrade_spaces_button)
            )
        }
    }
}

@Composable
fun SharedSpaceSharing(
    onManageSharedSpaceClicked: () -> Unit,
    isUserOwner: Boolean,
    requests: Int = 0
) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
            .noRippleClickable(
                onClick = throttledClick(
                    onClick = { onManageSharedSpaceClicked() }
                )
            )
    ) {
        Text(
            modifier = Modifier
                .padding(start = 20.dp)
                .align(Alignment.CenterStart),
            text = stringResource(id = R.string.space_type_shared_space),
            color = colorResource(id = R.color.text_primary),
            style = BodyRegular
        )
        Row(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = if (isUserOwner) {
                    if (requests > 0) {
                        pluralStringResource(
                            R.plurals.multiplayer_number_of_join_requests,
                            requests,
                            requests,
                            requests
                        )
                    } else {
                        stringResource(id = R.string.multiplayer_manage)
                    }
                } else {
                    stringResource(id = R.string.multiplayer_members)
                },
                color = colorResource(id = R.color.text_secondary),
                style = BodyRegular
            )
            Spacer(Modifier.width(10.dp))
            Image(
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = "Arrow forward",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 20.dp)
            )
        }
    }
}

@Composable
fun SharedSpacesIncentiveItem(
    modifier: Modifier = Modifier,
    count: Int,
    onAddMoreSpacesClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorResource(R.color.incentive_gradient_start),
                        colorResource(R.color.incentive_gradient_end)
                    ),
                    startY = 0.0f,
                    endY = Float.POSITIVE_INFINITY // vertical (180deg)
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(
                id = R.string.membership_space_settings_share_limit,
                count
            ),
            color = colorResource(id = R.color.text_primary),
            style = Title2
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.membership_space_settings_share_limit_2),
            color = colorResource(id = R.color.text_primary),
            style = Title3
        )
        ButtonUpgrade(
            modifier = Modifier
                .padding(top = 12.dp)
                .height(36.dp),
            onClick = {
                onAddMoreSpacesClicked()
            },
            text = stringResource(id = R.string.multiplayer_upgrade_button),
            style = UxSmallTextMedium
        )
    }
}

@Preview
@Composable
private fun PrivateSpaceSharingPreview() {
    SharedSpacesIncentiveItem(
        count = 3,
        modifier = Modifier.fillMaxWidth(),
        onAddMoreSpacesClicked = {}
    )
}

@Preview
@Composable
private fun SharedSpaceSharingPreview() {
    SharedSpaceSharing(
        onManageSharedSpaceClicked = {},
        isUserOwner = true
    )
}
