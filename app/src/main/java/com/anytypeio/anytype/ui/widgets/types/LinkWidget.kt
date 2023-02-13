package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.DropDownMenuAction
import com.anytypeio.anytype.ui.widgets.menu.WidgetMenu

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LinkWidgetCard(
    item: WidgetView.Link,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit
) {
    val isDropDownMenuExpanded = remember {
        mutableStateOf(false)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .combinedClickable(
                    onClick = { onWidgetObjectClicked(item.obj) },
                    onLongClick = {
                        isDropDownMenuExpanded.value = !isDropDownMenuExpanded.value
                    }
                )
        ) {
            Text(
                text = item.obj.name.orEmpty().trim(),
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
            )
        }
        WidgetMenu(
            isExpanded = isDropDownMenuExpanded,
            onDropDownMenuAction = onDropDownMenuAction
        )
    }
}