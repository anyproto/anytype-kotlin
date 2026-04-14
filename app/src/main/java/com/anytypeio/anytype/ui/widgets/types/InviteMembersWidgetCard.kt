package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.Relations3

@Composable
fun InviteMembersWidgetCard(
    onWidgetClicked: () -> Unit,
    onDismissClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
            .background(
                shape = RoundedCornerShape(24.dp),
                color = colorResource(id = R.color.dashboard_card_background)
            )
            .noRippleClickable { onWidgetClicked() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.invite_members_widget_title),
                    style = BodySemiBold,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_close_round),
                    contentDescription = "Dismiss",
                    tint = colorResource(id = R.color.glyph_active),
                    modifier = Modifier
                        .size(24.dp)
                        .noRippleClickable { onDismissClicked() }
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(id = R.string.invite_members_widget_description),
                style = Relations3,
                color = colorResource(id = R.color.text_secondary)
            )
        }
    }
}
