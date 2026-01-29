package com.anytypeio.anytype.sample.icons.type

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ui.CustomIconColor
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.sample.R

private val SIZES = listOf(
    120.dp,
    96.dp,
    80.dp,
    64.dp,
    48.dp,
    40.dp,
    32.dp,
    20.dp,
    18.dp,
    16.dp
)

@Composable
fun TypeIconsEmojiScreen() {

    val items = listOf(
        ObjectIcon.TypeIcon.Emoji(
            unicode = "😀",
            rawValue = "document",
            color = CustomIconColor.Red
        ) to "Type icon emoji",

        ObjectIcon.TypeIcon.Fallback(
            rawValue = "document"
        ) to "Type icon fallback",

        ObjectIcon.TypeIcon.Deleted to "Type icon deleted"
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
                text = "Type Emoji Icons, all sizes",
                style = Title1,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        items(
            count = items.size
        ) { index ->
            val (icon, label) = items[index]
            Text(
                text = label,
                modifier = Modifier.padding(16.dp)
            )
            SIZES.forEach { size ->
                Row(
                    modifier = basicModifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ListWidgetObjectIcon(
                        icon = icon,
                        modifier = Modifier.padding(vertical = 16.dp),
                        iconSize = size
                    )

                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = "size = $size",
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