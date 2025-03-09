package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun BubbleGalleryRowLayout(
    images: List<ChatView.Message.Attachment.Image>,
    onAttachmentClicked: (ChatView.Message.Attachment) -> Unit,
) {
    Row(
        modifier = Modifier
            .width(300.dp)
            .padding(horizontal = 4.dp)
        ,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        images.forEach { image ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(
                        color = colorResource(R.color.shape_tertiary),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .size(64.dp),
                    color = colorResource(R.color.glyph_active),
                    trackColor = colorResource(R.color.glyph_active).copy(alpha = 0.5f),
                    strokeWidth = 8.dp
                )
                GlideImage(
                    model = image.url,
                    contentDescription = "Attachment image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape = RoundedCornerShape(12.dp))
                        .clickable {
                            onAttachmentClicked(image)
                        }
                )
            }
        }
    }
}