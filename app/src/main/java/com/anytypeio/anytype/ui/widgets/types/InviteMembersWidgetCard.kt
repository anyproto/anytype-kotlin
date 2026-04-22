package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodySemiBold

@Composable
fun InviteMembersWidgetCard(
    onWidgetClicked: () -> Unit,
    onDismissClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 6.dp)
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
                Image(
                    painter = painterResource(id = R.drawable.ic_space_settings_invite_members),
                    contentDescription = "Invite Members",
                    modifier = Modifier.size(20.dp),
                    colorFilter = colorResource(id = R.color.palette_system_red).let {
                        ColorFilter.tint(it)
                    }
                )
                Text(
                    text = stringResource(id = R.string.invite_members_widget_title),
                    style = BodySemiBold,
                    maxLines = 1,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier.weight(1f).padding(start = 12.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_search_close_18),
                    contentDescription = "Dismiss",
                    colorFilter = colorResource(id = R.color.control_secondary).let {
                        ColorFilter.tint(it)
                    },
                    modifier = Modifier
                        .size(18.dp)
                        .noRippleClickable { onDismissClicked() }
                )
            }
        }
    }
}

@DefaultPreviews
@Composable
private fun InviteMembersWidgetCardPreview() {
    MaterialTheme {
        InviteMembersWidgetCard(
            onWidgetClicked = {},
            onDismissClicked = {}
        )
    }
}
