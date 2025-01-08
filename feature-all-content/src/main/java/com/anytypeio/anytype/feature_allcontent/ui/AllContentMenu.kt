package com.anytypeio.anytype.feature_allcontent.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.feature_allcontent.R
import com.anytypeio.anytype.feature_allcontent.models.AllContentMenuMode
import com.anytypeio.anytype.feature_allcontent.models.MenuSortsItem
import com.anytypeio.anytype.feature_allcontent.models.UiMenuState
import com.anytypeio.anytype.presentation.objects.AllContentSort

@Composable
fun AllContentMenu(
    uiMenuState: UiMenuState.Visible,
    onModeClick: (AllContentMenuMode) -> Unit,
    onSortClick: (AllContentSort) -> Unit,
    onBinClick: () -> Unit
) {
    var sortingExpanded by remember { mutableStateOf(false) }

    uiMenuState.mode.forEach { item ->
        MenuItem(
            title = getModeTitle(item),
            isSelected = item.isSelected,
            modifier = Modifier.clickable {
                onModeClick(item)
            }
        )
        Divider(0.5.dp)
    }
    if (uiMenuState.mode.isNotEmpty()) {
        Divider(7.5.dp)
    }
    SortingBox(
        modifier = Modifier
            .clickable {
                sortingExpanded = !sortingExpanded
            },
        subtitle = uiMenuState.container.sort.title(),
        isExpanded = sortingExpanded
    )
    Divider(0.5.dp)
    if (sortingExpanded) {
        uiMenuState.sorts.forEach { item ->
            MenuItem(
                title = item.sort.title(),
                isSelected = item.sort.isSelected,
                modifier = Modifier
                    .clickable {
                        onSortClick(item.sort)
                    }
            )
            Divider(0.5.dp)
        }
        Divider(7.5.dp)
        uiMenuState.types.forEachIndexed { index, item ->
            MenuItem(
                title = item.sortType.title(item.sort),
                isSelected = item.isSelected,
                modifier = Modifier
                    .clickable {
                        val updatedSort = when (item.sort) {
                            is AllContentSort.ByName -> item.sort.copy(sortType = item.sortType)
                            is AllContentSort.ByDateCreated -> item.sort.copy(sortType = item.sortType)
                            is AllContentSort.ByDateUpdated -> item.sort.copy(sortType = item.sortType)
                            is AllContentSort.ByDateUsed -> item.sort.copy(sortType = item.sortType)
                        }
                        onSortClick(updatedSort)
                    }
            )
            if (index < uiMenuState.types.size - 1) {
                Divider(0.5.dp)
            }
        }
    }
    if (uiMenuState.showBin && !sortingExpanded) {
        Divider(7.5.dp)
        MenuItem(
            title = stringResource(id = R.string.all_content_view_bin),
            isSelected = false,
            modifier = Modifier.clickable { onBinClick() }
        )
    }
}

@Composable
private fun Divider(height: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(colorResource(id = R.color.shape_secondary))
    )
}

@Composable
private fun SortingBox(modifier: Modifier, subtitle: String, isExpanded: Boolean) {
    val rotationAngle = if (isExpanded) 90f else 0f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.background_secondary)),
        verticalAlignment = CenterVertically
    ) {
        Image(
            modifier = Modifier
                .padding(start = 10.dp)
                .size(18.dp)
                .rotate(rotationAngle),
            painter = painterResource(R.drawable.ic_arrow_disclosure_18),
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
private fun MenuItem(modifier: Modifier, title: String, isSelected: Boolean) {
    Row(
        modifier = modifier
            .fillMaxWidth()
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

//region RESOURCES
@Composable
private fun getModeTitle(mode: AllContentMenuMode): String = stringResource(
    when (mode) {
        is AllContentMenuMode.AllContent -> R.string.all_content_title_all_content
        is AllContentMenuMode.Unlinked -> R.string.all_content_title_only_unlinked
    }
)

@Composable
private fun AllContentSort.title(): String = stringResource(
    when (this) {
        is AllContentSort.ByDateCreated -> R.string.all_content_sort_date_created
        is AllContentSort.ByDateUpdated -> R.string.all_content_sort_date_updated
        is AllContentSort.ByName -> R.string.all_content_sort_name
        is AllContentSort.ByDateUsed -> R.string.all_content_sort_date_used
    }
)

@Composable
private fun DVSortType.title(sort: AllContentSort): String = when (this) {
    DVSortType.ASC -> {
        when (sort) {
            is AllContentSort.ByDateCreated, is AllContentSort.ByDateUpdated, is AllContentSort.ByDateUsed -> stringResource(
                id = R.string.all_content_sort_date_asc
            )

            is AllContentSort.ByName -> stringResource(id = R.string.all_content_sort_name_asc)
        }
    }

    DVSortType.DESC -> {
        when (sort) {
            is AllContentSort.ByDateCreated,
            is AllContentSort.ByDateUpdated,
            is AllContentSort.ByDateUsed -> stringResource(
                id = R.string.all_content_sort_date_desc
            )

            is AllContentSort.ByName -> stringResource(id = R.string.all_content_sort_name_desc)
        }
    }

    DVSortType.CUSTOM -> ""
}
//endregion

//region PREVIEW
@DefaultPreviews
@Composable
fun AllContentMenuPreview() {
    AllContentMenu(
        uiMenuState = UiMenuState.Visible(
            mode = listOf(
                AllContentMenuMode.AllContent(isSelected = true),
                AllContentMenuMode.Unlinked(isSelected = false)
            ),
            sorts = listOf(
                MenuSortsItem.Sort(
                    sort = AllContentSort.ByName(isSelected = true)
                ),
                MenuSortsItem.Sort(
                    AllContentSort.ByDateUpdated(isSelected = false)
                ),
                MenuSortsItem.Sort(
                    AllContentSort.ByDateCreated(isSelected = false)
                )
            ),
            types = listOf(
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
            ),
            container = MenuSortsItem.Container(AllContentSort.ByName())
        ),
        onModeClick = {},
        onSortClick = {},
        onBinClick = {}
    )
}
//endregion