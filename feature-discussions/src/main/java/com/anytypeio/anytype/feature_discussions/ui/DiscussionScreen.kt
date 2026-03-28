package com.anytypeio.anytype.feature_discussions.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionHeader
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionInputMode
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionView
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel

@Composable
fun DiscussionScreenWrapper(
    vm: DiscussionViewModel,
    onBackClicked: () -> Unit
) {
    val header = vm.header.collectAsStateWithLifecycle().value
    val messages = vm.messages.collectAsStateWithLifecycle().value
    val inputMode = vm.inputMode.collectAsStateWithLifecycle().value
    val clipboard = LocalClipboardManager.current

    var inputText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    DiscussionScreen(
        header = header,
        comments = messages,
        onBackClicked = onBackClicked,
        inputText = inputText,
        onInputValueChange = { inputText = it },
        onSendClicked = { text ->
            vm.onSendComment(text)
            inputText = TextFieldValue("")
        },
        inputMode = inputMode,
        onReplyComment = { vm.onReplyComment(it) },
        onReplyToReply = { vm.onReplyToReply(it) },
        onCopyText = { clipboard.setText(AnnotatedString(it)) },
        onClearReply = { vm.onClearReply() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionScreen(
    header: DiscussionHeader,
    comments: List<DiscussionView>,
    onBackClicked: () -> Unit,
    inputText: TextFieldValue = TextFieldValue(""),
    onInputValueChange: (TextFieldValue) -> Unit = {},
    onSendClicked: (String) -> Unit = {},
    inputMode: DiscussionInputMode = DiscussionInputMode.Default,
    onReplyComment: (DiscussionView.Comment) -> Unit = {},
    onReplyToReply: (DiscussionView.Reply) -> Unit = {},
    onCopyText: (String) -> Unit = {},
    onClearReply: () -> Unit = {}
) {
    Scaffold(
        containerColor = colorResource(id = R.color.background_primary),
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            DiscussionTopBar(
                header = header,
                onBackClicked = onBackClicked,
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DiscussionCommentList(
                comments = comments,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                onReplyComment = onReplyComment,
                onReplyToReply = onReplyToReply,
                onCopyText = onCopyText
            )
            if (inputMode is DiscussionInputMode.Reply) {
                DiscussionReplyBanner(
                    mode = inputMode,
                    onClearReply = onClearReply
                )
            }
            DiscussionCommentInput(
                text = inputText,
                onValueChange = onInputValueChange,
                onSendClicked = onSendClicked,
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
fun DiscussionReplyBanner(
    mode: DiscussionInputMode.Reply,
    onClearReply: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(colorResource(id = R.color.background_primary))
    ) {
        Text(
            text = "Reply to ${mode.author}",
            style = Caption1Medium,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 44.dp)
        )
        Text(
            text = mode.text,
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp, top = 28.dp, end = 44.dp)
        )
        Icon(
            painter = painterResource(
                id = com.anytypeio.anytype.feature_discussions.R.drawable.ic_chat_close_chat_box_reply
            ),
            contentDescription = "Clear reply",
            tint = colorResource(id = R.color.glyph_active),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .clickable { onClearReply() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionTopBar(
    header: DiscussionHeader,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(id = R.color.background_primary)
        ),
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = colorResource(id = R.color.glyph_active)
                )
            }
        },
        title = {
            Column {
                if (header.title.isNotEmpty()) {
                    Text(
                        text = header.title,
                        style = Caption1Medium,
                        color = colorResource(id = R.color.text_transparent_secondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${header.commentCount} ${stringResource(id = com.anytypeio.anytype.localization.R.string.discussion_comments)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 20.sp,
                    letterSpacing = (-0.24).sp,
                    color = colorResource(id = R.color.text_primary)
                )
            }
        }
    )
}

@Composable
fun DiscussionCommentList(
    comments: List<DiscussionView>,
    modifier: Modifier = Modifier,
    onReplyComment: (DiscussionView.Comment) -> Unit = {},
    onReplyToReply: (DiscussionView.Reply) -> Unit = {},
    onCopyText: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(
            items = comments,
            key = { item ->
                when (item) {
                    is DiscussionView.Comment -> item.id
                    is DiscussionView.Reply -> item.id
                    is DiscussionView.ReplyDivider -> "reply-divider-${item.replyId}"
                    is DiscussionView.ThreadDivider -> "thread-divider-${item.threadId}"
                }
            }
        ) { item ->
            when (item) {
                is DiscussionView.Comment -> {
                    DiscussionCommentItem(
                        comment = item,
                        onReply = { onReplyComment(item) },
                        onCopy = { onCopyText(item.content.msg) }
                    )
                }
                is DiscussionView.Reply -> {
                    DiscussionReplyItem(
                        reply = item,
                        onReply = { onReplyToReply(item) },
                        onCopy = { onCopyText(item.content.msg) }
                    )
                }
                is DiscussionView.ReplyDivider -> {
                    HorizontalDivider(
                        color = colorResource(id = R.color.shape_secondary),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(
                            start = 24.dp,
                            end = 16.dp
                        )
                    )
                }
                is DiscussionView.ThreadDivider -> {
                    HorizontalDivider(
                        color = colorResource(id = R.color.shape_secondary),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiscussionCommentItem(
    comment: DiscussionView.Comment,
    onReply: () -> Unit = {},
    onCopy: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var showDropdownMenu by remember { mutableStateOf(false) }

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDropdownMenu = true
                    }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Author row: avatar + name + date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    CommentAvatar(
                        avatar = comment.avatar,
                        size = 32
                    )
                    Text(
                        text = comment.author,
                        style = Caption1Medium,
                        color = colorResource(id = R.color.text_primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (comment.formattedDate != null) {
                    Text(
                        text = comment.formattedDate,
                        style = Caption1Regular,
                        color = colorResource(id = R.color.text_secondary)
                    )
                }
            }
            // Text content
            if (comment.content.msg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comment.content.msg,
                    style = BodyCallout,
                    color = colorResource(id = R.color.text_primary)
                )
            }
            // Reactions
            if (comment.reactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ReactionsRow(reactions = comment.reactions)
            }
        }
        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = com.anytypeio.anytype.localization.R.string.chats_reply),
                        style = PreviewTitle1Regular,
                        color = colorResource(id = R.color.text_primary)
                    )
                },
                onClick = {
                    showDropdownMenu = false
                    onReply()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_dropdown_menu_reply),
                        contentDescription = null,
                        tint = colorResource(id = R.color.glyph_active)
                    )
                }
            )
            if (comment.content.msg.isNotEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = com.anytypeio.anytype.localization.R.string.copy_plain_text),
                            style = PreviewTitle1Regular,
                            color = colorResource(id = R.color.text_primary)
                        )
                    },
                    onClick = {
                        showDropdownMenu = false
                        onCopy()
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dropdown_menu_content_copy),
                            contentDescription = null,
                            tint = colorResource(id = R.color.glyph_active)
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiscussionReplyItem(
    reply: DiscussionView.Reply,
    onReply: () -> Unit = {},
    onCopy: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var showDropdownMenu by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDropdownMenu = true
                    }
                )
                .padding(
                    start = 12.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 12.dp
                )
        ) {
            // Vertical reply bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        color = colorResource(id = R.color.shape_transparent_secondary),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Author row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        CommentAvatar(
                            avatar = reply.avatar,
                            size = 32
                        )
                        Text(
                            text = reply.author,
                            style = Caption1Medium,
                            color = colorResource(id = R.color.text_primary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (reply.formattedDate != null) {
                        Text(
                            text = reply.formattedDate,
                            style = Caption1Regular,
                            color = colorResource(id = R.color.text_secondary)
                        )
                    }
                }
                // Text content
                if (reply.content.msg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reply.content.msg,
                        style = BodyCallout,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
                // Reactions
                if (reply.reactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ReactionsRow(reactions = reply.reactions)
                }
            }
        }
        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = com.anytypeio.anytype.localization.R.string.chats_reply),
                        style = PreviewTitle1Regular,
                        color = colorResource(id = R.color.text_primary)
                    )
                },
                onClick = {
                    showDropdownMenu = false
                    onReply()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_dropdown_menu_reply),
                        contentDescription = null,
                        tint = colorResource(id = R.color.glyph_active)
                    )
                }
            )
            if (reply.content.msg.isNotEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = com.anytypeio.anytype.localization.R.string.copy_plain_text),
                            style = PreviewTitle1Regular,
                            color = colorResource(id = R.color.text_primary)
                        )
                    },
                    onClick = {
                        showDropdownMenu = false
                        onCopy()
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dropdown_menu_content_copy),
                            contentDescription = null,
                            tint = colorResource(id = R.color.glyph_active)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun CommentAvatar(
    avatar: DiscussionView.Avatar,
    size: Int
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                color = colorResource(id = R.color.text_tertiary),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (avatar) {
                is DiscussionView.Avatar.Initials -> avatar.initial.ifEmpty {
                    stringResource(id = com.anytypeio.anytype.localization.R.string.u)
                }
                is DiscussionView.Avatar.Image -> avatar.fallbackInitial.ifEmpty {
                    stringResource(id = com.anytypeio.anytype.localization.R.string.u)
                }
            },
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.text_white)
            )
        )
        if (avatar is DiscussionView.Avatar.Image) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatar.hash)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun RichTextContent(parts: List<DiscussionView.Content.Part>) {
    val annotatedString = buildAnnotatedString {
        parts.forEach { part ->
            val style = SpanStyle(
                fontWeight = if (part.isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (part.isItalic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = when {
                    part.isStrike && part.underline -> TextDecoration.combine(
                        listOf(TextDecoration.LineThrough, TextDecoration.Underline)
                    )
                    part.isStrike -> TextDecoration.LineThrough
                    part.underline -> TextDecoration.Underline
                    else -> TextDecoration.None
                }
            )
            withStyle(style) {
                append(part.part)
            }
        }
    }
    Text(
        text = annotatedString,
        fontSize = 15.sp,
        color = colorResource(id = R.color.text_primary)
    )
}

@Composable
fun ReactionsRow(reactions: List<DiscussionView.Reaction>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        reactions.forEach { reaction ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = colorResource(id = R.color.shape_transparent_primary),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text(
                    text = reaction.emoji,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${reaction.count}",
                    style = Caption1Regular,
                    color = colorResource(id = R.color.text_primary)
                )
            }
        }
    }
}

