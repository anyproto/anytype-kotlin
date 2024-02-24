package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Title1

@Composable
@Preview
fun ShareInviteLinkCardPreview() {
    ShareInviteLinkCard(
        link = "https://anytype.io/ibafyrfhfsag6rea3ifffsasssa3ifffsasssga3ifffsasssga3ifffsasssg",
        onShareInviteClicked = {},
    )
}

@Composable
fun ShareInviteLinkCard(
    link: String,
    onShareInviteClicked: () -> Unit
) {
    Card {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Invite link",
            style = Title1,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Send this link to invite others. Assign access rights upon their request approval",
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
                color = colorResource(id = R.color.text_secondary)
            )
        }
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        ButtonPrimary(
            text = "Share invite link",
            onClick = onShareInviteClicked,
            size = ButtonSize.Large,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}