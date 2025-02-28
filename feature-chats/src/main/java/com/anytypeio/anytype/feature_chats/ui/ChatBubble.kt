package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.BUTTON_SECONDARY
import com.anytypeio.anytype.core_ui.foundation.BUTTON_WARNING
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_RED
import com.anytypeio.anytype.core_ui.foundation.GenericAlert
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Caption2Regular
import com.anytypeio.anytype.core_ui.views.fontIBM
import com.anytypeio.anytype.core_utils.const.DateConst.TIME_H24
import com.anytypeio.anytype.core_utils.ext.formatTimeInMillis
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Bubble(
    modifier: Modifier = Modifier,
    name: String,
    reply: ChatView.Message.Reply? = null,
    content: ChatView.Message.Content,
    timestamp: Long,
    attachments: List<ChatView.Message.Attachment> = emptyList(),
    isUserAuthor: Boolean = false,
    isEdited: Boolean = false,
    isMaxReactionCountReached: Boolean = false,
    reactions: List<ChatView.Message.Reaction> = emptyList(),
    onReacted: (String) -> Unit,
    onDeleteMessage: () -> Unit,
    onCopyMessage: () -> Unit,
    onEditMessage: () -> Unit,
    onReply: () -> Unit,
    onAttachmentClicked: (ChatView.Message.Attachment) -> Unit,
    onMarkupLinkClicked: (String) -> Unit,
    onScrollToReplyClicked: (ChatView.Message.Reply) -> Unit,
    onAddReactionClicked: () -> Unit,
    onViewChatReaction: (String) -> Unit,
    onMentionClicked: (Id) -> Unit
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showDeleteMessageWarning by remember { mutableStateOf(false) }
    if (showDeleteMessageWarning) {
        ModalBottomSheet(
            onDismissRequest = {
                showDeleteMessageWarning = false
            },
            containerColor = colorResource(id = R.color.background_secondary),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = null
        ) {
            GenericAlert(
                config = AlertConfig.WithTwoButtons(
                    title = stringResource(R.string.chats_alert_delete_this_message),
                    description = stringResource(R.string.chats_alert_delete_this_message_description),
                    firstButtonText = stringResource(R.string.cancel),
                    secondButtonText = stringResource(R.string.delete),
                    secondButtonType = BUTTON_WARNING,
                    firstButtonType = BUTTON_SECONDARY,
                    icon = AlertConfig.Icon(
                        gradient = GRADIENT_TYPE_RED,
                        icon = R.drawable.ic_alert_question_warning
                    )
                ),
                onFirstButtonClicked = {
                    showDeleteMessageWarning = false
                },
                onSecondButtonClicked = {
                    onDeleteMessage()
                }
            )
        }
    }
    Column(
        modifier = modifier
            .width(IntrinsicSize.Max)
    ) {
        if (reply != null) {
            ChatBubbleReply(
                reply = reply,
                onScrollToReplyClicked = onScrollToReplyClicked
            )
        }
        // Bubble username section
        if (!isUserAuthor) {
            Text(
                text = name,
                style = Caption1Medium,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 12.dp,
                        end = 12.dp
                    )
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        // Rendering text with attachments
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isUserAuthor)
                        colorResource(R.color.background_primary)
                    else
                        colorResource(R.color.shape_transparent_secondary),
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    showDropdownMenu = !showDropdownMenu
                }
                .padding(vertical = 4.dp)
        ) {
            BubbleAttachments(
                attachments = attachments,
                isUserAuthor = isUserAuthor,
                onAttachmentClicked = onAttachmentClicked
            )
            if (content.msg.isNotEmpty()) {
                Box(
                    modifier = Modifier.padding(
                        top = 4.dp,
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 4.dp
                    )
                ) {
                    // Rendering text body message
                    Text(
                        modifier = Modifier,
                        text = buildAnnotatedString {
                            content.parts.forEach { part ->
                                if (part.link != null && part.link.param != null) {
                                    withLink(
                                        LinkAnnotation.Clickable(
                                            tag = DEFAULT_MENTION_LINK_TAG,
                                            styles = TextLinkStyles(
                                                style = SpanStyle(
                                                    fontWeight = if (part.isBold) FontWeight.Bold else null,
                                                    fontStyle = if (part.isItalic) FontStyle.Italic else null,
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            )
                                        ) {
                                            onMarkupLinkClicked(part.link.param.orEmpty())
                                        }
                                    ) {
                                        append(part.part)
                                    }
                                } else if (part.mention != null && part.mention.param != null) {
                                    withLink(
                                        LinkAnnotation.Clickable(
                                            tag = DEFAULT_MENTION_SPAN_TAG,
                                            styles = TextLinkStyles(
                                                style = SpanStyle(
                                                    fontWeight = if (part.isBold) FontWeight.Bold else null,
                                                    fontStyle = if (part.isItalic) FontStyle.Italic else null,
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            )
                                        ) {
                                            onMentionClicked(part.mention.param.orEmpty())
                                        }
                                    ) {
                                        append(part.part)
                                    }
                                } else if (part.emoji != null && part.emoji.param != null) {
                                    append(part.emoji.param)
                                } else {
                                    withStyle(
                                        style = SpanStyle(
                                            fontWeight = if (part.isBold) FontWeight.Bold else null,
                                            fontStyle = if (part.isItalic) FontStyle.Italic else null,
                                            textDecoration = if (part.underline)
                                                TextDecoration.Underline
                                            else if (part.isStrike)
                                                TextDecoration.LineThrough
                                            else null,
                                            fontFamily = if (part.isCode) fontIBM else null,
                                        )
                                    ) {
                                        append(part.part)
                                    }
                                }
                            }
                            if (isEdited) {
                                withStyle(
                                    style = SpanStyle(color = colorResource(id = R.color.text_tertiary))
                                ) {
                                    append(
                                        " (${stringResource(R.string.chats_message_edited)})"
                                    )
                                }
                            }

                            withStyle(
                                style = SpanStyle(
                                    color = Color.Transparent
                                )
                            ) {
                                append(
                                    timestamp.formatTimeInMillis(
                                        TIME_H24
                                    )
                                )
                            }
                        },
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary),
                    )
                    // Rendering message timestamp
                    Text(
                        modifier = Modifier
                            .align(Alignment.BottomEnd),
                        text = timestamp.formatTimeInMillis(
                            TIME_H24
                        ),
                        style = Caption2Regular,
                        color = colorResource(id = R.color.transparent_active),
                        maxLines = 1
                    )
                }
            }
            MaterialTheme(
                shapes = MaterialTheme.shapes.copy(
                    medium = RoundedCornerShape(
                        16.dp
                    )
                ),
                colors = MaterialTheme.colors.copy(
                    surface = colorResource(id = R.color.background_secondary)
                )
            ) {
                DropdownMenu(
                    offset = DpOffset(0.dp, 8.dp),
                    expanded = showDropdownMenu,
                    onDismissRequest = {
                        showDropdownMenu = false
                    }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.chats_add_reaction),
                                color = colorResource(id = R.color.text_primary),
                                modifier = Modifier.padding(end = 64.dp)
                            )
                        },
                        onClick = {
                            onAddReactionClicked()
                            showDropdownMenu = false
                        }
                    )
                    Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.chats_reply),
                                color = colorResource(id = R.color.text_primary),
                                modifier = Modifier.padding(end = 64.dp)
                            )
                        },
                        onClick = {
                            onReply()
                            showDropdownMenu = false
                        }
                    )
                    if (content.msg.isNotEmpty()) {
                        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.copy),
                                    color = colorResource(id = R.color.text_primary),
                                    modifier = Modifier.padding(end = 64.dp)
                                )
                            },
                            onClick = {
                                onCopyMessage()
                                showDropdownMenu = false
                            }
                        )
                    }
                    if (isUserAuthor) {
                        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.edit),
                                    color = colorResource(id = R.color.text_primary),
                                    modifier = Modifier.padding(end = 64.dp)
                                )
                            },
                            onClick = {
                                onEditMessage()
                                showDropdownMenu = false
                            }
                        )
                    }
                    if (isUserAuthor) {
                        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.delete),
                                    color = colorResource(id = R.color.palette_system_red),
                                    modifier = Modifier.padding(end = 64.dp)
                                )
                            },
                            onClick = {
                                showDeleteMessageWarning = true
                                showDropdownMenu = false
                            }
                        )
                    }
                }
            }
        }
        if (reactions.isNotEmpty()) {
            ReactionList(
                reactions = reactions,
                onReacted = onReacted,
                onViewReaction = onViewChatReaction,
                onAddNewReaction = onAddReactionClicked,
                isMaxReactionCountReached = isMaxReactionCountReached
            )
        }
    }
}

