package com.anytypeio.anytype.core_ui.widgets.toolbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.editor.EditorViewModel


@Composable
fun ChooseTypeHorizontalWidget(
    state: EditorViewModel.TypesWidgetState,
    onTypeClicked: (EditorViewModel.TypesWidgetItem) -> Unit
) {
    if (state.visible) {
        Box(
            modifier = Modifier
                .height(52.dp)
                .fillMaxWidth()
                .background(color = colorResource(id = R.color.background_primary))
        ) {
            LazyRow(
                contentPadding = PaddingValues(
                    start = 10.dp,
                    end = 10.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(
                    items = state.items,
                    itemContent = { index, item ->
                        when (item) {
                            EditorViewModel.TypesWidgetItem.Search -> {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .border(
                                            width = 1.dp,
                                            color = colorResource(id = R.color.shape_primary),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .noRippleThrottledClickable { onTypeClicked(item) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_search_18),
                                        contentDescription = "Search icon",
                                        modifier = Modifier.wrapContentSize()
                                    )
                                }
                            }

                            is EditorViewModel.TypesWidgetItem.Type -> {
                                Row(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .border(
                                            width = 1.dp,
                                            color = colorResource(id = R.color.shape_primary),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .noRippleThrottledClickable { onTypeClicked(item) },
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
                        }
                    }
                )
            }
        }
    }
}