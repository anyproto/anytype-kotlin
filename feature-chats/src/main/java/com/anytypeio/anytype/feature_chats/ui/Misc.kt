package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption2Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.feature_chats.R

@Composable
internal fun EmptyState(
    modifier: Modifier,
    onAddMembersClick: () -> Unit,
    onShowQRCodeClick: () -> Unit,
    inviteLinkAccessLevel: SpaceInviteLinkAccessLevel = SpaceInviteLinkAccessLevel.LinkDisabled(),
    spaceUxType: SpaceUxType? = null
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.chat_empty_state_title),
                style = BodyBold,
                color = colorResource(id = R.color.text_primary),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Feature list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp)
                    .padding(horizontal = 25.dp),
            ) {
                FeatureRow(
                    icon = R.drawable.ic_chat_empty_1,
                    text = stringResource(R.string.chat_empty_state_feature_yours_forever)
                )
                FeatureRow(
                    icon = R.drawable.ic_chat_empty_2,
                    text = stringResource(R.string.chat_empty_state_feature_offline),
                    modifier = Modifier.padding(top = 8.dp)
                )
                FeatureRow(
                    icon = R.drawable.ic_chat_empty_3,
                    text = stringResource(R.string.chat_empty_state_feature_encrypted),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Buttons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
            ) {
                if (spaceUxType != SpaceUxType.ONE_TO_ONE) {
                    ButtonPrimary(
                        text = stringResource(R.string.chat_empty_state_add_members_button),
                        onClick = onAddMembersClick,
                        size = ButtonSize.SmallSecondary,
                        modifier = Modifier
                    )
                }
                if (inviteLinkAccessLevel !is SpaceInviteLinkAccessLevel.LinkDisabled) {
                    ButtonPrimary(
                        text = stringResource(R.string.chat_empty_state_show_qr_button),
                        onClick = onShowQRCodeClick,
                        size = ButtonSize.SmallSecondary,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter = painterResource(icon),
            modifier = Modifier.padding(end = 12.dp),
            contentDescription = null
        )
        Text(
            text = text,
            style = PreviewTitle2Regular,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Composable
internal fun LoadingState(
    modifier: Modifier
) {
    Box(modifier = modifier) {
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(16.dp),
            text = stringResource(R.string.loading_wait),
            textAlign = TextAlign.Center,
            style = Caption2Medium,
            color = colorResource(R.color.text_secondary)
        )
    }
}

@Preview(name = "Empty State", showBackground = true)
@Composable
private fun EmptyStatePreview() {
    EmptyState(
        modifier = Modifier.fillMaxWidth(),
        onAddMembersClick = {},
        onShowQRCodeClick = {}
    )
}

@Preview(name = "Loading State", showBackground = true)
@Composable
private fun LoadingStatePreview() {
    LoadingState(
        modifier = Modifier.fillMaxWidth()
    )
}