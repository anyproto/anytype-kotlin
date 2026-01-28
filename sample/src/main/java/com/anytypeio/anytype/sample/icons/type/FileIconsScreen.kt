package com.anytypeio.anytype.sample.icons.type

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_utils.const.MimeTypes
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.sample.R

@Composable
fun FileIconsScreen() {
    val items = listOf(
        Pair(120.dp, "File icon default, size 120"),
        Pair(96.dp, "File icon default, size 96"),
        Pair(80.dp, "File icon default, size 80"),
        Pair(64.dp, "File icon default, size 64"),
        Pair(48.dp, "File icon default, size 48"),
        Pair(40.dp, "File icon default, size 40"),
        Pair(32.dp, "File icon default, size 32"),
        Pair(20.dp, "File icon default, size 20"),
        Pair(18.dp, "File icon default, size 18"),
        Pair(16.dp, "File icon default, size 16")
    )

    val basicModifier = Modifier
        .wrapContentHeight()
        .fillMaxWidth()
        .padding(horizontal = 16.dp)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp)
    ) {
        // Header
        item {
            Text(
                text = "File Default Icons, all sizes",
                style = Title1,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        // List items
        items(
            count = items.size,
            key = { index -> items[index].second }
        ) { index ->
            val (containerSize, label) = items[index]
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    icon = ObjectIcon.File(
                        mime = "video/mp4",
                    ),
                    modifier = Modifier.padding(vertical = 16.dp),
                    iconSize = containerSize
                )

                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = label,
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Divider()
        }
    }
}

@Composable
fun FileIconsAllMimeTypesScreen() {
    val items = listOf(
        MimeTypes.Category.TEXT to "text/plain",
        MimeTypes.Category.TABLE to "application/vnd.ms-excel",
        MimeTypes.Category.PRESENTATION to "application/vnd.ms-powerpoint",
        MimeTypes.Category.IMAGE to "image/jpeg",
        MimeTypes.Category.VIDEO to "video/mp4",
        MimeTypes.Category.AUDIO to "audio/mp3",
        MimeTypes.Category.ARCHIVE to "application/zip",
        MimeTypes.Category.PDF to "application/pdf",
        MimeTypes.Category.OTHER to "application/x-anytype-object"
    )
    val sizes = listOf(120.dp, 96.dp, 80.dp, 64.dp, 48.dp, 40.dp, 32.dp, 20.dp, 18.dp, 16.dp)
    var selectedSize by remember { mutableStateOf(32.dp) }

    val basicModifier = Modifier
        .wrapContentHeight()
        .fillMaxWidth()
        .padding(horizontal = 16.dp)

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        LazyRow(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            items(sizes) { size ->
                OutlinedButton(onClick = { selectedSize = size }) {
                    Text(text = "${size.value.toInt()}")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // Header
            item {
                Text(
                    text = "File Icons by MimeType Category",
                    style = Title1,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // List items
            items(
                count = items.size,
                key = { index -> items[index].first.name + selectedSize.value }
            ) { index ->
                val (category, mime) = items[index]
                Row(
                    modifier = basicModifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ListWidgetObjectIcon(
                        icon = ObjectIcon.File(
                            mime = mime,
                        ),
                        modifier = Modifier.padding(vertical = 16.dp),
                        iconSize = selectedSize
                    )

                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = "Category: ${category.name}, size ${selectedSize.value.toInt()}",
                        style = Title2,
                        color = colorResource(id = R.color.text_primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Divider()
            }
        }
    }
}