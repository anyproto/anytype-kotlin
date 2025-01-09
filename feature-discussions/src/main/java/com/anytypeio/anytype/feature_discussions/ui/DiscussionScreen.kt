package com.anytypeio.anytype.feature_discussions.ui

import android.content.res.Configuration
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.AlertIcon
import com.anytypeio.anytype.core_ui.foundation.BUTTON_SECONDARY
import com.anytypeio.anytype.core_ui.foundation.BUTTON_WARNING
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_BLUE
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_RED
import com.anytypeio.anytype.core_ui.foundation.GenericAlert
import com.anytypeio.anytype.core_ui.foundation.Warning
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.fontIBM
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_utils.common.DefaultFileInfo
import com.anytypeio.anytype.core_utils.const.DateConst.TIME_H24
import com.anytypeio.anytype.core_utils.ext.formatTimeInMillis
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.feature_discussions.R
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionView
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel.ChatBoxMode
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel.UXCommand
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Async


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionScreenWrapper(
    isSpaceLevelChat: Boolean = false,
    vm: DiscussionViewModel,
    // TODO move to view model
    onAttachObjectClicked: () -> Unit,
    onBackButtonClicked: () -> Unit,
    onMarkupLinkClicked: (String) -> Unit,
    onRequestOpenFullScreenImage: (String) -> Unit,
    onSelectChatReaction: (String) -> Unit,
    onViewChatReaction: (Id, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showReactionSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    NavHost(
        navController = rememberNavController(),
        startDestination = "discussions"
    ) {
        composable(
            route = "discussions"
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (!isSpaceLevelChat) {
                            Modifier.background(
                                color = colorResource(id = R.color.background_primary)
                            )
                        } else {
                            Modifier
                        }
                    )
            ) {
                val clipboard = LocalClipboardManager.current
                val lazyListState = rememberLazyListState()
                DiscussionScreen(
                    chatBoxMode = vm.chatBoxMode.collectAsState().value,
                    isSpaceLevelChat = isSpaceLevelChat,
                    title = vm.name.collectAsState().value,
                    messages = vm.messages.collectAsState().value,
                    attachments = vm.chatBoxAttachments.collectAsState().value,
                    onMessageSent = vm::onMessageSent,
                    onTitleChanged = vm::onTitleChanged,
                    onAttachClicked = onAttachObjectClicked,
                    onClearAttachmentClicked = vm::onClearAttachmentClicked,
                    lazyListState = lazyListState,
                    onReacted = vm::onReacted,
                    onCopyMessage = { msg ->
                        clipboard.setText(AnnotatedString(text = msg.content.msg))
                    },
                    onDeleteMessage = vm::onDeleteMessage,
                    onEditMessage = vm::onRequestEditMessageClicked,
                    onAttachmentClicked = vm::onAttachmentClicked,
                    isInEditMessageMode = vm.chatBoxMode.collectAsState().value is ChatBoxMode.EditMessage,
                    onExitEditMessageMode = vm::onExitEditMessageMode,
                    onBackButtonClicked = onBackButtonClicked,
                    onMarkupLinkClicked = onMarkupLinkClicked,
                    onAttachObjectClicked = onAttachObjectClicked,
                    onAttachMediaClicked = {

                    },
                    onAttachFileClicked = {

                    },
                    onUploadAttachmentClicked = {

                    },
                    onReplyMessage = vm::onReplyMessage,
                    onClearReplyClicked = vm::onClearReplyClicked,
                    onChatBoxMediaPicked = { uris ->
                        vm.onChatBoxMediaPicked(uris.map { it.parseImagePath(context = context) })
                    },
                    onChatBoxFilePicked = { uris ->
                        val infos = uris.mapNotNull { uri ->
                            val cursor = context.contentResolver.query(
                                uri,
                                null,
                                null,
                                null,
                                null
                            )
                            if (cursor != null) {
                                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                                cursor.moveToFirst()
                                DefaultFileInfo(
                                    uri = uri.toString(),
                                    name = cursor.getString(nameIndex),
                                    size = cursor.getLong(sizeIndex).toInt()
                                )
                            } else {
                                null
                            }
                        }
                        vm.onChatBoxFilePicked(infos)
                    },
                    onAddReactionClicked = onSelectChatReaction,
                    onViewChatReaction = onViewChatReaction
                )
                LaunchedEffect(Unit) {
                    vm.commands.collect { command ->
                        when(command) {
                            is UXCommand.JumpToBottom -> {
                                lazyListState.animateScrollToItem(0)
                            }
                            is UXCommand.SetChatBoxInput -> {
                                // TODO
                            }
                            is UXCommand.OpenFullScreenImage -> {
                                onRequestOpenFullScreenImage(command.url)
                            }
                        }
                    }
                }
            }
        }
    }
    if (showReactionSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showReactionSheet = false
            },
            sheetState = sheetState,
            containerColor = colorResource(id = R.color.background_secondary),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = null
        ) {
            SelectChatReactionScreen(
                onEmojiClicked = {}
            )
        }
    }
}

