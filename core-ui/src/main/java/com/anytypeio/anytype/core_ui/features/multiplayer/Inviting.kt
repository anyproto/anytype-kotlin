package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Title1

@Composable
@Preview
fun ShareInviteLinkCardPreview() {
    ShareInviteLinkCard(
        link = "https://anytype.io/ibafyrfhfsag6rea3ifffsasssa3ifffsasssga3ifffsasssga3ifffsas",
        onShareInviteClicked = {},
        onRegenerateInviteLinkClicked = {},
        onShowQrCodeClicked = {},
        modifier = Modifier
    )
}

@Composable
@Preview
fun GenerateInviteLinkCardPreview() {
    GenerateInviteLinkCard(
        modifier = Modifier,
        onGenerateInviteLinkClicked = {}
    )
}

@Composable
fun ShareInviteLinkCard(
    modifier: Modifier = Modifier,
    link: String,
    onShareInviteClicked: () -> Unit,
    onRegenerateInviteLinkClicked: () -> Unit,
    onShowQrCodeClicked: () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.background_primary)
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 16.dp
        )

    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.multiplayer_invite_link),
                style = Title1,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier.weight(1.0f)
            )
            Image(
                painter = painterResource(id = R.drawable.ic_action_replace),
                contentDescription = "Regenerate-invite icon",
                modifier = Modifier.noRippleClickable {
                    onRegenerateInviteLinkClicked()
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.multiplayer_share_invite_link_description),
            style = BodyCalloutRegular,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .height(48.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = link,
                style = BodyCalloutRegular,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colorResource(id = R.color.text_secondary),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            ButtonPrimary(
                text = stringResource(R.string.multiplayer_share_invite_link),
                onClick = onShareInviteClicked,
                size = ButtonSize.Large,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            ButtonSecondary(
                text = stringResource(R.string.multiplayer_show_qr_code),
                onClick = onShowQrCodeClicked,
                size = ButtonSize.Large,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun GenerateInviteLinkCard(
    modifier: Modifier = Modifier,
    onGenerateInviteLinkClicked: () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.background_primary)
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 16.dp
        )

    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.multiplayer_invite_link),
                style = Title1,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier.weight(1.0f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.multiplayer_generate_invite_link_description),
            style = BodyCalloutRegular,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            ButtonPrimary(
                text = stringResource(R.string.multiplayer_generate_invite_link),
                onClick = onGenerateInviteLinkClicked,
                size = ButtonSize.Large,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}