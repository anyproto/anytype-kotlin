package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.DropDownMenuAction

@Composable
fun ListWidgetCard(
    item: WidgetView.Set,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onChangeWidgetView: (Id, Id) -> Unit
) {
    val isDropDownMenuExpanded = remember {
        mutableStateOf(false)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                Modifier
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                WidgetHeader(
                    item = item.obj,
                    isDropDownMenuExpanded = isDropDownMenuExpanded,
                    onWidgetObjectClicked = onWidgetObjectClicked
                )
            }
            LazyRow(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(
                    items = item.tabs,
                    itemContent = { tab ->
                        Text(
                            text = tab.name,
                            fontSize = 15.sp,
                            color = if (tab.isSelected)
                                colorResource(id = R.color.text_primary)
                            else
                                colorResource(id = R.color.glyph_active),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .clickable { onChangeWidgetView(item.id, tab.id) }
                        )
                    }
                )
            }
            item.elements.forEachIndexed { idx, element ->
                Box(
                    modifier = Modifier
                        .clickable(onClick = { onWidgetObjectClicked(element) })
                        .height(72.dp)
                        .fillMaxWidth()
                        .padding(end = 8.dp)
                ) {
                    val hasDescription = element.description?.isNotEmpty() ?: false
                    Text(
                        text = element.name.orEmpty(),
                        fontSize = 15.sp,
                        modifier = if (hasDescription)
                            Modifier
                                .padding(
                                    top = 18.dp,
                                    start = 16.dp
                                )
                        else
                            Modifier
                                .padding(start = 16.dp)
                                .align(Alignment.CenterStart),
                        color = colorResource(id = R.color.text_primary),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (hasDescription) {
                        Text(
                            text = element.description.orEmpty(),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(
                                top = 39.dp,
                                start = 16.dp
                            ),
                            color = colorResource(id = R.color.text_secondary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Divider(
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(end = 16.dp, start = 8.dp)
                )
                if (idx == item.elements.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}