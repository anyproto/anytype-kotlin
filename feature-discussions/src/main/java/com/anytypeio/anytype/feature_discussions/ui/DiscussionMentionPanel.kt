package com.anytypeio.anytype.feature_discussions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.multiplayer.SpaceMemberIcon
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_models.ui.SpaceMemberIconView
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel.MentionPanelState

@Composable
fun DiscussionMentionPanel(
    state: MentionPanelState.Visible,
    onMemberClicked: (MentionPanelState.Member) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .padding(12.dp)
            .fillMaxWidth()
            .height(168.dp)
            .background(
                color = colorResource(R.color.background_primary),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        items(
            items = state.results,
            key = { member -> member.id }
        ) { member ->
            DiscussionMemberItem(
                name = member.name,
                icon = member.icon,
                isUser = member.isUser,
                modifier = Modifier
                    .fillMaxWidth()
                    .noRippleClickable { onMemberClicked(member) }
            )
            HorizontalDivider(
                color = colorResource(R.color.shape_secondary),
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
fun DiscussionMemberItem(
    modifier: Modifier = Modifier,
    name: String,
    icon: SpaceMemberIconView,
    isUser: Boolean = false
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        SpaceMemberIcon(
            icon = icon,
            iconSize = 48.dp,
            modifier = Modifier.align(alignment = Alignment.CenterStart)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart)
                .padding(start = 60.dp)
        ) {
            Row {
                Text(
                    text = name.ifEmpty { stringResource(R.string.untitled) },
                    color = colorResource(R.color.text_primary),
                    style = PreviewTitle2Regular,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isUser) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${stringResource(id = com.anytypeio.anytype.localization.R.string.multiplayer_you_as_member)})",
                        style = PreviewTitle2Regular,
                        color = colorResource(id = R.color.text_secondary),
                    )
                }
            }
        }
    }
}
