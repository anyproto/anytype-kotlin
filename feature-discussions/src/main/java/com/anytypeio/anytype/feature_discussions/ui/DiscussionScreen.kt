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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.BUTTON_SECONDARY
import com.anytypeio.anytype.core_ui.foundation.BUTTON_WARNING
import com.anytypeio.anytype.core_ui.foundation.GenericAlert
import com.anytypeio.anytype.core_ui.text.InputSpan
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionHeader
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionInputMode
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionView
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel.MentionPanelState

@Composable
fun DiscussionScreenWrapper(
    vm: DiscussionViewModel,
    onBackClicked: () -> Unit,
    onPlusClicked: () -> Unit = {}
) {
    val header = vm.header.collectAsStateWithLifecycle().value
    val messages = vm.messages.collectAsStateWithLifecycle().value
    val inputMode = vm.inputMode.collectAsStateWithLifecycle().value
    val mentionPanelState = vm.mentionPanelState.collectAsStateWithLifecycle().value
    val clipboard = LocalClipboardManager.current

    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var spans by remember { mutableStateOf<List<InputSpan>>(emptyList()) }

    DiscussionScreen(
        header = header,
        comments = messages,
        onBackClicked = onBackClicked,
        inputText = inputText,
        spans = spans,
        mentionPanelState = mentionPanelState,
        onInputValueChange = { newText, newSpans ->
            inputText = newText
            spans = newSpans
            vm.onInputChanged(
                selection = newText.selection.start..newText.selection.end,
                text = newText.text
            )
        },
        onSendClicked = { text, currentSpans ->
            val marks = currentSpans.mapNotNull { span ->
                when (span) {
                    is InputSpan.Mention -> {
                        Block.Content.Text.Mark(
                            type = Block.Content.Text.Mark.Type.MENTION,
                            param = span.param,
                            range = span.start..span.end
                        )
                    }
                    is InputSpan.Markup -> {
                        val type = when (span.type) {
                            InputSpan.Markup.BOLD -> Block.Content.Text.Mark.Type.BOLD
                            InputSpan.Markup.ITALIC -> Block.Content.Text.Mark.Type.ITALIC
                            InputSpan.Markup.STRIKETHROUGH -> Block.Content.Text.Mark.Type.STRIKETHROUGH
                            InputSpan.Markup.CODE -> Block.Content.Text.Mark.Type.KEYBOARD
                            InputSpan.Markup.UNDERLINE -> Block.Content.Text.Mark.Type.UNDERLINE
                            else -> null
                        }
                        if (type != null) {
                            Block.Content.Text.Mark(
                                type = type,
                                range = span.start..span.end
                            )
                        } else {
                            null
                        }
                    }
                }
            }
            vm.onSendComment(text, marks)
            inputText = TextFieldValue("")
            spans = emptyList()
        },
        inputMode = inputMode,
        onReplyComment = { vm.onReplyComment(it) },
        onReplyToReply = { vm.onReplyToReply(it) },
        onCopyText = { clipboard.setText(AnnotatedString(it)) },
        onClearReply = { vm.onClearReply() },
        onDeleteComment = { vm.onDeleteComment(it) },
        onAddReaction = { vm.onAddReaction(it) },
        onToggleReaction = { msg, emoji -> vm.onToggleReaction(msg, emoji) },
        onPlusClicked = onPlusClicked,
        onMentionClicked = { id -> vm.onMentionClicked(id) },
        onLinkClicked = { url ->
            // Link clicks can be handled by the fragment via commands if needed
        },
        onMentionMemberClicked = { member ->
            val state = mentionPanelState
            if (state is MentionPanelState.Visible) {
                val query = state.query
                val input = inputText.text

                val replacementText = member.name + " "
                val lengthDifference =
                    replacementText.length - (query.range.last - query.range.first + 1)

                val updatedText = input.replaceRange(query.range, replacementText)

                val updatedSpans = spans.map { span ->
                    if (span.start > query.range.last) {
                        when (span) {
                            is InputSpan.Mention -> span.copy(
                                start = span.start + lengthDifference,
                                end = span.end + lengthDifference
                            )
                            is InputSpan.Markup -> span.copy(
                                start = span.start + lengthDifference,
                                end = span.end + lengthDifference
                            )
                        }
                    } else {
                        span
                    }
                }

                val mentionSpan = InputSpan.Mention(
                    start = query.range.start,
                    end = query.range.start + member.name.length,
                    style = SpanStyle(textDecoration = TextDecoration.Underline),
                    param = member.id
                )

                spans = updatedSpans + mentionSpan
                inputText = inputText.copy(
                    text = updatedText,
                    selection = TextRange(
                        index = query.range.start + replacementText.length
                    )
                )
                vm.onInputChanged(
                    selection = inputText.selection.start..inputText.selection.end,
                    text = inputText.text
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionScreen(
    header: DiscussionHeader,
    comments: List<DiscussionView>,
    onBackClicked: () -> Unit,
    inputText: TextFieldValue = TextFieldValue(""),
    spans: List<InputSpan> = emptyList(),
    mentionPanelState: MentionPanelState = MentionPanelState.Hidden,
    onInputValueChange: (TextFieldValue, List<InputSpan>) -> Unit = { _, _ -> },
    onSendClicked: (String, List<InputSpan>) -> Unit = { _, _ -> },
    inputMode: DiscussionInputMode = DiscussionInputMode.Default,
    onReplyComment: (DiscussionView.Comment) -> Unit = {},
    onReplyToReply: (DiscussionView.Reply) -> Unit = {},
    onCopyText: (String) -> Unit = {},
    onClearReply: () -> Unit = {},
    onDeleteComment: (Id) -> Unit = {},
    onAddReaction: (Id) -> Unit = {},
    onToggleReaction: (Id, String) -> Unit = { _, _ -> },
    onMentionMemberClicked: (MentionPanelState.Member) -> Unit = {},
    onPlusClicked: () -> Unit = {},
    onMentionClicked: (Id) -> Unit = {},
    onLinkClicked: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background_primary))
    ) {
        DiscussionTopBar(
            header = header,
            onBackClicked = onBackClicked,
            modifier = Modifier.statusBarsPadding()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            DiscussionCommentList(
                comments = comments,
                modifier = Modifier.fillMaxSize(),
                onReplyComment = onReplyComment,
                onReplyToReply = onReplyToReply,
                onCopyText = onCopyText,
                onDeleteComment = onDeleteComment,
                onAddReaction = onAddReaction,
                onToggleReaction = onToggleReaction,
                onMentionClicked = onMentionClicked,
                onLinkClicked = onLinkClicked
            )
            Column(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                if (mentionPanelState is MentionPanelState.Visible) {
                    DiscussionMentionPanel(
                        state = mentionPanelState,
                        onMemberClicked = onMentionMemberClicked
                    )
                }
                if (inputMode is DiscussionInputMode.Reply) {
                    DiscussionReplyBanner(
                        mode = inputMode,
                        onClearReply = onClearReply
                    )
                }
                DiscussionCommentInput(
                    text = inputText,
                    spans = spans,
                    onValueChange = onInputValueChange,
                    onSendClicked = onSendClicked,
                    onPlusClicked = onPlusClicked,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .imePadding()
                )
            }
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
    onCopyText: (String) -> Unit = {},
    onDeleteComment: (Id) -> Unit = {},
    onAddReaction: (Id) -> Unit = {},
    onToggleReaction: (Id, String) -> Unit = { _, _ -> },
    onMentionClicked: (Id) -> Unit = {},
    onLinkClicked: (String) -> Unit = {}
) {
    val listState = rememberLazyListState()

    LaunchedEffect(comments.isNotEmpty()) {
        if (comments.isNotEmpty()) {
            listState.scrollToItem(comments.lastIndex)
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(bottom = 120.dp)
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
                        onCopy = { onCopyText(item.content.msg) },
                        onDelete = { onDeleteComment(item.id) },
                        onAddReaction = { onAddReaction(item.id) },
                        onToggleReaction = { emoji -> onToggleReaction(item.id, emoji) },
                        onMentionClicked = onMentionClicked,
                        onLinkClicked = onLinkClicked
                    )
                }
                is DiscussionView.Reply -> {
                    DiscussionReplyItem(
                        reply = item,
                        onReply = { onReplyToReply(item) },
                        onCopy = { onCopyText(item.content.msg) },
                        onDelete = { onDeleteComment(item.id) },
                        onAddReaction = { onAddReaction(item.id) },
                        onToggleReaction = { emoji -> onToggleReaction(item.id, emoji) },
                        onMentionClicked = onMentionClicked,
                        onLinkClicked = onLinkClicked
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiscussionCommentItem(
    comment: DiscussionView.Comment,
    onReply: () -> Unit = {},
    onCopy: () -> Unit = {},
    onDelete: () -> Unit = {},
    onAddReaction: () -> Unit = {},
    onToggleReaction: (String) -> Unit = {},
    onMentionClicked: (Id) -> Unit = {},
    onLinkClicked: (String) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showDeleteWarning by remember { mutableStateOf(false) }

    if (showDeleteWarning) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteWarning = false },
            containerColor = colorResource(id = R.color.background_secondary),
            shape = RoundedCornerShape(16.dp),
            dragHandle = null,
            modifier = Modifier.padding(bottom = 32.dp, start = 12.dp, end = 12.dp)
        ) {
            GenericAlert(
                config = AlertConfig.WithTwoButtons(
                    title = stringResource(com.anytypeio.anytype.localization.R.string.chats_alert_delete_this_message),
                    description = stringResource(com.anytypeio.anytype.localization.R.string.chats_alert_delete_this_message_description),
                    firstButtonText = stringResource(com.anytypeio.anytype.localization.R.string.cancel),
                    secondButtonText = stringResource(com.anytypeio.anytype.localization.R.string.delete),
                    secondButtonType = BUTTON_WARNING,
                    firstButtonType = BUTTON_SECONDARY,
                    icon = R.drawable.ic_popup_question_56
                ),
                onFirstButtonClicked = { showDeleteWarning = false },
                onSecondButtonClicked = { onDelete() },
                addBottomSpacer = false
            )
        }
    }

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
                RichTextContent(
                    parts = comment.content.parts,
                    onMentionClicked = onMentionClicked,
                    onLinkClicked = onLinkClicked
                )
            }
            // Reactions
            if (comment.reactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ReactionsRow(
                    reactions = comment.reactions,
                    onToggleReaction = onToggleReaction
                )
            }
        }
        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(
                medium = RoundedCornerShape(16.dp)
            ),
            colors = MaterialTheme.colors.copy(
                surface = colorResource(id = R.color.background_secondary)
            )
        ) {
            DropdownMenu(
                expanded = showDropdownMenu,
                onDismissRequest = { showDropdownMenu = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = com.anytypeio.anytype.localization.R.string.chats_add_reaction),
                            style = PreviewTitle1Regular,
                            color = colorResource(id = R.color.text_primary)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dropdown_menu_mood),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = colorResource(id = R.color.text_primary)
                        )
                    },
                    onClick = {
                        showDropdownMenu = false
                        onAddReaction()
                    }
                )
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
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dropdown_menu_reply),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = colorResource(id = R.color.text_primary)
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
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_dropdown_menu_content_copy),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = colorResource(id = R.color.text_primary)
                            )
                        }
                    )
                }
                if (comment.isOwn) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = com.anytypeio.anytype.localization.R.string.delete),
                                style = PreviewTitle1Regular,
                                color = colorResource(id = R.color.palette_system_red)
                            )
                        },
                        onClick = {
                            showDropdownMenu = false
                            showDeleteWarning = true
                        },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_dropdown_menu_delete),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = colorResource(id = R.color.palette_system_red)
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiscussionReplyItem(
    reply: DiscussionView.Reply,
    onReply: () -> Unit = {},
    onCopy: () -> Unit = {},
    onDelete: () -> Unit = {},
    onAddReaction: () -> Unit = {},
    onToggleReaction: (String) -> Unit = {},
    onMentionClicked: (Id) -> Unit = {},
    onLinkClicked: (String) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showDeleteWarning by remember { mutableStateOf(false) }

    if (showDeleteWarning) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteWarning = false },
            containerColor = colorResource(id = R.color.background_secondary),
            shape = RoundedCornerShape(16.dp),
            dragHandle = null,
            modifier = Modifier.padding(bottom = 32.dp, start = 12.dp, end = 12.dp)
        ) {
            GenericAlert(
                config = AlertConfig.WithTwoButtons(
                    title = stringResource(com.anytypeio.anytype.localization.R.string.chats_alert_delete_this_message),
                    description = stringResource(com.anytypeio.anytype.localization.R.string.chats_alert_delete_this_message_description),
                    firstButtonText = stringResource(com.anytypeio.anytype.localization.R.string.cancel),
                    secondButtonText = stringResource(com.anytypeio.anytype.localization.R.string.delete),
                    secondButtonType = BUTTON_WARNING,
                    firstButtonType = BUTTON_SECONDARY,
                    icon = R.drawable.ic_popup_question_56
                ),
                onFirstButtonClicked = { showDeleteWarning = false },
                onSecondButtonClicked = { onDelete() },
                addBottomSpacer = false
            )
        }
    }

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
                    RichTextContent(
                        parts = reply.content.parts,
                        onMentionClicked = onMentionClicked,
                        onLinkClicked = onLinkClicked
                    )
                }
                // Reactions
                if (reply.reactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ReactionsRow(
                        reactions = reply.reactions,
                        onToggleReaction = onToggleReaction
                    )
                }
            }
        }
        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(
                medium = RoundedCornerShape(16.dp)
            ),
            colors = MaterialTheme.colors.copy(
                surface = colorResource(id = R.color.background_secondary)
            )
        ) {
            DropdownMenu(
                expanded = showDropdownMenu,
                onDismissRequest = { showDropdownMenu = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = com.anytypeio.anytype.localization.R.string.chats_add_reaction),
                            style = PreviewTitle1Regular,
                            color = colorResource(id = R.color.text_primary)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dropdown_menu_mood),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = colorResource(id = R.color.text_primary)
                        )
                    },
                    onClick = {
                        showDropdownMenu = false
                        onAddReaction()
                    }
                )
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
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dropdown_menu_reply),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = colorResource(id = R.color.text_primary)
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
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_dropdown_menu_content_copy),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = colorResource(id = R.color.text_primary)
                            )
                        }
                    )
                }
                if (reply.isOwn) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = com.anytypeio.anytype.localization.R.string.delete),
                                style = PreviewTitle1Regular,
                                color = colorResource(id = R.color.palette_system_red)
                            )
                        },
                        onClick = {
                            showDropdownMenu = false
                            showDeleteWarning = true
                        },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_dropdown_menu_delete),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = colorResource(id = R.color.palette_system_red)
                            )
                        }
                    )
                }
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
fun RichTextContent(
    parts: List<DiscussionView.Content.Part>,
    onMentionClicked: (Id) -> Unit = {},
    onLinkClicked: (String) -> Unit = {}
) {
    val annotatedString = buildAnnotatedString {
        parts.forEach { part ->
            if (part.mention?.param != null) {
                withLink(
                    LinkAnnotation.Clickable(
                        tag = MENTION_SPAN_TAG,
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
            } else if (part.link?.param != null) {
                withLink(
                    LinkAnnotation.Clickable(
                        tag = MENTION_LINK_TAG,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                fontWeight = if (part.isBold) FontWeight.Bold else null,
                                fontStyle = if (part.isItalic) FontStyle.Italic else null,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    ) {
                        onLinkClicked(part.link.param.orEmpty())
                    }
                ) {
                    append(part.part)
                }
            } else {
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
    }
    Text(
        text = annotatedString,
        fontSize = 15.sp,
        color = colorResource(id = R.color.text_primary)
    )
}

private const val MENTION_SPAN_TAG = "@-mention"
private const val MENTION_LINK_TAG = "link"

@Composable
fun ReactionsRow(
    reactions: List<DiscussionView.Reaction>,
    onToggleReaction: (String) -> Unit = {}
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        reactions.forEach { reaction ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onToggleReaction(reaction.emoji) }
                    .background(
                        color = if (reaction.isSelected)
                            colorResource(id = R.color.palette_very_light_orange)
                        else
                            colorResource(id = R.color.shape_transparent_primary),
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
