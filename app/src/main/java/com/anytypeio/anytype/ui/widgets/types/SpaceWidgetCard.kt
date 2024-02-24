package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.PERSONAL_SPACE_TYPE
import com.anytypeio.anytype.core_models.PRIVATE_SPACE_TYPE
import com.anytypeio.anytype.core_models.SpaceType
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.ui_settings.main.SpaceImageBlock


@Composable
@Preview
fun SpaceWidgetCardPreview() {
    SpaceWidgetCard(
        onClick = {},
        icon = SpaceIconView.Placeholder,
        name = "Research",
        spaceType = PRIVATE_SPACE_TYPE,
        onSpaceShareIconClicked = {},
        shareable = true
    )
}

@Composable
fun SpaceWidgetCard(
    onClick: () -> Unit,
    name: String,
    icon: SpaceIconView,
    spaceType: SpaceType,
    onSpaceShareIconClicked: () -> Unit,
    shareable: Boolean
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
                onSpaceIconClick = { onClick() },
                mainSize = 40.dp,
                gradientSize = 24.dp,
                emojiSize = 24.dp,
                gradientCornerRadius = 2.dp
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
        val spaceTypeName = when(spaceType) {
            PERSONAL_SPACE_TYPE -> stringResource(id = R.string.space_type_personal)
            PRIVATE_SPACE_TYPE -> stringResource(id = R.string.space_type_private)
            else -> stringResource(id = R.string.space_type_unknown)
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(start = 71.dp, bottom = 16.dp)
            ,
            text = spaceTypeName,
            style = Relations3,
            color = colorResource(id = R.color.text_secondary),
            maxLines = 1
        )
        if (shareable) {
            Image(
                painter = painterResource(id = R.drawable.ic_space_widget_share_space_icon),
                contentDescription = "Space share icon",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
                    .noRippleClickable(
                        onClick = throttledClick(onClick = { onSpaceShareIconClicked() })
                    )
            )
        }
    }
}