package com.anytypeio.anytype.feature_chats.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@Composable
@OptIn(ExperimentalGlideComposeApi::class)
fun ColumnScope.BubbleAttachments(
    attachments: List<ChatView.Message.Attachment>,
    onAttachmentClicked: (ChatView.Message.Attachment) -> Unit,
    isUserAuthor: Boolean
) {
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
                        // Given that the parent
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    index += rowSize
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
                            .clickable {
                                onAttachmentClicked(attachment)
                            }
                    )
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
                    }
                )
            }
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
                color = colorResource(id = R.color.shape_transparent_secondary),
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