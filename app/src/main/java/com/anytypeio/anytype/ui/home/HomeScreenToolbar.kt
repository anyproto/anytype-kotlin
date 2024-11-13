package com.anytypeio.anytype.ui.home


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.feature_discussions.R

@Composable
fun HomeScreenToolbar(
    onWidgetTabClicked: () -> Unit,
    onChatTabClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp)
    ) {

        Image(
           painter = painterResource(id = R.drawable.ic_home_toolbar_widgets),
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.CenterStart)
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
        onChatTabClicked = {}
    )
}
