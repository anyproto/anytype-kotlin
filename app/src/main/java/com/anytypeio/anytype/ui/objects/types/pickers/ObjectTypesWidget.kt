package com.anytypeio.anytype.ui.objects.types.pickers

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.objects.SelectTypeView

@Composable
fun ChooseTypeWidget(
    state: EditorViewModel.TypesWidgetState,
    onTypeClicked: (SelectTypeView.Type) -> Unit
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
                    items = state.views,
                    itemContent = { _, view ->
                        when (view) {
                            SelectTypeView.Search -> {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .border(
                                            width = 1.dp,
                                            color = colorResource(id = R.color.shape_primary),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .noRippleThrottledClickable { },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_search_18),
                                        contentDescription = "Search icon",
                                        modifier = Modifier.wrapContentSize()
                                    )
                                }
                            }
                            is SelectTypeView.Type -> {
                                Row(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .border(
                                            width = 1.dp,
                                            color = colorResource(id = R.color.shape_primary),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .noRippleThrottledClickable { onTypeClicked(view) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    val uri = Emojifier.safeUri(view.icon)
                                    if (uri.isNotEmpty()) {
                                        Image(
                                            painter = rememberAsyncImagePainter(uri),
                                            contentDescription = "Icon from URI",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = view.name,
                                        style = Caption1Medium,
                                        color = colorResource(id = R.color.text_primary),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChooseTypeWidget() {
    ChooseTypeWidget(state = EditorViewModel.TypesWidgetState(
        views = listOf(
            SelectTypeView.Search, SelectTypeView.Type(
                id = "voluptatibus",
                typeKey = "persecuti",
                name = "Books",
                icon = "📚",
                isFromLibrary = false
            )
        ),
        visible = true
    ), onTypeClicked = {})
}