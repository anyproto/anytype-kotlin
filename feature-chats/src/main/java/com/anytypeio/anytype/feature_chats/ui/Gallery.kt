package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@Composable
fun DynamicImageGallery(imageUrls: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp) // 4dp padding between rows
    ) {
        val rowConfig = getRowConfiguration(imageUrls.size)

        var index = 0
        rowConfig.forEach { rowSize ->
            RowLayout(imageUrls.slice(index until index + rowSize))
            index += rowSize
        }
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
fun RowLayout(imageUrls: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        imageUrls.forEach { url ->
            GlideImage(
                model = url,
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