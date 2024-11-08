package com.anytypeio.anytype.ui.home

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
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable

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
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = Color.Red)
                .align(Alignment.CenterStart)
                .noRippleClickable {
                    onWidgetTabClicked()
                }
        )

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color = Color.Yellow)
                .align(Alignment.Center)
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = Color.Blue)
                .align(Alignment.CenterEnd)
                .noRippleClickable {
                    onChatTabClicked()
                }
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
