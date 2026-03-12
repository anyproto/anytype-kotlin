package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.anytypeio.anytype.core_models.chats.ChatMessageSearchResult
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatSearchState

@Composable
fun ChatSearchScreen(
    state: ChatSearchState.Active,
    onQueryChanged: (String) -> Unit,
    onResultSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    resolveMemberName: (String) -> String,
    resolveMemberAvatar: (String) -> ChatView.Message.Avatar = { ChatView.Message.Avatar.Initials("") }
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background_primary))
            .statusBarsPadding()
    ) {
        // Results area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                state.isSearching -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colorResource(R.color.palette_system_blue)
                    )
                }
                state.query.isNotEmpty() && state.results.isEmpty() -> {
                    // Empty state
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier.size(56.dp),
                            painter = painterResource(
                                id = com.anytypeio.anytype.core_ui.R.drawable.ic_popup_coffee_56
                            ),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(
                                id = com.anytypeio.anytype.localization.R.string.nothing_found
                            ),
                            style = BodyCalloutMedium,
                            color = colorResource(R.color.text_primary)
                        )
                    }
                }
                state.results.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            items = state.results,
                            key = { _, result -> result.messageId }
                        ) { index, result ->
                            ChatSearchResultItem(
                                result = result,
                                memberName = resolveMemberName(result.message.creator),
                                avatar = resolveMemberAvatar(result.message.creator),
                                onClick = { onResultSelected(index) }
                            )
                        }
                    }
                }
            }
        }

        // Floating search bar
        ChatSearchBar(
            query = state.query,
            onQueryChanged = onQueryChanged,
            onDismiss = onDismiss,
            focusRequester = focusRequester,
            modifier = Modifier
                .imePadding()
                .navigationBarsPadding()
        )
    }
}

@Composable
fun ChatSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() },
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search input pill
        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(296.dp))
                .background(colorResource(R.color.shape_transparent))
                .then(
                    if (onClick != null) {
                        Modifier.clickable { onClick() }
                    } else {
                        Modifier
                    }
                )
                .padding(start = 12.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(
                    id = com.anytypeio.anytype.core_ui.R.drawable.ic_search_18
                ),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    colorResource(R.color.text_tertiary)
                ),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (onClick != null) {
                Text(
                    text = query.ifEmpty {
                        stringResource(com.anytypeio.anytype.localization.R.string.search)
                    },
                    style = PreviewTitle2Medium,
                    color = if (query.isEmpty()) {
                        colorResource(R.color.text_tertiary)
                    } else {
                        colorResource(R.color.text_primary)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            } else {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChanged,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = PreviewTitle2Medium.copy(
                        color = colorResource(R.color.text_primary)
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(colorResource(R.color.palette_system_blue)),
                    decorationBox = { innerTextField ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    text = stringResource(
                                        com.anytypeio.anytype.localization.R.string.search
                                    ),
                                    style = PreviewTitle2Medium,
                                    color = colorResource(R.color.text_tertiary)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Close button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(296.dp))
                .background(colorResource(R.color.shape_transparent))
                .noRippleClickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.ic_search_close_18
                ),
                contentDescription = stringResource(R.string.chats_search_close_content_description),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ChatSearchResultItem(
    result: ChatMessageSearchResult,
    memberName: String,
    avatar: ChatView.Message.Avatar = ChatView.Message.Avatar.Initials(""),
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(100))
                    .background(colorResource(R.color.text_tertiary)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = memberName.take(1).uppercase().ifEmpty { "U" },
                    style = PreviewTitle2Medium,
                    color = colorResource(R.color.text_white)
                )
                if (avatar is ChatView.Message.Avatar.Image) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(avatar.hash)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(100)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = memberName.ifEmpty {
                        stringResource(R.string.untitled)
                    },
                    style = PreviewTitle2Medium,
                    color = colorResource(R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Highlighted message preview
                val highlightColor = colorResource(R.color.palette_system_blue)
                Text(
                    text = buildHighlightedText(
                        text = result.highlight,
                        ranges = result.highlightRanges,
                        highlightColor = highlightColor
                    ),
                    style = Relations2,
                    color = colorResource(R.color.text_secondary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatSearchResultDate(result.message.createdAt),
                style = Relations2,
                color = colorResource(R.color.text_secondary)
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(0.5.dp)
                .background(colorResource(R.color.shape_primary))
        )
    }
}

private fun buildHighlightedText(
    text: String,
    ranges: List<IntRange>,
    highlightColor: androidx.compose.ui.graphics.Color
): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        append(text)
        for (range in ranges) {
            val start = range.first.coerceIn(0, text.length)
            val end = range.last.coerceIn(0, text.length)
            if (start < end) {
                addStyle(
                    style = SpanStyle(color = highlightColor),
                    start = start,
                    end = end
                )
            }
        }
    }
}

private fun formatSearchResultDate(timestampSeconds: Long): String {
    if (timestampSeconds == 0L) return ""
    val messageTime = timestampSeconds * 1000
    val calendar = java.util.Calendar.getInstance().apply { timeInMillis = messageTime }
    val today = java.util.Calendar.getInstance()

    return when {
        calendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
            calendar.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR) -> {
            "Today"
        }
        calendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
            today.get(java.util.Calendar.DAY_OF_YEAR) - calendar.get(java.util.Calendar.DAY_OF_YEAR) < 7 -> {
            java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault()).format(messageTime)
        }
        calendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) -> {
            java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(messageTime)
        }
        else -> {
            java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()).format(messageTime)
        }
    }
}
