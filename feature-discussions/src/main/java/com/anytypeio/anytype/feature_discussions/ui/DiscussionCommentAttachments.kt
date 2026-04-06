package com.anytypeio.anytype.feature_discussions.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.video.videoFrameMillis
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.feature_discussions.R
import com.anytypeio.anytype.feature_discussions.presentation.CommentAttachment

@Composable
internal fun DiscussionCommentAttachments(
    attachments: List<CommentAttachment>,
    onClearAttachmentClicked: (CommentAttachment) -> Unit
) {
    val context = LocalContext.current

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        attachments.forEach { attachment ->
            when (attachment) {
                is CommentAttachment.Media -> {
                    item {
                        Box(modifier = Modifier.padding()) {
                            if (attachment.isVideo) {
                                AsyncImage(
                                    modifier = Modifier
                                        .padding(top = 12.dp, end = 4.dp)
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .alpha(
                                            if (attachment.state is CommentAttachment.State.Uploading) 0.3f else 1f
                                        ),
                                    model = ImageRequest.Builder(context)
                                        .data(attachment.uri)
                                        .videoFrameMillis(0)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = rememberAsyncImagePainter(attachment.uri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(top = 12.dp, end = 4.dp)
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .alpha(
                                            if (attachment.state is CommentAttachment.State.Uploading) 0.3f else 1f
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                            }
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
                            CommentAttachmentStateIndicator(attachment.state)
                        }
                    }
                }
                is CommentAttachment.File -> {
                    item {
                        Box {
                            AttachedFileItem(
                                modifier = Modifier
                                    .padding(top = 12.dp, end = 4.dp)
                                    .width(216.dp),
                                name = attachment.name
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
                            CommentAttachmentStateIndicator(attachment.state)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.CommentAttachmentStateIndicator(state: CommentAttachment.State) {
    when (state) {
        is CommentAttachment.State.Uploading,
        is CommentAttachment.State.Preloading -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = 12.dp, end = 4.dp)
                    .align(alignment = Alignment.Center)
                    .size(36.dp),
                color = colorResource(com.anytypeio.anytype.core_ui.R.color.glyph_active),
                strokeWidth = 2.dp
            )
        }
        else -> { /* No indicator for Idle, Preloaded, Uploaded, Failed */ }
    }
}

@Composable
private fun AttachedFileItem(
    modifier: Modifier = Modifier,
    name: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .alpha(0.8f)
            .padding(8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        androidx.compose.material.Text(
            text = name,
            maxLines = 2,
            style = com.anytypeio.anytype.core_ui.views.Caption1Regular,
            color = colorResource(com.anytypeio.anytype.core_ui.R.color.text_primary)
        )
    }
}
