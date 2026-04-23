package com.anytypeio.anytype.feature_discussions.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.BUTTON_SECONDARY
import com.anytypeio.anytype.core_ui.foundation.BUTTON_WARNING
import com.anytypeio.anytype.core_ui.foundation.GenericAlert
import com.anytypeio.anytype.core_ui.text.InputSpan
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.CodeBlock
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.feature_discussions.presentation.CommentAttachment
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionHeader
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionInputMode
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionView
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel.MentionPanelState
import timber.log.Timber

@Composable
fun DiscussionScreenWrapper(
    vm: DiscussionViewModel,
    onBackClicked: () -> Unit
) {
    val header = vm.header.collectAsStateWithLifecycle().value
    val messages = vm.messages.collectAsStateWithLifecycle().value
    val inputMode = vm.inputMode.collectAsStateWithLifecycle().value
    val mentionPanelState = vm.mentionPanelState.collectAsStateWithLifecycle().value
    val attachments = vm.commentAttachments.collectAsStateWithLifecycle().value
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var spans by remember { mutableStateOf<List<InputSpan>>(emptyList()) }

    DiscussionScreen(
        header = header,
        comments = messages,
        onBackClicked = onBackClicked,
        inputText = inputText,
        spans = spans,
        attachments = attachments,
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
        onMediaPicked = { vm.onCommentMediaPicked(it) },
        onFilePicked = { uris ->
            val infos = uris.mapNotNull { uri ->
                val cursor = context.contentResolver.query(
                    uri, null, null, null, null
                )
                if (cursor != null) {
                    cursor.use { c ->
                        val nameIndex = c.getColumnIndex(
                            android.provider.OpenableColumns.DISPLAY_NAME
                        )
                        val sizeIndex = c.getColumnIndex(
                            android.provider.OpenableColumns.SIZE
                        )
                        c.moveToFirst()
                        com.anytypeio.anytype.core_utils.common.DefaultFileInfo(
                            uri = uri.toString(),
                            name = c.getString(nameIndex),
                            size = c.getLong(sizeIndex).toInt()
                        )
                    }
                } else {
                    null
                }
            }
            vm.onCommentFilePicked(infos)
        },
        onClearAttachment = { vm.onClearAttachment(it) },
        onMentionClicked = { id -> vm.onMentionClicked(id) },
        onLinkClicked = { url -> vm.onLinkClicked(url) },
        onContentBlockClicked = { block -> vm.onContentBlockClicked(block) },
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
    attachments: List<CommentAttachment> = emptyList(),
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
    onMediaPicked: (List<DiscussionViewModel.MediaUri>) -> Unit = {},
    onFilePicked: (List<android.net.Uri>) -> Unit = {},
    onClearAttachment: (CommentAttachment) -> Unit = {},
    onMentionClicked: (Id) -> Unit = {},
    onLinkClicked: (String) -> Unit = {},
    onContentBlockClicked: (DiscussionView.ContentBlock) -> Unit = {}
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
                onLinkClicked = onLinkClicked,
                onContentBlockClicked = onContentBlockClicked
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
                    attachments = attachments,
                    onValueChange = onInputValueChange,
                    onSendClicked = onSendClicked,
                    onMediaPicked = onMediaPicked,
                    onFilePicked = onFilePicked,
                    onClearAttachment = onClearAttachment,
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
    onLinkClicked: (String) -> Unit = {},
    onContentBlockClicked: (DiscussionView.ContentBlock) -> Unit = {}
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
                        onLinkClicked = onLinkClicked,
                        onContentBlockClicked = onContentBlockClicked
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
                        onLinkClicked = onLinkClicked,
                        onContentBlockClicked = onContentBlockClicked
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
    onLinkClicked: (String) -> Unit = {},
    onContentBlockClicked: (DiscussionView.ContentBlock) -> Unit = {}
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
            // Content blocks
            if (comment.contentBlocks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                ContentBlocksList(
                    blocks = comment.contentBlocks,
                    onMentionClicked = onMentionClicked,
                    onLinkClicked = onLinkClicked,
                    onContentBlockClicked = onContentBlockClicked
                )
            } else if (comment.content.msg.isNotEmpty()) {
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
                offset = DpOffset(8.dp, 8.dp),
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
    onLinkClicked: (String) -> Unit = {},
    onContentBlockClicked: (DiscussionView.ContentBlock) -> Unit = {}
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
                // Content blocks
                if (reply.contentBlocks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    ContentBlocksList(
                        blocks = reply.contentBlocks,
                        onMentionClicked = onMentionClicked,
                        onLinkClicked = onLinkClicked,
                        onContentBlockClicked = onContentBlockClicked
                    )
                } else if (reply.content.msg.isNotEmpty()) {
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
                offset = DpOffset(8.dp, 8.dp),
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
            val context = LocalContext.current
            val model = remember(avatar.hash) {
                ImageRequest.Builder(context)
                    .data(avatar.hash)
                    .size(128, 128)
                    .diskCachePolicy(coil3.request.CachePolicy.ENABLED)
                    .memoryCachePolicy(coil3.request.CachePolicy.ENABLED)
                    .crossfade(true)
                    .build()
            }
            AsyncImage(
                model = model,
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
fun ContentBlocksList(
    blocks: List<DiscussionView.ContentBlock>,
    onMentionClicked: (Id) -> Unit = {},
    onLinkClicked: (String) -> Unit = {},
    onContentBlockClicked: (DiscussionView.ContentBlock) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        blocks.forEach { block ->
            when (block) {
                is DiscussionView.ContentBlock.Text -> {
                    if (block.content.msg.isNotEmpty()) {
                        when (block.style) {
                            Block.Content.Text.Style.BULLET -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "•",
                                        fontSize = 15.sp,
                                        color = colorResource(id = R.color.text_primary),
                                        modifier = Modifier.width(24.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    RichTextContent(
                                        parts = block.content.parts,
                                        onMentionClicked = onMentionClicked,
                                        onLinkClicked = onLinkClicked,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Block.Content.Text.Style.NUMBERED -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${block.number}.",
                                        fontSize = 15.sp,
                                        color = colorResource(id = R.color.text_primary),
                                        modifier = Modifier.defaultMinSize(minWidth = 24.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    RichTextContent(
                                        parts = block.content.parts,
                                        onMentionClicked = onMentionClicked,
                                        onLinkClicked = onLinkClicked,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Block.Content.Text.Style.CHECKBOX -> {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (block.checked)
                                                R.drawable.ic_checkbox_selected
                                            else
                                                R.drawable.ic_checkbox_default
                                        ),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    RichTextContent(
                                        parts = block.content.parts,
                                        onMentionClicked = onMentionClicked,
                                        onLinkClicked = onLinkClicked,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Block.Content.Text.Style.QUOTE -> {
                                Row(
                                    modifier = Modifier.height(IntrinsicSize.Min)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .fillMaxHeight()
                                            .background(
                                                colorResource(id = R.color.block_highlight_divider)
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    RichTextContent(
                                        parts = block.content.parts,
                                        onMentionClicked = onMentionClicked,
                                        onLinkClicked = onLinkClicked,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Block.Content.Text.Style.CODE_SNIPPET -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = colorResource(id = R.color.shape_tertiary),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    RichTextContent(
                                        parts = block.content.parts,
                                        style = block.style,
                                        onMentionClicked = onMentionClicked,
                                        onLinkClicked = onLinkClicked
                                    )
                                }
                            }
                            else -> {
                                RichTextContent(
                                    parts = block.content.parts,
                                    style = block.style,
                                    onMentionClicked = onMentionClicked,
                                    onLinkClicked = onLinkClicked
                                )
                            }
                        }
                    }
                }
                is DiscussionView.ContentBlock.Image -> {
                    val context = LocalContext.current
                    val model = remember(block.url) {
                        ImageRequest.Builder(context)
                            .data(block.url)
                            .size(1024, 1024)
                            .diskCachePolicy(coil3.request.CachePolicy.ENABLED)
                            .memoryCachePolicy(coil3.request.CachePolicy.ENABLED)
                            .crossfade(true)
                            .build()
                    }
                    AsyncImage(
                        model = model,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = colorResource(id = R.color.shape_transparent_secondary),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onContentBlockClicked(block) },
                        contentScale = ContentScale.Crop
                    )
                }
                is DiscussionView.ContentBlock.Link -> {
                    AttachedObjectCard(
                        title = block.title,
                        typeName = block.typeName,
                        icon = block.icon,
                        onClicked = { onContentBlockClicked(block) }
                    )
                }
                is DiscussionView.ContentBlock.Bookmark -> {
                    BookmarkCard(
                        url = block.url,
                        title = block.title,
                        description = block.description,
                        imageUrl = block.imageUrl,
                        onClicked = { onContentBlockClicked(block) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AttachedObjectCard(
    title: String,
    typeName: String,
    icon: ObjectIcon,
    onClicked: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.shape_transparent_secondary),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = colorResource(id = R.color.background_secondary)
            )
            .clickable(onClick = onClicked)
    ) {
        ListWidgetObjectIcon(
            icon = icon,
            iconSize = 48.dp,
            modifier = Modifier
                .padding(start = 12.dp)
                .align(alignment = Alignment.CenterStart),
            onTaskIconClicked = {}
        )
        Text(
            text = title.ifEmpty { stringResource(R.string.untitled) },
            modifier = Modifier.padding(
                start = if (icon != ObjectIcon.None) 72.dp else 12.dp,
                top = 17.5.dp,
                end = 12.dp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = PreviewTitle2Medium,
            color = colorResource(id = R.color.text_primary)
        )
        Text(
            text = typeName.ifEmpty { stringResource(R.string.unknown_type) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = if (icon != ObjectIcon.None) 72.dp else 12.dp,
                    bottom = 17.5.dp,
                    end = 12.dp
                ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = Relations3,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}

@Composable
fun BookmarkCard(
    url: String,
    title: String,
    description: String,
    imageUrl: String?,
    onClicked: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color = colorResource(id = R.color.shape_transparent_secondary))
            .clickable(onClick = onClicked)
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            val painter = rememberAsyncImagePainter(imageUrl)
            Box {
                if (painter.state is AsyncImagePainter.State.Loading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier
                            .align(alignment = Alignment.Center)
                            .size(48.dp),
                        color = colorResource(R.color.glyph_active),
                        trackColor = colorResource(R.color.glyph_active).copy(alpha = 0.5f),
                        strokeWidth = 4.dp
                    )
                }
                Image(
                    painter = painter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.91f),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = url,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = Relations3,
            color = colorResource(R.color.transparent_active),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = Title2,
            color = colorResource(R.color.text_primary),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = Relations3,
            color = colorResource(R.color.transparent_active),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun RichTextContent(
    parts: List<DiscussionView.Content.Part>,
    style: Block.Content.Text.Style = Block.Content.Text.Style.P,
    onMentionClicked: (Id) -> Unit = {},
    onLinkClicked: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val resources = LocalContext.current.resources
    val textStyle = when (style) {
        Block.Content.Text.Style.H1 -> HeadlineTitle
        Block.Content.Text.Style.H2 -> HeadlineHeading
        Block.Content.Text.Style.H3 -> HeadlineSubheading
        Block.Content.Text.Style.CODE_SNIPPET -> CodeBlock
        else -> null
    }
    val annotatedString = buildAnnotatedString {
        parts.forEach { part ->
            val textColor = part.resolveTextColor(resources)
            val bgColor = part.resolveBackgroundColor(resources)
            if (part.mention?.param != null) {
                withLink(
                    LinkAnnotation.Clickable(
                        tag = MENTION_SPAN_TAG,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = textColor ?: Color.Unspecified,
                                background = bgColor ?: Color.Unspecified,
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
                                color = textColor ?: Color.Unspecified,
                                background = bgColor ?: Color.Unspecified,
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
                val spanStyle = SpanStyle(
                    color = textColor ?: Color.Unspecified,
                    background = bgColor ?: Color.Unspecified,
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
                withStyle(spanStyle) {
                    append(part.part)
                }
            }
        }
    }
    Text(
        text = annotatedString,
        style = textStyle ?: TextStyle.Default,
        fontSize = if (textStyle == null) 15.sp else TextStyle.Default.fontSize,
        color = colorResource(id = R.color.text_primary),
        modifier = modifier
    )
}

private const val MENTION_SPAN_TAG = "@-mention"
private const val MENTION_LINK_TAG = "link"

private fun DiscussionView.Content.Part.resolveTextColor(
    resources: android.content.res.Resources
): Color? {
    val param = textColor?.param ?: return null
    val theme = ThemeColor.entries.find { it.code == param } ?: return null
    if (theme == ThemeColor.DEFAULT) return null
    return Color(resources.dark(theme))
}

private fun DiscussionView.Content.Part.resolveBackgroundColor(
    resources: android.content.res.Resources
): Color? {
    val param = backgroundColor?.param ?: return null
    val theme = ThemeColor.entries.find { it.code == param } ?: return null
    if (theme == ThemeColor.DEFAULT) return null
    return Color(resources.light(theme))
}

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
