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
import androidx.compose.ui.unit.dp
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
    30.dp,
    26.dp,
    22.dp,
    20.dp,
    18.dp,
    16.dp
)

@Composable
fun BasicIconsScreen() {
    val items = listOf(
        ObjectIcon.Basic.Emoji("😀",) to "Basic.Emoji",
        ObjectIcon.Basic.Image(
            hash = "https://samplelib.com/lib/preview/png/sample-red-400x300.png"
        ) to "Basic.Image",
        ObjectIcon.Profile.Avatar(
            name = "A"
        ) to "Profile.Avatar",
        ObjectIcon.Profile.Image(
            hash = "sample_hash",
            name = "Profile Image"
        ) to "Profile.Image"
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
        item {
            Text(
                text = "Basic & Profile Icons, all sizes",
                style = Title1,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
        items(count = items.size) { index ->
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
                        style = Title2
                    )
                }
                Divider()
            }
        }
    }
} 