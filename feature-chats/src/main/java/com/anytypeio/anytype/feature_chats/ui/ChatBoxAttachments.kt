package com.anytypeio.anytype.feature_chats.ui

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.video.videoFrameMillis
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import timber.log.Timber

@Composable
internal fun ChatBoxAttachments(
    attachments: List<ChatView.Message.ChatBoxAttachment>,
    onClearAttachmentClicked: (ChatView.Message.ChatBoxAttachment) -> Unit
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
                is ChatView.Message.ChatBoxAttachment.Existing.Link -> {
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
                                type = attachment.typeName,
                                icon = attachment.icon,
                                onAttachmentClicked = {
                                    // Do nothing
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
                is ChatView.Message.ChatBoxAttachment.Link -> {
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
                                    // Do nothing
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
                is ChatView.Message.ChatBoxAttachment.Media -> {
                    item {
                        Box(modifier = Modifier.padding()) {
                            if (attachment.isVideo) {
                                AsyncImage(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .padding(
                                            top = 12.dp,
                                            end = 4.dp
                                        )
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .alpha(
                                            if (attachment.state is ChatView.Message.ChatBoxAttachment.State.Uploading) {
                                                0.3f
                                            } else {
                                                1f
                                            }
                                        ),
                                    model = ImageRequest.Builder(context)
                                        .data(attachment.uri)
                                        .videoFrameMillis(0)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    onState = { state ->
                                        Timber.d("Chat box video attachment state for ${attachment.uri}: $state")
                                    }
                                )
                            } else {
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
                                        .alpha(
                                            if (attachment.state is ChatView.Message.ChatBoxAttachment.State.Uploading) {
                                                0.3f
                                            } else {
                                                1f
                                            }
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
                            ChatBoxAttachmentState(attachment.state)
                        }
                    }
                }
                is ChatView.Message.ChatBoxAttachment.Existing.Image -> {
                    item {
                        Box(modifier = Modifier.padding()) {
                            Image(
                                painter = rememberAsyncImagePainter(attachment.url),
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
                is ChatView.Message.ChatBoxAttachment.Existing.Video -> {
                    item {
                        Box(modifier = Modifier.padding()) {
                            Image(
                                painter = rememberAsyncImagePainter(attachment.url),
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
                is ChatView.Message.ChatBoxAttachment.File -> {
                    item {
                        Box {
                            AttachedFile(
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
                                ),
                                onAttachmentClicked = {
                                    // Do nothing
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
                            ChatBoxAttachmentState(attachment.state)
                        }
                    }
                }
                is ChatView.Message.ChatBoxAttachment.Bookmark -> {
                    item {
                        Box {
                            AttachedObject(
                                modifier = Modifier
                                    .padding(
                                        top = 12.dp,
                                        end = 4.dp
                                    )
                                    .width(216.dp),
                                title = if (attachment.isLoadingPreview)
                                    stringResource(R.string.three_dots_text_placeholder)
                                else
                                    attachment.preview.title,
                                type = stringResource(R.string.bookmark),
                                icon = ObjectIcon.None,
                                onAttachmentClicked = {
                                    // Do nothing
                                }
                            )
                            Box(
                                Modifier
                                    .align(
                                        Alignment.TopEnd
                                    )
                                    .padding(top = 6.dp)
                                    .noRippleClickable {
                                        onClearAttachmentClicked(attachment)
                                    }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_clear_chatbox_attachment),
                                    contentDescription = "Close icon",
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                )
                                if (attachment.isLoadingPreview || attachment.isUploading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .align(Alignment.Center),
                                        color = colorResource(R.color.text_white),
                                        trackColor = colorResource(R.color.glyph_active).copy(alpha = 0.5f),
                                        strokeWidth = 1.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.ChatBoxAttachmentState(state: ChatView.Message.ChatBoxAttachment.State) {
    when (state) {
        ChatView.Message.ChatBoxAttachment.State.Failed -> {
            Image(
                painter = painterResource(R.drawable.ic_chat_box_attachment_error_circle),
                contentDescription = null,
                modifier = Modifier
                    .padding(
                        top = 12.dp,
                        end = 4.dp
                    )
                    .align(alignment = Alignment.Center)
            )
        }

        ChatView.Message.ChatBoxAttachment.State.Uploaded -> {
            // Do nothing.
        }
        is ChatView.Message.ChatBoxAttachment.State.Preloaded -> {
            // Do nothing.
        }
        ChatView.Message.ChatBoxAttachment.State.Uploading, ChatView.Message.ChatBoxAttachment.State.Preloading -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(
                        top = 12.dp,
                        end = 4.dp
                    )
                    .align(alignment = Alignment.Center)
                    .size(36.dp),
                color = colorResource(R.color.glyph_active),
                trackColor = colorResource(R.color.glyph_active).copy(alpha = 0.5f),
                strokeWidth = 4.dp
            )
        }

        ChatView.Message.ChatBoxAttachment.State.Idle -> {
            // Do nothing.
        }
    }
}

@DefaultPreviews
@Composable
fun ChatBoxAttachmentsInUploadingStatePreview() {
    ChatBoxAttachments(
        attachments = listOf(
            ChatView.Message.ChatBoxAttachment.Media(
                state = ChatView.Message.ChatBoxAttachment.State.Uploading,
                uri = "Uri"
            ),
            ChatView.Message.ChatBoxAttachment.Media(
                state = ChatView.Message.ChatBoxAttachment.State.Uploading,
                uri = "Uri"
            )
        ),
        onClearAttachmentClicked = {}
    )
}

@DefaultPreviews
@Composable
fun ChatBoxAttachmentsInFailedStatePreview() {
    ChatBoxAttachments(
        attachments = listOf(
            ChatView.Message.ChatBoxAttachment.Media(
                state = ChatView.Message.ChatBoxAttachment.State.Failed,
                uri = "Uri"
            ),
            ChatView.Message.ChatBoxAttachment.Media(
                state = ChatView.Message.ChatBoxAttachment.State.Failed,
                uri = "Uri"
            )
        ),
        onClearAttachmentClicked = {}
    )
}