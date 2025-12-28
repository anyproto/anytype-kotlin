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
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopToolbar(
    modifier: Modifier,
    header: ChatViewModel.HeaderView,
    onSpaceIconClicked: () -> Unit,
    onBackButtonClicked: () -> Unit,
    onSpaceNameClicked: () -> Unit,
    onInviteMembersClicked: () -> Unit,
    onEditInfo: () -> Unit,
    onPin: () -> Unit,
    onCopyLink: () -> Unit = {},
    onMoveToBin: () -> Unit,
    onProperties: () -> Unit = {},
    onNotificationSettingChanged: (NotificationSetting) -> Unit
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.height(52.dp)
    ) {
        Image(
            modifier = Modifier
                .height(52.dp)
                .width(56.dp)
                .noRippleClickable {
                    onBackButtonClicked()
                },
            painter = painterResource(R.drawable.ic_default_top_back),
            contentDescription = stringResource(R.string.content_desc_back_button),
            contentScale = ContentScale.Inside
        )
        
        when(header) {
            is ChatViewModel.HeaderView.ChatObject -> {
                ListWidgetObjectIcon(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .noRippleClickable {
                            showDropdownMenu = !showDropdownMenu
                        }
                    ,
                    iconSize = 28.dp,
                    icon = header.icon
                )
            }
            is ChatViewModel.HeaderView.Default -> {
                if (header.showAddMembers) {
                    Image(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 48.dp)
                            .height(52.dp)
                            .width(40.dp)
                            .noRippleClickable {
                                onInviteMembersClicked()
                            },
                        contentScale = ContentScale.Inside,
                        painter = painterResource(id = R.drawable.ic_space_settings_invite_members),
                        contentDescription = "Invite members icon",
                        colorFilter = ColorFilter.tint(colorResource(R.color.control_transparent_secondary))
                    )
                }
                SpaceIconView(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    mainSize = 28.dp,
                    icon = header.icon,
                    onSpaceIconClick = { onSpaceIconClicked() }
                )
            }
            ChatViewModel.HeaderView.Init -> {
                // Do nothing
            }
        }

        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 100.dp)
                .fillMaxWidth()
                .noRippleClickable {
                    if (header is ChatViewModel.HeaderView.ChatObject && header.showDropDownMenu) {
                        showDropdownMenu = !showDropdownMenu
                    } else {
                        onSpaceNameClicked()
                    }
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isMuted = when (header) {
                is ChatViewModel.HeaderView.Default -> header.isMuted
                is ChatViewModel.HeaderView.ChatObject -> header.isMuted
                else -> false
            }

            Text(
                text = when (header) {
                    is ChatViewModel.HeaderView.Default -> header.title.ifEmpty {
                        stringResource(R.string.untitled)
                    }
                    is ChatViewModel.HeaderView.ChatObject -> header.title.ifEmpty {
                        stringResource(R.string.untitled)
                    }
                    is ChatViewModel.HeaderView.Init -> ""
                },
                color = colorResource(R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = Title1,
                modifier = if (isMuted) Modifier.weight(1f, fill = false) else Modifier
            )
            if (isMuted) {
                Spacer(modifier = Modifier.width(6.dp))
                Image(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(id = R.drawable.ci_notifications_off),
                    contentDescription = stringResource(id = R.string.content_desc_muted),
                    colorFilter = ColorFilter.tint(colorResource(R.color.text_primary))
                )
            }
        }

        if (header is ChatViewModel.HeaderView.ChatObject && header.showDropDownMenu) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 52.dp)
                    .align(Alignment.TopEnd)
            ) {
                ChatMenu(
                    expanded = showDropdownMenu,
                    currentNotificationSetting = header.notificationSetting,
                    isPinned = header.isPinned,
                    canEdit = header.canEdit,
                    onDismissRequest = {
                        showDropdownMenu = false
                    },
                    onPropertiesClick = {
                        onProperties()
                        showDropdownMenu = false
                    },
                    onEditInfoClick = {
                        onEditInfo()
                        showDropdownMenu = false
                    },
                    onNotificationSettingChanged = { setting ->
                        onNotificationSettingChanged(setting)
                        showDropdownMenu = false
                    },
                    onPinClick = {
                        onPin()
                        showDropdownMenu = false
                    },
                    onMoveToBinClick = {
                        onMoveToBin()
                        showDropdownMenu = false
                    }
                )
            }
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
            isMuted = true
        ),
        onSpaceIconClicked = {},
        onBackButtonClicked = {},
        onSpaceNameClicked = {},
        onInviteMembersClicked = {},
        onEditInfo = {},
        onPin = {},
        onCopyLink = {},
        onMoveToBin = {},
        onProperties = {},
        onNotificationSettingChanged = {}
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
            isMuted = true
        ),
        onSpaceIconClicked = {},
        onBackButtonClicked = {},
        onSpaceNameClicked = {},
        onInviteMembersClicked = {},
        onEditInfo = {},
        onPin = {},
        onCopyLink = {},
        onMoveToBin = {},
        onProperties = {},
        onNotificationSettingChanged = {}
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
                },
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