/**
 * TODO: do date formating before rendering?
 */
@Composable
fun DiscussionScreen(
    chatBoxMode: ChatBoxMode,
    isSpaceLevelChat: Boolean,
    isInEditMessageMode: Boolean = false,
    lazyListState: LazyListState,
    title: String?,
    messages: List<DiscussionView>,
    attachments: List<DiscussionView.Message.ChatBoxAttachment>,
    onMessageSent: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onAttachClicked: () -> Unit,
    onBackButtonClicked: () -> Unit,
    onClearAttachmentClicked: (DiscussionView.Message.ChatBoxAttachment) -> Unit,
    onClearReplyClicked: () -> Unit,
    onReacted: (Id, String) -> Unit,
    onDeleteMessage: (DiscussionView.Message) -> Unit,
    onCopyMessage: (DiscussionView.Message) -> Unit,
    onEditMessage: (DiscussionView.Message) -> Unit,
    onReplyMessage: (DiscussionView.Message) -> Unit,
    onAttachmentClicked: (DiscussionView.Message.Attachment) -> Unit,
    onExitEditMessageMode: () -> Unit,
    onMarkupLinkClicked: (String) -> Unit,
    onAttachObjectClicked: () -> Unit,
    onAttachMediaClicked: () -> Unit,
    onAttachFileClicked: () -> Unit,
    onUploadAttachmentClicked: () -> Unit,
    onChatBoxMediaPicked: (List<Uri>) -> Unit,
    onChatBoxFilePicked: (List<Uri>) -> Unit,
    onAddReactionClicked: (String) -> Unit,
    onViewChatReaction: (Id, String) -> Unit
) {
    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var isTitleFocused by remember { mutableStateOf(false) }
    val chatBoxFocusRequester = FocusRequester()
    val isHeaderVisible by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                false
            } else {
                visibleItems.last().key == HEADER_KEY
            }
        }
    }
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (!isSpaceLevelChat) {
            TopDiscussionToolbar(
                title = title,
                isHeaderVisible = isHeaderVisible
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            Messages(
                isSpaceLevelChat = isSpaceLevelChat,
                modifier = Modifier.fillMaxSize(),
                messages = messages,
                scrollState = lazyListState,
                onTitleChanged = onTitleChanged,
                title = title,
                onTitleFocusChanged = {
                    isTitleFocused = it
                },
                onReacted = onReacted,
                onCopyMessage = onCopyMessage,
                onDeleteMessage = onDeleteMessage,
                onAttachmentClicked = onAttachmentClicked,
                onEditMessage = { msg ->
                    onEditMessage(msg).also {
                        textState = TextFieldValue(
                            msg.content.msg,
                            selection = TextRange(msg.content.msg.length)
                        )
                        chatBoxFocusRequester.requestFocus()
                    }
                },
                onReplyMessage = {
                    onReplyMessage(it)
                    chatBoxFocusRequester.requestFocus()
                },
                onMarkupLinkClicked = onMarkupLinkClicked,
                onAddReactionClicked = onAddReactionClicked,
                onViewChatReaction = onViewChatReaction
            )
            // Jump to bottom button shows up when user scrolls past a threshold.
            // Convert to pixels:
            val jumpThreshold = with(LocalDensity.current) {
                JumpToBottomThreshold.toPx()
            }

            // Show the button if the first visible item is not the first one or if the offset is
            // greater than the threshold.
            val jumpToBottomButtonEnabled by remember {
                derivedStateOf {
                    lazyListState.firstVisibleItemIndex != 0 ||
                            lazyListState.firstVisibleItemScrollOffset > jumpThreshold
                }
            }

            GoToBottomButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(end = 12.dp),
                onGoToBottomClicked = {
                    scope.launch {
                        lazyListState.animateScrollToItem(index = 0)
                    }
                },
                enabled = jumpToBottomButtonEnabled
            )
        }
        if (isInEditMessageMode) {
            EditMessageToolbar(
                onExitClicked = {
                    onExitEditMessageMode().also {
                        textState = TextFieldValue()
                    }
                }
            )
        }

        ChatBox(
            mode = chatBoxMode,
            modifier = Modifier
                .imePadding()
                .navigationBarsPadding(),
            chatBoxFocusRequester = chatBoxFocusRequester,
            textState = textState,
            onMessageSent = onMessageSent,
            onAttachClicked = onAttachClicked,
            resetScroll = {
                scope.launch {
                    lazyListState.animateScrollToItem(index = 0)
                }
            },
            isTitleFocused = isTitleFocused,
            attachments = attachments,
            updateValue = {
                textState = it
            },
            clearText = {
                textState = TextFieldValue()
            },
            onBackButtonClicked = onBackButtonClicked,
            onAttachFileClicked = onAttachFileClicked,
            onAttachMediaClicked = onAttachMediaClicked,
            onUploadAttachmentClicked = onUploadAttachmentClicked,
            onAttachObjectClicked = onAttachObjectClicked,
            onClearAttachmentClicked = onClearAttachmentClicked,
            onClearReplyClicked = onClearReplyClicked,
            onChatBoxMediaPicked = onChatBoxMediaPicked,
            onChatBoxFilePicked = onChatBoxFilePicked
        )
    }
}

