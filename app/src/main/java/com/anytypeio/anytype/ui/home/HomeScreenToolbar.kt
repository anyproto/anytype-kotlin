package com.anytypeio.anytype.ui.home


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.features.SpaceIconView
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.feature_discussions.R
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

@Composable
fun HomeScreenToolbar(
    spaceIconView: SpaceIconView,
    isChatActive: Boolean,
    onWidgetTabClicked: () -> Unit,
    onChatTabClicked: () -> Unit,
    onSpaceIconClicked: () -> Unit,
    name: String,
    membersCount: Int
) {
    Box(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp)
    ) {


        SpaceIconView(
            modifier = Modifier
                .align(Alignment.CenterStart),
            icon = spaceIconView,
            onSpaceIconClick = {
                onSpaceIconClicked()
            },
            mainSize = 40.dp
        )

        Text(
            text = name.ifEmpty { stringResource(R.string.untitled) },
            style = PreviewTitle2Medium,
            color = colorResource(R.color.text_primary),
            modifier = Modifier.padding(
                start = 52.dp,
                top = 13.dp
            )
        )

        Text(
            text = if (membersCount > 0 ) {
                pluralStringResource(
                    id = R.plurals.multiplayer_number_of_space_members,
                    membersCount,
                    membersCount,
                    membersCount
                )
            } else
                stringResource(id = R.string.three_dots_text_placeholder),
            style = Relations3,
            color = colorResource(R.color.text_secondary),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 52.dp,
                    bottom = 13.dp
                )
        )

        Image(
            painter = painterResource(id = R.drawable.ic_home_toolbar_widgets),
            modifier = Modifier
                .padding(end = 48.dp)
                .size(32.dp)
                .align(Alignment.CenterEnd)
                .alpha(
                    if (isChatActive) 0.5f else 1f
                )
                .noRippleClickable {
                    onWidgetTabClicked()
                },
            contentDescription = "Widgets button"
        )

        Image(
            painter = painterResource(id = R.drawable.ic_home_toolbar_chat),
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.CenterEnd)
                .alpha(
                    if (isChatActive) 1f else 0.5f
                )
                .noRippleClickable {
                    onChatTabClicked()
                },
            contentDescription = "Chats button"
        )
    }
}

@DefaultPreviews
@Composable
fun HomeScreenToolbarPreview() {
    HomeScreenToolbar(
        onWidgetTabClicked = {},
        onChatTabClicked = {},
        isChatActive = false,
        spaceIconView = SpaceIconView.Loading,
        onSpaceIconClicked = {},
        membersCount = 74,
        name = "Test space"
    )
}
