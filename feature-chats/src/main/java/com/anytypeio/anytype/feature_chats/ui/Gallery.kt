package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@Composable
fun DynamicImageGallery(gallery: ChatView.Message.Attachment.Gallery) {
    val rowConfig = getRowConfiguration(gallery.images.size)
    var index = 0
    rowConfig.forEach { rowSize ->
        RowLayout(gallery.images.slice(index until index + rowSize))
        index += rowSize
    }
}

fun getRowConfiguration(imageCount: Int): List<Int> {
    return when (imageCount) {
        2 -> listOf(2)
        3 -> listOf(3)
        4 -> listOf(2, 2)
        5 -> listOf(2, 3)
        6 -> listOf(3, 3)
        7 -> listOf(2, 2, 3)
        8 -> listOf(2, 3, 3)
        9 -> listOf(3, 3, 3)
        10 -> listOf(2, 3, 3, 2)
        else -> listOf()
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun RowLayout(images: List<ChatView.Message.Attachment.Image>) {
    Row(
        modifier = Modifier.width(292.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        images.forEach { image ->
            GlideImage(
                model = image.url,
                contentDescription = "Gallery Image",
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f) // Keeps each image square
                    .clip(RoundedCornerShape(8.dp)), // Rounded corners
                contentScale = ContentScale.Crop // Crop to fit the image
            )
        }
    }
}