package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

@Composable
fun ChatTopToolbar(
    modifier: Modifier,
    header: ChatViewModel.HeaderView,
    onSpaceIconClicked: () -> Unit,
    onBackButtonClicked: () -> Unit,
    onSpaceNameClicked: () -> Unit
) {
    Row(
        modifier = modifier
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
                painter = painterResource(R.drawable.ic_default_top_back),
                contentDescription = stringResource(R.string.content_desc_back_button)
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .noRippleClickable { onSpaceNameClicked() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when(header) {
                    is ChatViewModel.HeaderView.Default -> header.title.ifEmpty {
                        stringResource(R.string.untitled)
                    }
                    is ChatViewModel.HeaderView.Init -> ""
                },
                color = colorResource(R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = Title1
            )
            if (header is ChatViewModel.HeaderView.Default && header.isMuted) {
                Spacer(modifier = Modifier.width(6.dp))
                Image(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(id = R.drawable.ci_notifications_off),
                    contentDescription = stringResource(id = R.string.content_desc_muted),
                    colorFilter = ColorFilter.tint(colorResource(R.color.text_primary))
                )
            }
        }
        if (header is ChatViewModel.HeaderView.Default && header.showIcon) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .noRippleClickable {
                        onSpaceIconClicked()
                    }
            ) {
                SpaceIconView(
                    modifier = Modifier.align(Alignment.Center),
                    mainSize = 28.dp,
                    icon = header.icon,
                    onSpaceIconClick = {
                        onSpaceIconClicked()
                    }
                )
            }
        } else {
            Spacer(
                modifier = Modifier.width(60.dp)
            )
        }

    }
}

@DefaultPreviews
@Composable
fun ChatTopToolbarPreview() {
    ChatTopToolbar(
        modifier = Modifier.fillMaxWidth(),
        header = ChatViewModel.HeaderView.Default(
            title = LoremIpsum(words = 10).values.joinToString(),
            icon = SpaceIconView.ChatSpace.Placeholder(name = "Us"),
            showIcon = true,
            isMuted = false
        ),
        onSpaceIconClicked = {},
        onBackButtonClicked = {},
        onSpaceNameClicked = {}
    )
}

@DefaultPreviews
@Composable
fun ChatTopToolbarMutedPreview() {
    ChatTopToolbar(
        modifier = Modifier.fillMaxWidth(),
        header = ChatViewModel.HeaderView.Default(
            title = "My Chat Space",
            icon = SpaceIconView.ChatSpace.Placeholder(name = "MCS"),
            showIcon = true,
            isMuted = true
        ),
        onSpaceIconClicked = {},
        onBackButtonClicked = {},
        onSpaceNameClicked = {}
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

@Composable
fun GoToMentionButton(
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
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
                    onClick()
                }

        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_go_to_mention_button),
                contentDescription = "Arrow icon",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@DefaultPreviews
@Composable
fun GoToMentionButtonPreview() {
    GoToMentionButton(
        enabled = true,
        modifier = Modifier,
        onClick = {}
    )
}

@Composable
fun FloatingDateHeader(
    modifier: Modifier,
    text: String
) {
    Box(
        modifier = modifier
            .background(
                color = colorResource(R.color.transparent_active),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = Caption1Medium,
            color = colorResource(R.color.glyph_white),
        )
    }
}

@DefaultPreviews
@Composable
fun FloatingDateHeaderPreview() {
    FloatingDateHeader(
        modifier = Modifier,
        text = "Today"
    )
}

@Composable
fun TopDiscussionToolbar(
    title: String? = null,
    isHeaderVisible: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.Center)
                    .background(color = Color.Green, shape = CircleShape)
            )
        }
        Text(
            text = if (isHeaderVisible) "" else title ?: stringResource(id = R.string.untitled),
            style = PreviewTitle2Regular,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(48.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_toolbar_three_dots),
                contentDescription = "Three dots menu",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@DefaultPreviews
@Composable
fun TopDiscussionToolbarPreview() {
    TopDiscussionToolbar()
}
