package com.anytypeio.anytype.ui.home


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.features.SpaceIconView
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.feature_discussions.R
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

@Composable
fun HomeScreenToolbar(
    spaceIconView: SpaceIconView,
    isChatActive: Boolean,
    onWidgetTabClicked: () -> Unit,
    onChatTabClicked: () -> Unit,
    onSpaceIconClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp)
    ) {

        Image(
           painter = painterResource(id = R.drawable.ic_home_toolbar_widgets),
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.CenterStart)
                .alpha(
                    if (isChatActive) 0.5f else 1f
                )
                .noRippleClickable {
                    onWidgetTabClicked()
                },
            contentDescription = "Widgets button"
        )

        SpaceIconView(
            modifier = Modifier.align(Alignment.Center),
            icon = spaceIconView,
            onSpaceIconClick = {
                onSpaceIconClicked()
            },
            mainSize = 40.dp
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
        onSpaceIconClicked = {}
    )
}
