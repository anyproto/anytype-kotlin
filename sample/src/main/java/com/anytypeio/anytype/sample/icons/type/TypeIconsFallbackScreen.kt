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
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.sample.R

@Composable
fun TypeIconsFallbackScreen() {

    val items = listOf(
        Pair(120.dp, "Type icon fallback, size 120"),
        Pair(96.dp, "Type icon fallback, size 96"),
        Pair(80.dp, "Type icon fallback, size 80"),
        Pair(64.dp, "Type icon fallback, size 64"),
        Pair(48.dp, "Type icon fallback, size 48"),
        Pair(40.dp, "Type icon fallback, size 40"),
        Pair(32.dp, "Type icon fallback, size 32"),
        Pair(20.dp, "Type icon fallback, size 20"),
        Pair(18.dp, "Type icon fallback, size 18"),
        Pair(16.dp, "Type icon fallback, size 16")
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
                text = "Type Fallback Icons, all sizes",
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
                    icon = ObjectIcon.TypeIcon.Fallback(
                        rawValue = "document",
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