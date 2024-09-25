package com.anytypeio.anytype.feature_allcontent.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.feature_allcontent.R
import com.anytypeio.anytype.feature_allcontent.models.MenuSortsItem
import com.anytypeio.anytype.feature_allcontent.models.AllContentMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentSort

@Composable
fun AllContentMenu(
    mode: List<AllContentMode>,
    onModeClick: (AllContentMode) -> Unit = {},
    sortsItems: List<MenuSortsItem>
) {
    val scrollState = rememberLazyListState()
    var sortingExpanded by remember { mutableStateOf(false) }
    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .width(252.dp)
            .background(
                shape = RoundedCornerShape(size = 16.dp),
                color = colorResource(id = R.color.shape_primary)
            ),
        verticalArrangement = Arrangement.spacedBy(0.5.dp),
    ) {
        items(
            count = mode.size
        ) { index ->
            val item = mode[index]
            MenuItem(
                title = getModeTitle(item),
                isSelected = item.isSelected,
                modifier = Modifier.clickable {
                    onModeClick(item)
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(
            count = if (sortingExpanded) sortsItems.size else 1,
            key = { index -> sortsItems[index].id }
        ) { sortItemsIndex ->
            when (val item = sortsItems[sortItemsIndex]) {
                is MenuSortsItem.Container -> {
                    SortingBox(
                        modifier = Modifier
                            .animateItem()
                            .clickable {
                                sortingExpanded = !sortingExpanded
                            },
                        subtitle = item.sort.text()
                    )
                }

                is MenuSortsItem.Sort -> {
                    MenuItem(
                        title = item.sort.text(),
                        isSelected = item.sort.isSelected,
                        modifier = Modifier
                            .animateItem()
                            .clickable {
                                //onSortClick(item)
                            }
                    )
                }

                is MenuSortsItem.Spacer -> {
                    Spacer(modifier = Modifier.height(7.5.dp))
                }

                is MenuSortsItem.SortType -> {
                    MenuItem(
                        title = GetSortTypeName(item.sort, item.sortType),
                        isSelected = item.isSelected,
                        modifier = Modifier
                            .animateItem()
                            .clickable {
                                //onSortClick(item)
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun GetSortTypeName(sort: AllContentSort, type: DVSortType): String {
    return when (type) {
        Block.Content.DataView.Sort.Type.ASC -> {
            when (sort) {
                is AllContentSort.ByDateCreated, is AllContentSort.ByDateUpdated -> stringResource(
                    id = R.string.all_content_sort_date_asc
                )

                is AllContentSort.ByName -> stringResource(id = R.string.all_content_sort_name_asc)
            }
        }

        Block.Content.DataView.Sort.Type.DESC -> {
            when (sort) {
                is AllContentSort.ByDateCreated, is AllContentSort.ByDateUpdated -> stringResource(
                    id = R.string.all_content_sort_date_desc
                )

                is AllContentSort.ByName -> stringResource(id = R.string.all_content_sort_name_desc)
            }
        }

        Block.Content.DataView.Sort.Type.CUSTOM -> ""
    }
}

@Composable
private fun LazyItemScope.SortingBox(modifier: Modifier, subtitle: String) {
    Row(
        modifier = modifier
            .fillParentMaxWidth()
            .background(colorResource(id = R.color.background_secondary)),
        verticalAlignment = CenterVertically
    ) {
        Image(
            modifier = Modifier.size(32.dp),
            painter = painterResource(R.drawable.ic_menu_arrow_right),
            contentDescription = "",
            colorFilter = tint(colorResource(id = R.color.glyph_selected))
        )
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .padding(top = 11.dp, bottom = 10.dp, start = 6.dp)
        ) {
            Text(
                text = stringResource(id = R.string.all_content_sort_by),
                modifier = Modifier.wrapContentSize(),
                style = UXBody,
                color = colorResource(id = R.color.text_primary)
            )
            Text(
                text = subtitle,
                modifier = Modifier.wrapContentSize(),
                style = BodyCalloutRegular,
                color = colorResource(id = R.color.text_secondary)
            )
        }
    }
}

@Composable
private fun LazyItemScope.MenuItem(modifier: Modifier, title: String, isSelected: Boolean) {
    Row(
        modifier = modifier
            .fillParentMaxWidth()
            .height(44.dp)
            .background(colorResource(id = R.color.background_secondary)),
        verticalAlignment = CenterVertically,
    ) {
        Image(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 12.dp),
            painter = painterResource(R.drawable.ic_check_16),
            contentDescription = "All Content mode selected",
            alpha = if (isSelected) 1f else 0f
        )
        Text(
            text = title,
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 8.dp),
            style = UXBody,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Composable
private fun getModeTitle(mode: AllContentMode): String {
    return when (mode) {
        is AllContentMode.AllContent -> stringResource(id = R.string.all_content_title_all_content)
        is AllContentMode.Unlinked -> stringResource(id = R.string.all_content_title_only_unlinked)
    }
}

@DefaultPreviews
@Composable
fun AllContentMenuPreview() {
    AllContentMenu(
        mode = listOf(
            AllContentMode.AllContent(isSelected = true),
            AllContentMode.Unlinked(isSelected = false)
        ),
        sortsItems = listOf(
            MenuSortsItem.Container(
                sort = AllContentSort.ByName(isSelected = true)
            ),
            MenuSortsItem.Sort(
                id = "byName",
                sort = AllContentSort.ByName(isSelected = true)
            ),
            MenuSortsItem.Sort(
                id = "byDateUpdated",
                AllContentSort.ByDateUpdated(isSelected = false)
            ),
            MenuSortsItem.Sort(
                id = "byDateCreated",
                AllContentSort.ByDateCreated(isSelected = false)
            ),
            MenuSortsItem.Spacer(),
            MenuSortsItem.SortType(
                sortType = DVSortType.ASC,
                isSelected = true,
                sort = AllContentSort.ByName(isSelected = true)
            ),
            MenuSortsItem.SortType(
                sortType = DVSortType.DESC,
                isSelected = false,
                sort = AllContentSort.ByName(isSelected = false)
            ),
        )
    )
}