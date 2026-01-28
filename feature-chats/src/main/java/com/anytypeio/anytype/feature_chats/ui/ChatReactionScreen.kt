package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.features.multiplayer.SpaceMemberIcon
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatReactionViewModel.ViewState
import com.anytypeio.anytype.core_models.ui.SpaceMemberIconView

@Composable
fun ChatReactionScreen(
    viewState: ViewState
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
        EmojiToolbar(viewState)
        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f)
        ) {
            when(viewState) {
                is ViewState.Init -> {
                    // Do nothing.
                }
                is ViewState.Empty -> {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize()
                        ) {
                            EmptyState(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
                is ViewState.Success -> {
                    items(
                        count = viewState.members.size
                    ) { idx ->
                        val member = viewState.members[idx]
                        ChatMemberItem(
                            name = member.name,
                            icon = member.icon,
                            isUser = member.isUser
                        )
                        if (idx != viewState.members.lastIndex) {
                            Divider()
                        }
                    }
                }
                is ViewState.Error.MessageNotFound -> {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize()
                        ) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                color = colorResource(R.color.palette_system_red),
                                text = "Message not found",
                                style = BodyCallout
                            )
                        }
                    }
                }
                is ViewState.Loading -> {

                }
            }
        }
    }
}

@Composable
fun ChatMemberItem(
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
            modifier = Modifier.align(
                alignment = Alignment.CenterStart
            )
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
                    val youAsMemberText = stringResource(id = R.string.multiplayer_you_as_member)
                    Text(
                        text = "($youAsMemberText)",
                        style = PreviewTitle2Regular,
                        color = colorResource(id = R.color.text_secondary),
                    )
                }
            }
            Text(
                text = stringResource(R.string.object_types_human),
                style = Relations3,
                color = colorResource(R.color.text_secondary)
            )
        }
    }
}

@Composable
private fun EmojiToolbar(
    viewState: ViewState
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(viewState.emoji)
            Spacer(modifier = Modifier.width(8.dp))
            when(viewState) {
                is ViewState.Success -> {
                    Text(
                        text = viewState.members.size.toString(),
                        style = BodyRegular,
                        color = colorResource(R.color.text_primary)
                    )
                }
                is ViewState.Empty -> {
                    Text(
                        text = "0",
                        style = BodyRegular,
                        color = colorResource(R.color.text_primary)
                    )
                }
                else -> {
                    // Do nothing.
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier
) {
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        Text(
            stringResource(R.string.chat_message_reactions_no_reactions_yet),
            style = BodyRegular,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = colorResource(R.color.text_primary)
        )
        Text(
            stringResource(R.string.chat_message_reactions_no_reactions_message),
            style = BodyRegular,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = colorResource(R.color.text_primary)
        )
    }
}

@DefaultPreviews
@Composable
private fun MemberPreview() {
    ChatMemberItem(
        name = "Walter Benjamin",
        icon = SpaceMemberIconView.Placeholder(
            name = "Walter"
        )
    )
}

@DefaultPreviews
@Composable
private fun MemberUserPreview() {
    ChatMemberItem(
        name = "Walter Benjamin Walter Walter Walter Walter",
        icon = SpaceMemberIconView.Placeholder(
            name = "Walter"
        ),
        isUser = true
    )
}

@DefaultPreviews
@Composable
private fun MemberOverflowPreview() {
    ChatMemberItem(
        name = "Walter Benjamin Walter Walter Walter Walter",
        icon = SpaceMemberIconView.Placeholder(
            name = "Walter"
        ),
        isUser = true
    )
}

@DefaultPreviews
@Composable
private fun EmojiToolbarPreview() {
    EmojiToolbar(
        viewState = ViewState.Empty(
            emoji = "😀"
        )
    )
}

@DefaultPreviews
@Composable
private fun EmptyStatePreview() {
    EmptyState(modifier = Modifier)
}

@DefaultPreviews
@Composable
private fun ChatReactionEmptyStateScreenPreview() {
    ChatReactionScreen(
        viewState = ViewState.Empty(
            emoji = "😀"
        )
    )
}

@DefaultPreviews
@Composable
private fun ChatReactionSuccessStateScreenPreview() {
    ChatReactionScreen(
        viewState = ViewState.Success(
            emoji = "😀",
            members = listOf(
                ViewState.Member(
                    name = "Walter Benjamin",
                    icon = SpaceMemberIconView.Placeholder(
                        name = "Walter"
                    ),
                    isUser = false
                ),
                ViewState.Member(
                    name = "Walter Benjamin",
                    icon = SpaceMemberIconView.Placeholder(
                        name = "Walter"
                    ),
                    isUser = false
                ),
                ViewState.Member(
                    name = "Walter Benjamin",
                    icon = SpaceMemberIconView.Placeholder(
                        name = "Walter"
                    ),
                    isUser = false
                )
            )
        )
    )
}