@Composable
private fun DiscussionTitle(
    title: String?,
    onTitleChanged: (String) -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {}
) {
    var lastFocusState by remember { mutableStateOf(false) }
    BasicTextField(
        textStyle = HeadlineTitle.copy(
            color = colorResource(id = R.color.text_primary)
        ),
        value = title.orEmpty(),
        onValueChange = {
            onTitleChanged(it)
        },
        modifier = Modifier
            .padding(
                top = 24.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 8.dp
            )
            .onFocusChanged { state ->
                if (lastFocusState != state.isFocused) {
                    onFocusChanged(state.isFocused)
                }
                lastFocusState = state.isFocused
            }
        ,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        decorationBox = @Composable { innerTextField ->
            DefaultHintDecorationBox(
                hint = stringResource(id = R.string.untitled),
                text = title.orEmpty(),
                innerTextField = innerTextField,
                textStyle = HeadlineTitle
            )
        }
    )
}

@Composable
private fun ChatBox(
    mode: ChatBoxMode = ChatBoxMode.Default,
    modifier: Modifier = Modifier,
    onBackButtonClicked: () -> Unit,
    chatBoxFocusRequester: FocusRequester,
    textState: TextFieldValue,
    onMessageSent: (String) -> Unit = {},
    onAttachClicked: () -> Unit = {},
    resetScroll: () -> Unit = {},
    isTitleFocused: Boolean,
    attachments: List<DiscussionView.Message.ChatBoxAttachment>,
    clearText: () -> Unit,
    updateValue: (TextFieldValue) -> Unit,
    onAttachObjectClicked: () -> Unit,
    onAttachMediaClicked: () -> Unit,
    onAttachFileClicked: () -> Unit,
    onUploadAttachmentClicked: () -> Unit,
    onClearAttachmentClicked: (DiscussionView.Message.ChatBoxAttachment) -> Unit,
    onClearReplyClicked: () -> Unit,
    onChatBoxMediaPicked: (List<Uri>) -> Unit,
    onChatBoxFilePicked: (List<Uri>) -> Unit,
) {
    val uploadMediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) {
        onChatBoxMediaPicked(it)
    }

    val uploadFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
        onChatBoxFilePicked(it)
    }

    var showDropdownMenu by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val focus = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(
                start = 12.dp,
                end = 12.dp,
                bottom = 20.dp
            )
            .background(
                color = colorResource(R.color.navigation_panel),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            attachments.forEach { attachment ->
                when(attachment) {
                    is DiscussionView.Message.ChatBoxAttachment.Link -> {
                        item {
                            Box {
                                AttachedObject(
                                    modifier = Modifier
                                        .padding(
                                            top = 12.dp,
                                            end = 4.dp
                                        )
                                        .width(216.dp),
                                    title = attachment.wrapper.title,
                                    type = attachment.wrapper.type,
                                    icon = attachment.wrapper.icon,
                                    onAttachmentClicked = {
                                        // TODO
                                    }
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.ic_clear_chatbox_attachment),
                                    contentDescription = "Close icon",
                                    modifier = Modifier
                                        .align(
                                            Alignment.TopEnd
                                        )
                                        .padding(top = 6.dp)
                                        .noRippleClickable {
                                            onClearAttachmentClicked(attachment)
                                        }
                                )
                            }
                        }
                    }
                    is DiscussionView.Message.ChatBoxAttachment.Media -> {
                        item {
                            Box(modifier = Modifier.padding()) {
                                Image(
                                    painter = rememberAsyncImagePainter(attachment.uri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(
                                            top = 12.dp,
                                            end = 4.dp
                                        )
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp))

                                    ,
                                    contentScale = ContentScale.Crop
                                )
                                Image(
                                    painter = painterResource(R.drawable.ic_clear_chatbox_attachment),
                                    contentDescription = "Clear attachment icon",
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 6.dp)
                                        .noRippleClickable {
                                            onClearAttachmentClicked(attachment)
                                        }
                                )
                            }
                        }
                    }
                    is DiscussionView.Message.ChatBoxAttachment.File -> {
                        item {
                            Box {
                                AttachedObject(
                                    modifier = Modifier
                                        .padding(
                                            top = 12.dp,
                                            end = 4.dp
                                        )
                                        .width(216.dp),
                                    title = attachment.name,
                                    type = stringResource(R.string.file),
                                    icon = ObjectIcon.File(
                                        mime = null,
                                        fileName = null
                                    ),
                                    onAttachmentClicked = {
                                        // TODO
                                    }
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.ic_clear_chatbox_attachment),
                                    contentDescription = "Close icon",
                                    modifier = Modifier
                                        .align(
                                            Alignment.TopEnd
                                        )
                                        .padding(top = 6.dp)
                                        .noRippleClickable {
                                            onClearAttachmentClicked(attachment)
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
        when(mode) {
            is ChatBoxMode.Default -> {

            }
            is ChatBoxMode.EditMessage -> {

            }
            is ChatBoxMode.Reply -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "Reply to ${mode.author}",
                        modifier = Modifier.padding(
                            start = 12.dp,
                            top = 8.dp,
                            end = 44.dp
                        ),
                        style = Caption1Medium,
                        color = colorResource(R.color.text_primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = mode.text,
                        modifier = Modifier.padding(
                            start = 12.dp,
                            top = 28.dp,
                            end = 44.dp
                        ),
                        style = Caption1Regular,
                        color = colorResource(R.color.text_primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_chat_close_chat_box_reply),
                        contentDescription = "Clear reply to icon",
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .align(Alignment.CenterEnd)
                            .clickable {
                                onClearReplyClicked()
                            }
                    )
                }
            }
        }
        Row {
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .clip(CircleShape)
                    .align(Alignment.Bottom)
                    .clickable {
                        scope.launch {
                            focus.clearFocus(force = true)
                            onBackButtonClicked()
                        }
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_nav_panel_back),
                    contentDescription = "Back button",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
            ChatBoxUserInput(
                textState = textState,
                onMessageSent = {
                    onMessageSent(it)
                    clearText()
                    resetScroll()
                },
                onTextChanged = { value ->
                    updateValue(value)
                },
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Bottom)
                    .focusRequester(chatBoxFocusRequester)
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .clip(CircleShape)
                    .align(Alignment.Bottom)
                    .clickable {
                        scope.launch {
                            focus.clearFocus(force = true)
                            showDropdownMenu = true
                        }
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_nav_panel_plus),
                    contentDescription = "Plus button",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )
                MaterialTheme(
                    shapes = MaterialTheme.shapes.copy(
                        medium = RoundedCornerShape(
                            12.dp
                        )
                    ),
                    colors = MaterialTheme.colors.copy(
                        surface = colorResource(id = R.color.background_secondary)
                    )
                ) {
                    DropdownMenu(
                        offset = DpOffset(8.dp, 40.dp),
                        expanded = showDropdownMenu,
                        onDismissRequest = {
                            showDropdownMenu = false
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .defaultMinSize(
                                minWidth = 252.dp
                            )
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.chat_attachment_object),
                                    color = colorResource(id = R.color.text_primary)
                                )
                            },
                            onClick = {
                                showDropdownMenu = false
                                onAttachObjectClicked()
                            }
                        )
                        Divider(
                            paddingStart = 0.dp,
                            paddingEnd = 0.dp
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.chat_attachment_media),
                                    color = colorResource(id = R.color.text_primary)
                                )
                            },
                            onClick = {
                                showDropdownMenu = false
                                uploadMediaLauncher.launch(
                                    PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                        Divider(
                            paddingStart = 0.dp,
                            paddingEnd = 0.dp
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.chat_attachment_file),
                                    color = colorResource(id = R.color.text_primary)
                                )
                            },
                            onClick = {
                                showDropdownMenu = false
                                uploadFileLauncher.launch(
                                    arrayOf("*/*")
                                )
                            }
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = attachments.isNotEmpty() || textState.text.isNotEmpty(),
                exit = fadeOut() + scaleOut(),
                enter = fadeIn() + scaleIn(),
                modifier = Modifier.align(Alignment.Bottom)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .clip(CircleShape)
                        .clickable {
                            onMessageSent(textState.text)
                            clearText()
                            resetScroll()
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_send_message),
                        contentDescription = "Send message button",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
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
                color = colorResource(id = R.color.background_highlighted_light)
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
private fun ChatBoxUserInput(
    modifier: Modifier,
    textState: TextFieldValue,
    onMessageSent: (String) -> Unit,
    onTextChanged: (TextFieldValue) -> Unit,
) {
    BasicTextField(
        value = textState,
        onValueChange = { onTextChanged(it) },
        textStyle = BodyRegular.copy(
            color = colorResource(id = R.color.text_primary)
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Send
        ),
        keyboardActions = KeyboardActions {
            if (textState.text.isNotBlank()) {
                onMessageSent(textState.text)
            }
        },
        modifier = modifier
            .padding(
                start = 4.dp,
                end = 4.dp,
                top = 16.dp,
                bottom = 16.dp
            )
        ,
        cursorBrush = SolidColor(colorResource(id = R.color.palette_system_blue)),
        maxLines = 5,
        decorationBox = @Composable { innerTextField ->
            DefaultHintDecorationBox(
                text = textState.text,
                hint = stringResource(R.string.write_a_message),
                innerTextField = innerTextField,
                textStyle = BodyRegular.copy(color = colorResource(R.color.text_tertiary))
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable

private fun DefaultHintDecorationBox(
    text: String,
    hint: String,
    innerTextField: @Composable () -> Unit,
    textStyle: TextStyle
) {
    OutlinedTextFieldDefaults.DecorationBox(
        value = text,
        visualTransformation = VisualTransformation.None,
        innerTextField = innerTextField,
        singleLine = true,
        enabled = true,
        placeholder = {
            Text(
                text = hint,
                color = colorResource(id = R.color.text_tertiary),
                style = textStyle
            )
        },
        interactionSource = remember { MutableInteractionSource() },
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        contentPadding = PaddingValues()
    )
}


@Composable
fun Messages(
    isSpaceLevelChat: Boolean = true,
    title: String?,
    onTitleChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    messages: List<DiscussionView>,
    scrollState: LazyListState,
    onTitleFocusChanged: (Boolean) -> Unit,
    onReacted: (Id, String) -> Unit,
    onDeleteMessage: (DiscussionView.Message) -> Unit,
    onCopyMessage: (DiscussionView.Message) -> Unit,
    onAttachmentClicked: (DiscussionView.Message.Attachment) -> Unit,
    onEditMessage: (DiscussionView.Message) -> Unit,
    onReplyMessage: (DiscussionView.Message) -> Unit,
    onMarkupLinkClicked: (String) -> Unit,
    onAddReactionClicked: (String) -> Unit,
    onViewChatReaction: (Id, String) -> Unit
) {
    val scope = rememberCoroutineScope()
    LazyColumn(
        modifier = modifier,
        reverseLayout = true,
        state = scrollState,
    ) {
        itemsIndexed(
            messages,
            key = { _, msg ->
                when(msg) {
                    is DiscussionView.DateSection -> msg.timeInMillis
                    is DiscussionView.Message -> msg.id
                }
            }
        ) { idx, msg ->
            if (msg is DiscussionView.Message) {
                if (idx == 0)
                    Spacer(modifier = Modifier.height(36.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .animateItem(),
                    horizontalArrangement = if (msg.isUserAuthor)
                        Arrangement.End
                    else
                        Arrangement.Start
                ) {
                    if (!msg.isUserAuthor) {
                        ChatUserAvatar(
                            msg = msg,
                            avatar = msg.avatar,
                            modifier = Modifier.align(Alignment.Bottom)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Bubble(
                        modifier = Modifier.padding(
                            start = if (msg.isUserAuthor) 32.dp else 0.dp,
                            end = if (msg.isUserAuthor) 0.dp else 32.dp
                        ),
                        name = msg.author,
                        content = msg.content,
                        timestamp = msg.timestamp,
                        attachments = msg.attachments,
                        isUserAuthor = msg.isUserAuthor,
                        isEdited = msg.isEdited,
                        onReacted = { emoji ->
                            onReacted(msg.id, emoji)
                        },
                        reactions = msg.reactions,
                        onDeleteMessage = {
                            onDeleteMessage(msg)
                        },
                        onCopyMessage = {
                            onCopyMessage(msg)
                        },
                        onAttachmentClicked = onAttachmentClicked,
                        onEditMessage = {
                            onEditMessage(msg)
                        },
                        onMarkupLinkClicked = onMarkupLinkClicked,
                        onReply = {
                            onReplyMessage(msg)
                        },
                        reply = msg.reply,
                        onScrollToReplyClicked = { reply ->
                            // Naive implementation
                            val idx = messages.indexOfFirst { it is DiscussionView.Message && it.id == reply.msg }
                            if (idx != -1) {
                                scope.launch {
                                    scrollState.animateScrollToItem(index = idx)
                                }
                            }
                        },
                        onAddReactionClicked = {
                            onAddReactionClicked(msg.id)
                        },
                        onViewChatReaction = { emoji ->
                            onViewChatReaction(msg.id, emoji)
                        }
                    )
                }
                if (idx == messages.lastIndex) {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            } else if (msg is DiscussionView.DateSection) {
                Text(
                    text = msg.formattedDate,
                    style = Caption1Medium,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = colorResource(R.color.transparent_active)
                )
            }
        }
        if (messages.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                    ) {
                        AlertIcon(
                            icon = AlertConfig.Icon(
                                gradient = GRADIENT_TYPE_BLUE,
                                icon = R.drawable.ic_alert_message
                            )
                        )
                        Text(
                            text = stringResource(R.string.chat_empty_state_message),
                            style = Caption1Regular,
                            color = colorResource(id = R.color.text_secondary),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 12.dp
                                )
                        )
                    }
                }
            }
        }
        if (!isSpaceLevelChat) {
            item(key = HEADER_KEY) {
                Column {
                    DiscussionTitle(
                        title = title,
                        onTitleChanged = onTitleChanged,
                        onFocusChanged = onTitleFocusChanged
                    )
                    Text(
                        style = Relations2,
                        text = stringResource(R.string.chat),
                        color = colorResource(id = R.color.text_secondary),
                        modifier = Modifier.padding(
                            start = 20.dp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatUserAvatar(
    msg: DiscussionView.Message,
    avatar: DiscussionView.Message.Avatar,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
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
        if (avatar is DiscussionView.Message.Avatar.Image) {
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

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Bubble(
    modifier: Modifier = Modifier,
    name: String,
    reply: DiscussionView.Message.Reply? = null,
    content: DiscussionView.Message.Content,
    timestamp: Long,
    attachments: List<DiscussionView.Message.Attachment> = emptyList(),
    isUserAuthor: Boolean = false,
    isEdited: Boolean = false,
    reactions: List<DiscussionView.Message.Reaction> = emptyList(),
    onReacted: (String) -> Unit,
    onDeleteMessage: () -> Unit,
    onCopyMessage: () -> Unit,
    onEditMessage: () -> Unit,
    onReply: () -> Unit,
    onAttachmentClicked: (DiscussionView.Message.Attachment) -> Unit,
    onMarkupLinkClicked: (String) -> Unit,
    onScrollToReplyClicked: (DiscussionView.Message.Reply) -> Unit,
    onAddReactionClicked: () -> Unit,
    onViewChatReaction: (String) -> Unit
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
            .background(
                color = if (isUserAuthor)
                    colorResource(R.color.background_primary)
                else
                    colorResource(R.color.shape_transparent_secondary),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                showDropdownMenu = !showDropdownMenu
            }
    ) {
        if (reply != null) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(
                        color = colorResource(R.color.shape_transparent_secondary),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        onScrollToReplyClicked(reply)
                    }
            ) {
                Text(
                    text = reply.author,
                    modifier = Modifier.padding(
                        start = 12.dp,
                        top = 8.dp,
                        end = 12.dp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colorResource(id = R.color.text_primary)
                )
                Text(
                    modifier = Modifier.padding(
                        start = 12.dp,
                        top = 26.dp,
                        end = 12.dp
                    ),
                    text = reply.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colorResource(id = R.color.text_primary),
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = if (reply == null) 12.dp else 0.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = PreviewTitle2Medium,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1
            )
            Spacer(Modifier.width(12.dp))
            Text(
                modifier = Modifier.padding(top = 1.dp),
                text = timestamp.formatTimeInMillis(
                    TIME_H24
                ),
                style = Caption1Regular,
                color = colorResource(id = R.color.text_secondary),
                maxLines = 1
            )
        }
        Text(
            modifier = Modifier.padding(
                top = 0.dp,
                start = 12.dp,
                end = 12.dp,
                bottom = 0.dp
            ),
            text = buildAnnotatedString {
                content.parts.forEach { part ->
                    if (part.link != null && part.link.param != null) {
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = "link",
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
            },
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
        )
        BubbleAttachments(attachments, onAttachmentClicked)
        if (reactions.isNotEmpty()) {
            ReactionList(
                reactions = reactions,
                onReacted = onReacted,
                onViewReaction = onViewChatReaction
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
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
                            color = colorResource(id = R.color.text_primary)
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
                            color = colorResource(id = R.color.text_primary)
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
                                color = colorResource(id = R.color.text_primary)
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
                                color = colorResource(id = R.color.text_primary)
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
                                color = colorResource(id = R.color.palette_system_red)
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
}

@Composable
@OptIn(ExperimentalGlideComposeApi::class)
private fun BubbleAttachments(
    attachments: List<DiscussionView.Message.Attachment>,
    onAttachmentClicked: (DiscussionView.Message.Attachment) -> Unit
) {
    attachments.forEach { attachment ->
        when (attachment) {
            is DiscussionView.Message.Attachment.Image -> {
                GlideImage(
                    model = attachment.url,
                    contentDescription = "Attachment image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(300.dp)
                        .padding(8.dp)
                        .clip(shape = RoundedCornerShape(16.dp))
                        .clickable {
                            onAttachmentClicked(attachment)
                        }
                )
            }

            is DiscussionView.Message.Attachment.Link -> {
                AttachedObject(
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp
                        )
                        .fillMaxWidth(),
                    title = attachment.wrapper?.name.orEmpty(),
                    type = attachment.typeName,
                    icon = attachment.icon,
                    onAttachmentClicked = {
                        onAttachmentClicked(attachment)
                    }
                )
            }
        }
    }
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

@Composable
fun AttachedObject(
    modifier: Modifier,
    title: String,
    type: String,
    icon: ObjectIcon,
    onAttachmentClicked: () -> Unit
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.shape_tertiary),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = colorResource(id = R.color.background_secondary)
            )
            .clickable {
                onAttachmentClicked()
            }
    ) {
        ListWidgetObjectIcon(
            icon = icon,
            iconSize = 48.dp,
            modifier = Modifier
                .padding(
                    start = 12.dp
                )
                .align(alignment = Alignment.CenterStart),
            onTaskIconClicked = {
                // Do nothing
            }
        )
        Text(
            text = title.ifEmpty { stringResource(R.string.untitled) },
            modifier = Modifier.padding(
                start = if (icon != ObjectIcon.None)
                    72.dp
                else
                    12.dp,
                top = 17.5.dp,
                end = 12.dp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = PreviewTitle2Medium,
            color = colorResource(id = R.color.text_primary)
        )
        Text(
            text = type.ifEmpty { stringResource(R.string.unknown_type) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = if (icon != ObjectIcon.None)
                        72.dp
                    else
                        12.dp,
                    bottom = 17.5.dp
                ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = Relations3,
            color = colorResource(id = R.color.text_secondary)
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ReactionList(
    reactions: List<DiscussionView.Message.Reaction>,
    onReacted: (String) -> Unit,
    onViewReaction: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        reactions.forEach { reaction ->
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .width(60.dp)
                    .background(
                        color = if (reaction.isSelected)
                            colorResource(id = R.color.palette_very_light_orange)
                        else
                            colorResource(id = R.color.shape_transparent_primary),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .clip(RoundedCornerShape(100.dp))
                    .then(
                        if (reaction.isSelected)
                            Modifier.border(
                                width = 1.dp,
                                color = colorResource(id = R.color.palette_system_amber_50),
                                shape = RoundedCornerShape(100.dp)
                            )
                        else
                            Modifier
                    )
                    .combinedClickable(
                        onClick = {
                            onReacted(reaction.emoji)
                        },
                        onLongClick = {
                            onViewReaction(reaction.emoji)
                        }
                    )
            ) {
                Text(
                    text = reaction.emoji,
                    style = BodyCalloutMedium,
                    modifier = Modifier
                        .align(
                            alignment = Alignment.CenterStart
                        )
                        .padding(
                            start = 8.dp
                        )
                )
                Text(
                    text = reaction.count.toString(),
                    style = BodyCalloutMedium,
                    modifier = Modifier
                        .align(
                            alignment = Alignment.CenterEnd
                        )
                        .padding(
                            end = 12.dp
                        ),
                    color = if (reaction.isSelected)
                        colorResource(id = R.color.text_primary)
                    else
                        colorResource(id = R.color.text_white)
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun ReactionListPreview() {
    ReactionList(
        reactions = listOf(
            DiscussionView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = false
            ),
            DiscussionView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = true
            ),
            DiscussionView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = false
            ),
            DiscussionView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = false
            ),
            DiscussionView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = false
            ),
            DiscussionView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = false
            )
        ),
        onReacted = {},
        onViewReaction = {}
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun TopDiscussionToolbarPreview() {
    TopDiscussionToolbar()
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun AttachmentPreview() {
    AttachedObject(
        modifier = Modifier.fillMaxWidth(),
        icon = ObjectIcon.None,
        type = "Project",
        title = "Travel to Switzerland",
        onAttachmentClicked = {}
    )
}

private const val HEADER_KEY = "key.discussions.item.header"
private val JumpToBottomThreshold = 200.dp