@Composable
private fun ChatBubbleReply(
    reply: ChatView.Message.Reply,
    onScrollToReplyClicked: (ChatView.Message.Reply) -> Unit
) {
    Text(
        text = reply.author,
        modifier = Modifier
            .padding(
                start = 16.dp,
                top = 8.dp,
                end = 12.dp
            )
            .alpha(0.5f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = colorResource(id = R.color.text_primary),
        style = Caption1Medium
    )
    Spacer(modifier = Modifier.height(2.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable {
                onScrollToReplyClicked(reply)
            }
            .alpha(0.5f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(
                    color = colorResource(R.color.shape_transparent_primary),
                    shape = RoundedCornerShape(4.dp)
                )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(R.color.shape_transparent_secondary),
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    onScrollToReplyClicked(reply)
                }
                .alpha(0.5f)
        ) {
            Text(
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                ),
                text = reply.text,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = colorResource(id = R.color.text_primary),
                style = Caption1Regular
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun ChatUserAvatar(
    msg: ChatView.Message,
    avatar: ChatView.Message.Avatar,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                color = colorResource(id = R.color.text_tertiary),
                shape = CircleShape
            )
    ) {
        Text(
            text = msg.author.take(1).uppercase().ifEmpty { stringResource(id = R.string.u) },
            modifier = Modifier.align(Alignment.Center),
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.text_white)
            )
        )
        if (avatar is ChatView.Message.Avatar.Image) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatar.hash)
                    .crossfade(true)
                    .build(),
                contentDescription = "Space member profile icon",
                modifier = modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}