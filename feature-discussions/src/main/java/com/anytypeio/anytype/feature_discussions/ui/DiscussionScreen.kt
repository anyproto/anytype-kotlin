package com.anytypeio.anytype.feature_discussions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionHeader
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionView
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel

@Composable
fun DiscussionScreenWrapper(
    vm: DiscussionViewModel,
    onBackClicked: () -> Unit
) {
    val header = vm.header.collectAsStateWithLifecycle().value
    val messages = vm.messages.collectAsStateWithLifecycle().value

    DiscussionScreen(
        header = header,
        comments = messages,
        onBackClicked = onBackClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionScreen(
    header: DiscussionHeader,
    comments: List<DiscussionView>,
    onBackClicked: () -> Unit
) {
    Scaffold(
        containerColor = colorResource(id = R.color.background_primary),
        topBar = {
            DiscussionTopBar(
                header = header,
                onBackClicked = onBackClicked
            )
        }
    ) { paddingValues ->
        DiscussionCommentList(
            comments = comments,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionTopBar(
    header: DiscussionHeader,
    onBackClicked: () -> Unit
) {
    TopAppBar(
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = R.color.text_primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${header.commentCount} ${stringResource(id = com.anytypeio.anytype.localization.R.string.discussion_comments)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text_secondary)
                )
            }
        }
    )
}

@Composable
fun DiscussionCommentList(
    comments: List<DiscussionView>,
    modifier: Modifier = Modifier
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
                    is DiscussionView.DateSection -> item.formattedDate
                }
            }
        ) { item ->
            when (item) {
                is DiscussionView.Comment -> {
                    DiscussionCommentItem(comment = item)
                    HorizontalDivider(
                        color = colorResource(id = R.color.shape_primary),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                is DiscussionView.Reply -> {
                    DiscussionReplyItem(reply = item)
                }
                is DiscussionView.DateSection -> {
                    DateSectionHeader(section = item)
                }
            }
        }
    }
}

@Composable
fun DiscussionCommentItem(comment: DiscussionView.Comment) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Author row: avatar + name + date
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CommentAvatar(
                avatar = comment.avatar,
                size = 32
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = comment.author,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (comment.formattedDate != null) {
                Text(
                    text = comment.formattedDate,
                    fontSize = 13.sp,
                    color = colorResource(id = R.color.text_secondary)
                )
            }
        }
        // Text content
        if (comment.content.parts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            RichTextContent(parts = comment.content.parts)
        }
        // Reactions
        if (comment.reactions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            ReactionsRow(reactions = comment.reactions)
        }
    }
}

@Composable
fun DiscussionReplyItem(reply: DiscussionView.Reply) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
    ) {
        // Vertical reply bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(48.dp)
                .background(
                    color = colorResource(id = R.color.shape_primary),
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Author row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CommentAvatar(
                    avatar = reply.avatar,
                    size = 24
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = reply.author,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (reply.formattedDate != null) {
                    Text(
                        text = reply.formattedDate,
                        fontSize = 13.sp,
                        color = colorResource(id = R.color.text_secondary)
                    )
                }
            }
            // Text content
            if (reply.content.parts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                RichTextContent(parts = reply.content.parts)
            }
            // Reactions
            if (reply.reactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                ReactionsRow(reactions = reply.reactions)
            }
        }
    }
}

@Composable
fun CommentAvatar(
    avatar: DiscussionView.Avatar,
    size: Int
) {
    when (avatar) {
        is DiscussionView.Avatar.Image -> {
            AsyncImage(
                model = avatar.hash,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        is DiscussionView.Avatar.Initials -> {
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.shape_transparent_secondary)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatar.initial,
                    fontSize = (size / 2).sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.text_secondary)
                )
            }
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
    Row {
        reactions.forEach { reaction ->
            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .background(
                        color = colorResource(id = R.color.shape_transparent_primary),
                        shape = CircleShape
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${reaction.emoji} ${reaction.count}",
                    fontSize = 13.sp,
                    color = colorResource(id = R.color.text_primary)
                )
            }
        }
    }
}

@Composable
fun DateSectionHeader(section: DiscussionView.DateSection) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = section.formattedDate,
            fontSize = 13.sp,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}
