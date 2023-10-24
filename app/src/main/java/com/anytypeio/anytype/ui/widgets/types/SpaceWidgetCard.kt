package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.ui_settings.main.SpaceImageBlock

@Composable
fun SpaceWidgetCard(
    onClick: () -> Unit,
    name: String,
    icon: SpaceIconView
) {
    Box(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 6.dp)
            .height(68.dp)
            .fillMaxWidth()
            .noRippleClickable { onClick() }
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.dashboard_card_background)
            )
    ) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterStart)
        ) {
            SpaceImageBlock(
                icon = icon,
                onSpaceIconClick = {},
                mainSize = 40.dp,
                gradientSize = 24.dp,
                emojiSize = 24.dp
            )
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(start = 71.dp, top = 16.dp, end = 32.dp)
            ,
            text = name.trim().ifEmpty { stringResource(id = R.string.untitled) },
            style = PreviewTitle2Medium,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(start = 71.dp, bottom = 16.dp)
            ,
            text = stringResource(id = R.string.personal),
            style = Relations3,
            color = colorResource(id = R.color.text_secondary),
            maxLines = 1
        )
    }
}