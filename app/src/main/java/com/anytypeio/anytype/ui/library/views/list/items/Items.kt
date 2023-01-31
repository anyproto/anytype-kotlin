package com.anytypeio.anytype.ui.library.views.list.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.core_ui.widgets.RelationFormatIconWidget
import com.anytypeio.anytype.presentation.navigation.LibraryView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.ui.library.views.list.items.ItemDefaults.TEXT_PADDING_START

@Composable
fun MyTypeItem(item: LibraryView, modifier: Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon = item.icon)
        Text(
            text = item.name,
            modifier = Modifier
                .padding(start = TEXT_PADDING_START)
        )
    }
}

@Composable
fun LibTypeItem(item: LibraryView, modifier: Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon = item.icon)
        Text(
            text = item.name,
            modifier = Modifier
                .padding(start = TEXT_PADDING_START)
        )
    }
}

@Composable
fun MyRelationItem(item: LibraryView, modifier: Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.name,
            modifier = Modifier
                .padding(start = TEXT_PADDING_START)
        )
    }
}

@Composable
fun LibRelationItem(item: LibraryView, modifier: Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.name,
            modifier = Modifier
                .padding(start = TEXT_PADDING_START)
        )
    }
}

@Composable
fun Icon(icon: ObjectIcon) {
    AndroidView(factory = { ctx ->
        ObjectIconWidget(ctx).apply {
            setIcon(icon)
        }
    })
}


@Immutable
object ItemDefaults {
    val ITEM_HEIGHT = 52.dp
    val TEXT_PADDING_START = 10.dp
}
