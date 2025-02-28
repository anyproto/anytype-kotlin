package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews

@Composable
fun DynamicImageGallery(imageCount: Int) {
    val colors = listOf(
        Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Cyan,
        Color.Magenta, Color.Gray, Color.LightGray, Color.DarkGray, Color.Black
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val images = (0 until imageCount.coerceIn(2, 10)).toList() // Limit to 2-10 images
        val rowConfig = getRowConfiguration(imageCount)

        var index = 0
        rowConfig.forEach { rowSize ->
            RowLayout(images.slice(index until index + rowSize), colors)
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

@Composable
fun RowLayout(images: List<Int>, colors: List<Color>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        images.forEach { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f) // Keeps each item square
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors[index % colors.size])
            )
        }
    }
}

@DefaultPreviews
@Composable
fun GalleryPreview1() {
    DynamicImageGallery(2)
}

@DefaultPreviews
@Composable
fun GalleryPreview2() {
    DynamicImageGallery(3)
}

@DefaultPreviews
@Composable
fun GalleryPreview3() {
    DynamicImageGallery(5)
}

@DefaultPreviews
@Composable
fun GalleryPreview4() {
    DynamicImageGallery(6)
}

@DefaultPreviews
@Composable
fun GalleryPreview5() {
    DynamicImageGallery(7)
}

@DefaultPreviews
@Composable
fun GalleryPreview6() {
    DynamicImageGallery(8)
}


@DefaultPreviews
@Composable
fun GalleryPreview7() {
    DynamicImageGallery(9)
}


@DefaultPreviews
@Composable
fun GalleryPreview8() {
    DynamicImageGallery(10)
}

@DefaultPreviews
@Composable
fun GalleryPreview9() {
    DynamicImageGallery(4)
}