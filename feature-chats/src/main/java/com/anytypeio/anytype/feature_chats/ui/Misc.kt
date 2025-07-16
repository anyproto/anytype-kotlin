package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption2Medium
import com.anytypeio.anytype.feature_chats.R

@Composable
internal fun EmptyState(
    modifier: Modifier,
    canCreateInviteLink: Boolean,
    onShareInviteClicked: () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(56.dp),
                painter = painterResource(id = R.drawable.ic_vault_create_space),
                contentDescription = "Empty state icon",
                colorFilter = ColorFilter.tint(colorResource(id = R.color.transparent_inactive))
            )
            Text(
                text = stringResource(R.string.chat_empty_state_title),
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )
            Text(
                text = stringResource(R.string.chat_empty_state_subtitle),
                style = BodyRegular,
                color = colorResource(id = R.color.text_secondary),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
            if (canCreateInviteLink) {
                ButtonSecondary(
                    text = stringResource(R.string.chat_empty_state_share_invite_button),
                    onClick = { onShareInviteClicked() },
                    size = ButtonSize.SmallSecondary,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
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