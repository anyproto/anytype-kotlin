package com.anytypeio.anytype.feature_discussions.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_utils.const.DateConst.DEFAULT_DATE_FORMAT
import com.anytypeio.anytype.core_utils.ext.formatTimeInMillis
import com.anytypeio.anytype.feature_discussions.R
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionView
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel
import kotlinx.coroutines.launch
import timber.log.Timber


@Composable
fun DiscussionScreenWrapper(
    vm: DiscussionViewModel
) {
    NavHost(
        navController = rememberNavController(),
        startDestination = "discussions"
    ) {
        composable(
            route = "discussions"
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colorResource(id = R.color.background_primary))
            ) {
                DiscussionScreen(
                    title = vm.name.collectAsState().value,
                    messages = vm.messages.collectAsState().value,
                    onMessageSent = vm::onMessageSent,
                    onTitleChanged = vm::onTitleChanged
                )
            }
        }
    }
}

/**
 * TODO: do date formating before rendering?
 */
@Composable
fun DiscussionScreen(
    title: String?,
    messages: List<DiscussionView.Message>,
    onMessageSent: (String) -> Unit,
    onTitleChanged: (String) -> Unit
) {
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        TopDiscussionToolbar(title = title)
        Messages(
            modifier = Modifier.weight(1.0f),
            messages = messages,
            scrollState = scrollState,
            onTitleChanged = onTitleChanged,
            title = title
        )
        Divider(
            paddingStart = 0.dp,
            paddingEnd = 0.dp
        )
        ChatBox(
            onMessageSent = onMessageSent,
            resetScroll = {
                scope.launch {
                    scrollState.animateScrollToItem(index = 0)
                }
            }
        )
    }
}

@Composable
private fun DiscussionTitle(
    title: String?,
    onTitleChanged: (String) -> Unit
) {
    Timber.d("DROID-2635: Rendering title: $title")
    BasicTextField(
        textStyle = HeadlineTitle.copy(
            color = colorResource(id = R.color.text_primary)
        ),
        value = title.orEmpty(),
        onValueChange = {
            onTitleChanged(it)
        },
        modifier = Modifier.padding(
            top = 24.dp,
            start = 20.dp,
            end = 20.dp,
            bottom = 8.dp
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        )
    )
}

@Composable
private fun ChatBox(
    onMessageSent: (String) -> Unit = {},
    resetScroll: () -> Unit = {},
) {
    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    Row(
        modifier = Modifier
            .imePadding()
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .clip(CircleShape)
                .align(Alignment.CenterVertically)
                .clickable {
                    // TODO
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_plus_32),
                contentDescription = "Plus button",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .clickable {
                        // TODO
                    }
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
                .imePadding()
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .clip(CircleShape)
                .align(Alignment.CenterVertically)
                .clickable {
                    if (textState.text.isNotBlank()) {
                        onMessageSent(textState.text)
                        textState = TextFieldValue()
                        resetScroll()
                    }
                }
        ) {
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
                end = 4.dp
            ),
        cursorBrush = SolidColor(colorResource(id = R.color.palette_system_blue))
    )
}


@Composable
fun Messages(
    title: String?,
    onTitleChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    messages: List<DiscussionView.Message>,
    scrollState: LazyListState
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
                        text = "U",
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
                    timestamp = msg.timestamp
                )
            }
            if (idx == messages.lastIndex) {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
        item(key = "Header") {
            Column {
                DiscussionTitle(
                    title = title,
                    onTitleChanged = onTitleChanged
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
    timestamp: Long
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.palette_very_light_grey),
                shape = RoundedCornerShape(24.dp)
            )
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
                    DEFAULT_DATE_FORMAT
                ),
                style = Caption1Regular,
                color = colorResource(id = R.color.text_secondary),
                maxLines = 1
            )
        }
        Text(
            modifier = Modifier.padding(
                top = 32.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 12.dp
            ),
            text = msg,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Composable
fun TopDiscussionToolbar(
    title: String? = null
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
            text = title ?: stringResource(id = R.string.untitled),
            style = PreviewTitle2Regular,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f)
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

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun TopDiscussionToolbarPreview() {
    TopDiscussionToolbar()
}