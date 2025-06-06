package com.anytypeio.anytype.feature_chats.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.video.videoFrameMillis
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy

@Composable
@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
fun BubbleAttachments(
    attachments: List<ChatView.Message.Attachment>,
    onAttachmentClicked: (ChatView.Message.Attachment) -> Unit,
    onAttachmentLongClicked: (ChatView.Message.Attachment) -> Unit,
    isUserAuthor: Boolean
) {
    var isVideoPreviewLoaded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    attachments.forEachIndexed { idx, attachment ->
        when (attachment) {
            is ChatView.Message.Attachment.Gallery -> {
                val rowConfig = attachment.rowConfig
                var index = 0
                rowConfig.forEachIndexed { idx, rowSize ->
                    BubbleGalleryRowLayout(
                        onAttachmentClicked = onAttachmentClicked,
                        images = attachment.images.slice(index until index + rowSize)
                    )
                    if (idx != rowConfig.lastIndex) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    index += rowSize
                }
            }
            is ChatView.Message.Attachment.Video -> {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(292.dp)
                        .background(
                            color = colorResource(R.color.shape_tertiary),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .combinedClickable(
                            onClick = {
                                requestPlayingVideoByOS(attachment, context)
                            },
                            onLongClick = {
                                onAttachmentLongClicked(attachment)
                            }
                        )
                ) {
                    if (!isVideoPreviewLoaded) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(alignment = Alignment.Center)
                                .size(48.dp),
                            color = colorResource(R.color.glyph_active),
                            trackColor = colorResource(R.color.glyph_active).copy(alpha = 0.5f),
                            strokeWidth = 4.dp
                        )
                    }
                    AsyncImage(
                        modifier = Modifier
                            .size(292.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        model = ImageRequest.Builder(context)
                            .data(attachment.url)
                            .videoFrameMillis(0)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        onState = { state ->
                            isVideoPreviewLoaded = state is AsyncImagePainter.State.Success
                        }
                    )
                    if (isVideoPreviewLoaded) {
                        Image(
                            modifier = Modifier.align(Alignment.Center),
                            painter = painterResource(id = R.drawable.ic_chat_attachment_play),
                            contentDescription = "Play button"
                        )
                    }
                }
            }
            is ChatView.Message.Attachment.Image -> {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(292.dp)
                        .background(
                            color = colorResource(R.color.shape_tertiary),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(alignment = Alignment.Center)
                            .size(48.dp),
                        color = colorResource(R.color.glyph_active),
                        trackColor = colorResource(R.color.glyph_active).copy(alpha = 0.5f),
                        strokeWidth = 4.dp
                    )
                    GlideImage(
                        model = attachment.url,
                        contentDescription = "Attachment image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(292.dp)
                            .clip(shape = RoundedCornerShape(12.dp))
                            .combinedClickable(
                                onClick = {
                                    onAttachmentClicked(attachment)
                                },
                                onLongClick = {
                                    onAttachmentLongClicked(attachment)
                                }
                            )
                    ) {
                        it
                            .override(1024, 1024)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .format(DecodeFormat.PREFER_RGB_565)
                    }
                }
            }
            is ChatView.Message.Attachment.Link -> {
                AttachedObject(
                    modifier = Modifier
                        .padding(
                            start = 4.dp,
                            end = 4.dp,
                            bottom = 4.dp,
                            top = 0.dp
                        )
                        .fillMaxWidth()
                    ,
                    title = attachment.wrapper?.name.orEmpty(),
                    type = attachment.typeName,
                    icon = attachment.icon,
                    onAttachmentClicked = {
                        onAttachmentClicked(attachment)
                    },
                    onAttachmentLongClicked = {
                        onAttachmentLongClicked(attachment)
                    }
                )
            }
            is ChatView.Message.Attachment.Bookmark -> {
                Bookmark(
                    url = attachment.url,
                    title = attachment.title,
                    description = attachment.description,
                    imageUrl = attachment.imageUrl,
                    onClick = {
                        onAttachmentClicked(attachment)
                    },
                    onLongClick = {
                        onAttachmentLongClicked(attachment)
                    }
                )
            }
        }
    }
}

private fun requestPlayingVideoByOS(
    attachment: ChatView.Message.Attachment.Video,
    context: Context
) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(Uri.parse(attachment.url), "video/*")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AttachedObject(
    modifier: Modifier,
    title: String,
    type: String,
    icon: ObjectIcon,
    onAttachmentClicked: () -> Unit = {},
    onAttachmentLongClicked: () -> Unit = {}
) {
    Box(
        modifier = modifier
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
            .combinedClickable(
                onClick = onAttachmentClicked,
                onLongClick = onAttachmentLongClicked
            )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Bookmark(
    url: String,
    title: String,
    description: String,
    imageUrl: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
        ,
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.shape_transparent_secondary)
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.91f),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
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

@DefaultPreviews
@Composable
fun BookmarkPreview() {
    Bookmark(
        url = "algo.tv",
        title = "Algo - Video Automation",
        description = "Algo is a data-visualization studio specializing in video automation.",
        imageUrl = null,
        onClick = {},
        onLongClick = {}
    )
}

@DefaultPreviews
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