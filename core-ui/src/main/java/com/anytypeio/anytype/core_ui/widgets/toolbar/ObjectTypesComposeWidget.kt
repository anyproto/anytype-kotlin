package com.anytypeio.anytype.core_ui.widgets.toolbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.EditorViewModel.TypesWidgetItem
import com.anytypeio.anytype.presentation.objects.ObjectTypeView


@Preview(showBackground = true, apiLevel = 33)
@Composable
fun MyChooseTypeHorizontalWidget() {
    val state = EditorViewModel.TypesWidgetState(
        visible = true,
        items = listOf(
            TypesWidgetItem.Search,
            TypesWidgetItem.Type(
                item = ObjectTypeView(
                    emoji = "👍",
                    name = "Like",
                    id = "12312",
                    description = null,
                    key = "dd"
                )
            )
        ),
        expanded = true
    )
    ChooseTypeHorizontalWidget(state = state, onTypeClicked = {})
}

@Preview(showBackground = true, apiLevel = 33)
@Composable
fun MyChooseTypeHorizontalWidgetCollapsed() {
    val state = EditorViewModel.TypesWidgetState(
        visible = true,
        items = listOf(
            TypesWidgetItem.Search,
            TypesWidgetItem.Type(
                item = ObjectTypeView(
                    emoji = "👍",
                    name = "Like",
                    id = "12312",
                    description = null,
                    key = "dd"
                )
            )
        ),
        expanded = false
    )
    ChooseTypeHorizontalWidget(state = state, onTypeClicked = {})
}

@Composable
fun ChooseTypeHorizontalWidget(
    state: EditorViewModel.TypesWidgetState,
    onTypeClicked: (TypesWidgetItem) -> Unit
) {
    if (state.visible) {
        if (state.expanded) {
            ChooseTypeHorizontalWidgetExpanded(state = state, action = onTypeClicked)
        } else {
            ChooseTypeHorizontalWidgetCollapsed(state = state, action = onTypeClicked)
        }
    }
}

@Composable
fun ChooseTypeHorizontalWidgetExpanded(
    state: EditorViewModel.TypesWidgetState,
    action: (TypesWidgetItem) -> Unit
) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(color = colorResource(id = R.color.background_primary))
    ) {
        ChooseTypeHeader(state = state, action = action)
        LazyRow(
            contentPadding = PaddingValues(
                start = 10.dp,
                end = 10.dp,
                bottom = 6.dp,
                top = 6.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(
                items = state.items,
                itemContent = { index, item ->
                    when (item) {
                        TypesWidgetItem.Search -> {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(
                                        width = 1.dp,
                                        color = colorResource(id = R.color.shape_primary),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .noRippleThrottledClickable { action(item) },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_search_18),
                                    contentDescription = "Search icon",
                                    modifier = Modifier.wrapContentSize()
                                )
                            }
                        }

                        is TypesWidgetItem.Type -> {
                            Row(
                                modifier = Modifier
                                    .height(40.dp)
                                    .border(
                                        width = 1.dp,
                                        color = colorResource(id = R.color.shape_primary),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .noRippleThrottledClickable { action(item) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(modifier = Modifier.width(12.dp))
                                val uri = Emojifier.safeUri(item.item.emoji.orEmpty())
                                if (uri.isNotEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(uri),
                                        contentDescription = "Icon from URI",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = item.item.name,
                                    style = Caption1Medium,
                                    color = colorResource(id = R.color.text_primary),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }
                        }

                        else -> {
                            // Do nothing
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ChooseTypeHorizontalWidgetCollapsed(
    state: EditorViewModel.TypesWidgetState,
    action: (TypesWidgetItem) -> Unit
) {
    ChooseTypeHeader(state = state, action = action)
}

@Composable
private fun ChooseTypeHeader(
    state: EditorViewModel.TypesWidgetState,
    action: (TypesWidgetItem) -> Unit
) {
    val (title, icon) = if (state.expanded) {
        Pair(
            stringResource(id = R.string.widget_types_hide),
            painterResource(id = R.drawable.ic_list_arrow_18)
        )
    } else {
        Pair(
            stringResource(id = R.string.widget_types_show),
            painterResource(id = R.drawable.ic_arrow_up_18)
        )
    }
    Box(
        modifier = Modifier
            .height(44.dp)
            .fillMaxWidth()
            .background(color = colorResource(id = R.color.background_primary)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .noRippleThrottledClickable {
                    if (state.expanded) {
                        action(TypesWidgetItem.Collapse)
                    } else {
                        action(TypesWidgetItem.Expand)
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = title,
                style = BodyRegular,
                color = colorResource(id = R.color.text_secondary)
            )
            Image(
                painter = icon,
                contentDescription = "Arrow icon",
                modifier = Modifier.padding(start = 6.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .noRippleThrottledClickable { action(TypesWidgetItem.Done) }
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp),
                text = stringResource(id = R.string.done),
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
        }
    }
}