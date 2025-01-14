package com.anytypeio.anytype.feature_discussions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.feature_discussions.R
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionView
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AttachmentGallery(items: List<DiscussionView.Message.Attachment.Image>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp)
    ) {
        items(items) { attachment ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
//            .padding(
//                start = 4.dp,
//                end = 4.dp,
//                bottom = 4.dp,
//                top = if (idx == 0) 4.dp else 0.dp
//            )
                    .background(
                        color = colorResource(R.color.shape_tertiary),
                        shape = RoundedCornerShape(16.dp)
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
                    model = attachment.url,
                    contentDescription = "Attachment image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(300.dp)
                        .clip(shape = RoundedCornerShape(16.dp))
                        .clickable {
                            //
                        }
                )
            }
        }
    }
}
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(IntrinsicSize.Max)
//    ) {
//        when (items.size) {
//            1 -> {
//                SingleItem(items.first())
//            }
//            2 -> {
//                RowItems(
//                    items,
//                    modifier = Modifier
//                        .weight(1.0f)
//                )
//            }
//            3 -> {
//                SingleItem(items.first())
//                Spacer(modifier = Modifier.height(4.dp))
//                RowItems(items.drop(1))
//            }
//            4 -> {
//                RowItems(items.take(2))
//                Spacer(modifier = Modifier.height(4.dp))
//                RowItems(items.drop(2))
//            }
//            5 -> {
//                RowItems(items.take(2))
//                Spacer(modifier = Modifier.height(4.dp))
//                RowItems(items.drop(2))
//            }
//            6 -> {
//                RowItems(items.take(3))
//                Spacer(modifier = Modifier.height(4.dp))
//                RowItems(items.drop(3))
//            }
//            7 -> {
//                RowItems(items.slice(0..1))
//                Spacer(modifier = Modifier.height(4.dp))
//                RowItems(items.slice(2..3))
//                Spacer(modifier = Modifier.height(4.dp))
//                RowItems(items.slice(4..6))
//            }
//            8 -> {
//                RowItems(items.slice(0..1))
//                Spacer(modifier = Modifier.height(4.dp))
//                RowItems(items.slice(2..4))
//                Spacer(modifier = Modifier.height(4.dp))
//                RowItems(items.slice(5..7))
//            }
//            9 -> {
//                RowItems(items.slice(0..2))
//                Spacer(modifier = Modifier.height(4.dp))
//                RowItems(items.slice(3..5))
//                Spacer(modifier = Modifier.height(4.dp))
//                RowItems(items.slice(6..8))
//            }
//            10 -> {
//                RowItems(items.slice(0..1))
//                Spacer(modifier = Modifier.height(4.dp))
//                RowItems(items.slice(2..3))
//                Spacer(modifier = Modifier.height(4.dp))
//                RowItems(items.slice(4..6))
//                Spacer(modifier = Modifier.height(4.dp))
//                RowItems(items.slice(7..9))
//            }
//            else -> {
//                // Handle cases for more than 6 items if necessary
//            }
//        }
//    }

@Composable
fun SingleItem(item: DiscussionView.Message.Attachment.Image) {
    Item(
        attachment = item,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}

@Composable
fun RowItems(
    items: List<DiscussionView.Message.Attachment.Image>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { item ->
            Item(
                attachment = item,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun Item(
    attachment: DiscussionView.Message.Attachment.Image,
    modifier: Modifier = Modifier,
    onAttachmentClicked: (DiscussionView.Message.Attachment.Image) -> Unit = {}
) {
    Box(
        modifier = modifier
//            .padding(
//                start = 4.dp,
//                end = 4.dp,
//                bottom = 4.dp,
//                top = if (idx == 0) 4.dp else 0.dp
//            )
            .background(
                color = colorResource(R.color.shape_tertiary),
                shape = RoundedCornerShape(16.dp)
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
            model = attachment.url,
            contentDescription = "Attachment image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
//                .size(300.dp)
                .clip(shape = RoundedCornerShape(16.dp))
                .clickable {
                    onAttachmentClicked(attachment)
                }
        )
    }
}

//@DefaultPreviews
//@Composable
//fun AttachmentGalleryOneItemPreview() {
//    AttachmentGallery(
//        items = listOf("A")
//    )
//}
//
//@DefaultPreviews
//@Composable
//fun AttachmentGalleryTwoItemsPreview() {
//    AttachmentGallery(
//        items = listOf("A", "B")
//    )
//}
//
//@DefaultPreviews
//@Composable
//fun AttachmentGalleryThreeItemsPreview() {
//    AttachmentGallery(
//        items = listOf("A", "B", "C")
//    )
//}
//
//@DefaultPreviews
//@Composable
//fun AttachmentGalleryFourItemsPreview() {
//    AttachmentGallery(
//        items = listOf("A", "B", "C", "D")
//    )
//}
//
//@DefaultPreviews
//@Composable
//fun AttachmentGalleryFiveItemsPreview() {
//    AttachmentGallery(
//        items = listOf("A", "B", "C", "D", "E")
//    )
//}
//
//@DefaultPreviews
//@Composable
//fun AttachmentGallery6ItemsPreview() {
//    AttachmentGallery(
//        items = listOf("A", "B", "C", "D", "E", "F")
//    )
//}
//
//@DefaultPreviews
//@Composable
//fun AttachmentGallery7ItemsPreview() {
//    AttachmentGallery(
//        items = listOf("A", "B", "C", "D", "E", "F", "G")
//    )
//}
//
//@DefaultPreviews
//@Composable
//fun AttachmentGallery8ItemsPreview() {
//    AttachmentGallery(
//        items = listOf("1", "2", "3", "4", "5", "6", "7", "8")
//    )
//}
//
//@DefaultPreviews
//@Composable
//fun AttachmentGallery9ItemsPreview() {
//    AttachmentGallery(
//        items = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
//    )
//}
//
//@DefaultPreviews
//@Composable
//fun AttachmentGallery10ItemsPreview() {
//    AttachmentGallery(
//        items = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
//    )
//}