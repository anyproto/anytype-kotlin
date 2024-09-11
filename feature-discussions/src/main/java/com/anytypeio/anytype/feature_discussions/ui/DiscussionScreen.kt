package com.anytypeio.anytype.feature_discussions.ui

import android.content.res.Configuration
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.AlertIcon
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_BLUE
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.widgets.GlobalSearchObjectIcon
import com.anytypeio.anytype.core_utils.const.DateConst.TIME_H24
import com.anytypeio.anytype.core_utils.ext.formatTimeInMillis
import com.anytypeio.anytype.feature_discussions.R
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionView
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel.UXCommand
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView
import kotlinx.coroutines.launch


@Composable
fun DiscussionScreenWrapper(
    vm: DiscussionViewModel,
    // TODO move to view model
    onAttachClicked: () -> Unit
) {
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
                    .background(
                        color = colorResource(id = R.color.background_primary)
                    )
            ) {
                val lazyListState = rememberLazyListState()
                DiscussionScreen(
                    title = vm.name.collectAsState().value,
                    messages = vm.messages.collectAsState().value,
                    attachments = vm.attachments.collectAsState().value,
                    onMessageSent = vm::onMessageSent,
                    onTitleChanged = vm::onTitleChanged,
                    onAttachClicked = onAttachClicked,
                    onClearAttachmentClicked = vm::onClearAttachmentClicked,
                    lazyListState = lazyListState
                )
                LaunchedEffect(Unit) {
                    vm.commands.collect { command ->
                        when(command) {
                            UXCommand.JumpToBottom -> {
                                lazyListState.animateScrollToItem(0)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * TODO: do date formating before rendering?
 */
@Composable
fun DiscussionScreen(
    lazyListState: LazyListState,
    title: String?,
    messages: List<DiscussionView.Message>,
    attachments: List<GlobalSearchItemView>,
    onMessageSent: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onAttachClicked: () -> Unit,
    onClearAttachmentClicked: () -> Unit
) {
    var isTitleFocused by remember { mutableStateOf(false) }
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
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        TopDiscussionToolbar(
            title = title,
            isHeaderVisible = isHeaderVisible
        )
        Box(modifier = Modifier.weight(1.0f)) {
            Messages(
                modifier = Modifier.fillMaxSize(),
                messages = messages,
                scrollState = lazyListState,
                onTitleChanged = onTitleChanged,
                title = title,
                onTitleFocusChanged = {
                    isTitleFocused = it
                }
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
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp),
                onGoToBottomClicked = {
                    scope.launch {
                        lazyListState.animateScrollToItem(index = 0)
                    }
                },
                enabled = jumpToBottomButtonEnabled
            )
        }
        Divider(
            paddingStart = 0.dp,
            paddingEnd = 0.dp
        )
        attachments.forEach {
            Box {
                Attachment(
                    modifier = Modifier.padding(
                        top = 12.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    globalSearchItemView = it
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_clear_18),
                    contentDescription = "Close icon",
                    modifier = Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(
                            top = 6.dp,
                            end = 10.dp
                        )
                        .noRippleClickable {
                            onClearAttachmentClicked()
                        }
                )
            }
        }
        ChatBox(
            onMessageSent = onMessageSent,
            onAttachClicked = onAttachClicked,
            resetScroll = {
                scope.launch {
                    lazyListState.animateScrollToItem(index = 0)
                }
            },
            isTitleFocused = isTitleFocused,
            attachments = attachments
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
    onMessageSent: (String) -> Unit = {},
    onAttachClicked: () -> Unit = {},
    resetScroll: () -> Unit = {},
    isTitleFocused: Boolean,
    attachments: List<GlobalSearchItemView>,
) {
    val context = LocalContext.current
    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    val scope = rememberCoroutineScope()

    val focus = LocalFocusManager.current

    Row(
        modifier = Modifier
            .then(
                if (isTitleFocused)
                    Modifier
                else
                    Modifier.imePadding()
            )
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .clip(CircleShape)
                .align(Alignment.Bottom)
                .clickable {
                    scope.launch {
                        focus.clearFocus(force = true)
                        onAttachClicked()
                    }
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_plus_32),
                contentDescription = "Plus button",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
        ChatBoxUserInput(
            textState = textState,
            onMessageSent = {
                onMessageSent(it)
                textState = TextFieldValue()
                resetScroll()
            },
            onTextChanged = { value ->
                textState = value
            },
            modifier = Modifier
                .weight(1f)
                .align(Alignment.Bottom)
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .clip(CircleShape)
                .align(Alignment.Bottom)
                .clickable {
                    if (textState.text.isNotBlank()) {
                        onMessageSent(textState.text)
                        textState = TextFieldValue()
                        resetScroll()
                    }
                }
        ) {
            if (textState.text.isNotBlank()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_send_message),
                    contentDescription = "Send message button",
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .align(Alignment.Center)
                )
            }
        }
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
                hint = "Write a message",
                innerTextField = innerTextField,
                textStyle = BodyRegular
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
    title: String?,
    onTitleChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    messages: List<DiscussionView.Message>,
    scrollState: LazyListState,
    onTitleFocusChanged: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        reverseLayout = true,
        state = scrollState,
    ) {
        itemsIndexed(
            messages,
            key = { _, msg -> msg.id }
        ) { idx, msg ->
            if (idx == 0)
                Spacer(modifier = Modifier.height(36.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            colorResource(id = R.color.palette_system_blue),
                            shape = CircleShape
                        )
                        .align(Alignment.Bottom)
                ) {
                    Text(
                        text = msg.author.take(1).uppercase().ifEmpty { stringResource(id = R.string.u) },
                        modifier = Modifier.align(Alignment.Center),
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text_white)
                        )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Bubble(
                    name = msg.author,
                    msg = msg.msg,
                    timestamp = msg.timestamp,
                    attachments = msg.attachments,
                    isUserAuthor = msg.isUserAuthor
                )
            }
            if (idx == messages.lastIndex) {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
        if (messages.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 170.dp)
                ) {
                    AlertIcon(
                        icon = AlertConfig.Icon(
                            gradient = GRADIENT_TYPE_BLUE,
                            icon = R.drawable.ic_alert_message
                        )
                    )
                    Text(
                        text = "There is no messages yet. \n" +
                                "Be the first to start a discussion."
                        ,
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
        item(key = HEADER_KEY) {
            Column {
                DiscussionTitle(
                    title = title,
                    onTitleChanged = onTitleChanged,
                    onFocusChanged = onTitleFocusChanged
                )
                Text(
                    style = Relations2,
                    text = "Discussion",
                    color = colorResource(id = R.color.text_secondary),
                    modifier = Modifier.padding(
                        start = 20.dp
                    )
                )
            }
        }
    }
}

@Composable
fun Bubble(
    name: String,
    msg: String,
    timestamp: Long,
    attachments: List<DiscussionView.Message.Attachment> = emptyList(),
    isUserAuthor: Boolean = false
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isUserAuthor)
                    colorResource(id = R.color.palette_very_light_lime)
                else
                    colorResource (id = R.color.palette_very_light_grey)
                ,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable {
                showDropdownMenu = !showDropdownMenu
            }
    ) {
        Row(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp
            )
        ) {
            Text(
                text = name,
                style = PreviewTitle2Medium,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text(
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
                start = 16.dp,
                end = 16.dp,
                bottom = 0.dp
            ),
            text = msg,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary)
        )
        attachments.forEach {
            Attachment(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp
                ),
                globalSearchItemView = it.item
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
                        Row {
                            Text(
                                text = "\uD83D\uDC4D",
                                modifier = Modifier.noRippleClickable {
                                    showDropdownMenu = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "❤\uFE0F",
                                modifier = Modifier.noRippleClickable {
                                    showDropdownMenu = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "\uD83D\uDE02",
                                modifier = Modifier.noRippleClickable {
                                    showDropdownMenu = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "\uD83D\uDE2E",
                                modifier = Modifier.noRippleClickable {
                                    showDropdownMenu = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "\uD83D\uDE22",
                                modifier = Modifier.noRippleClickable {
                                    showDropdownMenu = false
                                }
                            )
                        }
                    },
                    onClick = { /*TODO*/ }
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
fun Attachment(
    modifier: Modifier,
    globalSearchItemView: GlobalSearchItemView
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.shape_tertiary),
                shape = RoundedCornerShape(12.dp)
            )
            .background(color = colorResource(id = R.color.background_secondary))
    ) {
        GlobalSearchObjectIcon(
            icon = globalSearchItemView.icon,
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
            text = globalSearchItemView.title,
            modifier = Modifier.padding(
                start = 72.dp,
                top = 17.5.dp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = PreviewTitle2Medium,
            color = colorResource(id = R.color.text_primary)
        )
        Text(
            text = globalSearchItemView.type,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 72.dp,
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
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.shape_primary),
                    shape = RoundedCornerShape(12.dp)
                )
                .background(color = colorResource(id = R.color.background_primary))
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
    Attachment(
        modifier = Modifier,
        GlobalSearchItemView(
            id = "id",
            layout = ObjectType.Layout.BASIC,
            title = "Travel to Switzerland",
            type = "Project",
            meta = GlobalSearchItemView.Meta.None,
            pinned = false,
            links = emptyList(),
            backlinks = emptyList(),
            space = SpaceId("spaced"),
            icon = ObjectIcon.None
        )
    )
}

private const val HEADER_KEY = "key.discussions.item.header"
private val JumpToBottomThreshold = 56.dp