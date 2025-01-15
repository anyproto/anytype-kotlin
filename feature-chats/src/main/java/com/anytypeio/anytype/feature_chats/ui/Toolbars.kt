package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

@Composable
fun ChatTopToolbar(
    header: ChatViewModel.HeaderView,
    onSpaceIconClicked: () -> Unit,
    onBackButtonClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight()
                .noRippleClickable {
                    onBackButtonClicked()
                }
        ) {
            Image(
                modifier = Modifier.align(Alignment.Center),
                painter = painterResource(R.drawable.ic_home_top_toolbar_back),
                contentDescription = stringResource(R.string.content_desc_back_button)
            )
        }
        Text(
            text = when(header) {
                is ChatViewModel.HeaderView.Default -> header.title
                is ChatViewModel.HeaderView.Init -> ""
            },
            color = colorResource(R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .noRippleClickable {
                    onSpaceIconClicked()
                }
        ) {
            com.anytypeio.anytype.core_ui.features.SpaceIconView(
                modifier = Modifier.align(Alignment.Center),
                mainSize = 28.dp,
                icon = when(header) {
                    is ChatViewModel.HeaderView.Default -> header.icon
                    is ChatViewModel.HeaderView.Init -> SpaceIconView.Loading
                },
                onSpaceIconClick = {
                    // Do nothing.
                }
            )
        }
    }
}

@DefaultPreviews
@Composable
fun ChatTopToolbarPreview() {
    ChatTopToolbar(
        header = ChatViewModel.HeaderView.Default(
            title = LoremIpsum(words = 10).values.joinToString(),
            icon = SpaceIconView.Placeholder(name = "Us")
        ),
        onSpaceIconClicked = {},
        onBackButtonClicked = {}
    )
}

@Composable
fun EditMessageToolbar(
    onExitClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.background_highlighted_light),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp
                )
            )
    ) {
        Text(
            modifier = Modifier
                .padding(
                    start = 12.dp
                )
                .align(
                    Alignment.CenterStart
                ),
            text = stringResource(R.string.chats_edit_message),
            style = Caption1Medium,
            color = colorResource(id = R.color.text_primary)
        )
        Image(
            modifier = Modifier
                .padding(
                    end = 12.dp
                )
                .align(
                    Alignment.CenterEnd
                )
                .noRippleClickable {
                    onExitClicked()
                }
            ,
            painter = painterResource(id = R.drawable.ic_edit_message_close),
            contentDescription = "Close edit-message mode"
        )
    }
}

@Composable
fun GoToBottomButton(
    enabled: Boolean,
    modifier: Modifier,
    onGoToBottomClicked: () -> Unit
) {
    val transition = updateTransition(
        enabled,
        label = "JumpToBottom visibility animation"
    )
    val bottomOffset by transition.animateDp(label = "JumpToBottom offset animation") {
        if (it) {
            (12).dp
        } else {
            (-12).dp
        }
    }
    if (bottomOffset > 0.dp) {
        Box(
            modifier = modifier
                .offset(x = 0.dp, y = -bottomOffset)
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color = colorResource(id = R.color.navigation_panel))
                .clickable {
                    onGoToBottomClicked()
                }

        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_go_to_bottom_arrow),
                contentDescription = "Arrow icon",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}