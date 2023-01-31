package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.WidgetView
import timber.log.Timber

@Composable
fun HomeScreen(
    widgets: List<WidgetView>,
    onExpand: (TreePath) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Timber.d("Widgets:\n$widgets")
        items(
            items = widgets, itemContent = { item ->
                when (item) {
                    is WidgetView.Tree -> {
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = item.obj.name.orEmpty().trim(),
                                    style = MaterialTheme.typography.h6,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                item.elements.forEach { element ->
                                    Row(
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                            .clickable {
                                                onExpand(element.path)
                                            }
                                    ) {
                                        if (element.indent > 0) {
                                            Spacer(Modifier.width(20.dp.times(element.indent)))
                                        }
                                        Box(
                                            Modifier
                                                .width(20.dp)
                                                .height(20.dp)
                                        ) {
                                            when(val icon = element.icon) {
                                                is WidgetView.Tree.Icon.Branch -> {
                                                    Text(
                                                        text = ">",
                                                        modifier = Modifier
                                                            .align(Alignment.Center)
                                                            .rotate(if (icon.isExpanded) 90f else 0f)
                                                    )
                                                }
                                                is WidgetView.Tree.Icon.Leaf -> {
                                                    Text(
                                                        text = "•",
                                                        modifier = Modifier.align(Alignment.Center)
                                                    )
                                                }
                                                is WidgetView.Tree.Icon.Set -> {
                                                    Text(
                                                        text = "#",
                                                        modifier = Modifier.align(Alignment.Center)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = element.obj.name?.trim() ?: "Untitled"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}