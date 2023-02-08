package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.WidgetView
import timber.log.Timber

@Composable
fun HomeScreen(
    widgets: List<WidgetView>,
    onExpand: (TreePath) -> Unit,
    onCreateWidget: () -> Unit,
    onEditWidgets: () -> Unit,
    onRefresh: () -> Unit,
    onDeleteWidget: (Id) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Timber.d("Widgets:\n$widgets")
        items(
            items = widgets, itemContent = { item ->
                when (item) {
                    is WidgetView.Tree -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Circle {
                                onDeleteWidget(item.id)
                            }
                            Column(Modifier.padding(16.dp)) {
                                TreeWidgetHeader(item)
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
                                            when (val icon = element.icon) {
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
                    is WidgetView.Link -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(Modifier.fillMaxHeight()) {
                                Text(
                                    text = item.obj.name.orEmpty().trim(),
                                    style = MaterialTheme.typography.h6,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(start = 16.dp)
                                )
                            }
                        }
                        Circle {
                            onDeleteWidget(item.id)
                        }
                    }
                    is WidgetView.Action.CreateWidget -> {
                        Box(Modifier.fillMaxWidth()) {
                            WidgetActionButton(
                                label = stringResource(R.string.create_widget),
                                onClick = onCreateWidget,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    is WidgetView.Action.EditWidgets -> {
                        Box(Modifier.fillMaxWidth()) {
                            WidgetActionButton(
                                label = stringResource(R.string.edit_widgets),
                                onClick = onEditWidgets,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    is WidgetView.Action.Refresh -> {
                        Box(Modifier.fillMaxWidth()) {
                            WidgetActionButton(
                                label = "Refresh (for testing)",
                                onClick = onRefresh,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun TreeWidgetHeader(item: WidgetView.Tree) {
    Row() {
        Text(
            // TODO trimming should be a part of presentation module.
            text = item.obj.name.orEmpty().trim(),
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = ">",
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .rotate(90f)
        )
    }
}

@Composable
fun WidgetActionButton(
    modifier: Modifier,
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(id = R.color.text_primary),
            contentColor = colorResource(id = R.color.widget_button)
        ),
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = label)
    }
}

@Composable
fun Circle(
    onClick: () -> Unit
) {
    Box(
        Modifier
            .height(24.dp)
            .width(24.dp)) {
        Canvas(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterEnd)
                .padding(4.dp)
                .clickable { onClick() },
            onDraw = {
                drawCircle(color = Color.Red)
            }
        )
    }
}
