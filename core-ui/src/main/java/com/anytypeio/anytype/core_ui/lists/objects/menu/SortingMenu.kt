package com.anytypeio.anytype.core_ui.lists.objects.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.presentation.objects.MenuSortsItem
import com.anytypeio.anytype.presentation.objects.ObjectsListSort

@Composable
fun ObjectsListSortingMenuContainer(
    container: MenuSortsItem.Container,
    sorts: List<MenuSortsItem.Sort>,
    types: List<MenuSortsItem.SortType>,
    sortingExpanded: Boolean,
    onChangeSortExpandedState: (Boolean) -> Unit,
    onSortClick: (ObjectsListSort) -> Unit
) {

    SortingBox(
        modifier = Modifier
            .clickable {
                onChangeSortExpandedState(!sortingExpanded)
            },
        subtitle = container.sort.title(),
        isExpanded = sortingExpanded
    )
    Divider(
        paddingStart = 0.dp,
        paddingEnd = 0.dp,
        color = colorResource(R.color.shape_secondary)
    )
    if (sortingExpanded) {
        sorts.forEach { item ->
            ObjectsListMenuItem(
                title = item.sort.title(),
                isSelected = item.sort.isSelected,
                modifier = Modifier
                    .clickable {
                        onSortClick(item.sort)
                    }
            )
            Divider(
                paddingStart = 0.dp,
                paddingEnd = 0.dp,
                color = colorResource(R.color.shape_secondary)
            )
        }
        Divider(
            height = 7.5.dp,
            paddingStart = 0.dp,
            paddingEnd = 0.dp,
            color = colorResource(R.color.shape_secondary)
        )
        val size = types.size
        types.forEachIndexed { index, item ->
            val s = item.sort
            ObjectsListMenuItem(
                title = item.sortType.title(item.sort),
                isSelected = item.isSelected,
                modifier = Modifier
                    .clickable {
                        val updatedSort = when (val sort = item.sort) {
                            is ObjectsListSort.ByName -> sort.copy(sortType = item.sortType)
                            is ObjectsListSort.ByDateCreated -> sort.copy(sortType = item.sortType)
                            is ObjectsListSort.ByDateUpdated -> sort.copy(sortType = item.sortType)
                            is ObjectsListSort.ByDateUsed -> sort.copy(sortType = item.sortType)
                        }
                        onSortClick(updatedSort)
                    }
            )
            if (index < size - 1) {
                Divider(
                    paddingStart = 0.dp,
                    paddingEnd = 0.dp,
                    color = colorResource(R.color.shape_secondary)
                )
            }
        }
    }
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
fun ObjectsListMenuItem(
    modifier: Modifier,
    title: String,
    isSelected: Boolean,
    contentDescription: String? = null
) {
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
            contentDescription = contentDescription,
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
fun ObjectsListSort.title(): String = stringResource(
    when (this) {
        is ObjectsListSort.ByDateCreated -> R.string.all_content_sort_date_created
        is ObjectsListSort.ByDateUpdated -> R.string.all_content_sort_date_updated
        is ObjectsListSort.ByName -> R.string.all_content_sort_name
        is ObjectsListSort.ByDateUsed -> R.string.all_content_sort_date_used
    }
)

@Composable
fun DVSortType.title(sort: ObjectsListSort): String = when (this) {
    DVSortType.ASC -> {
        when (sort) {
            is ObjectsListSort.ByDateCreated, is ObjectsListSort.ByDateUpdated, is ObjectsListSort.ByDateUsed -> stringResource(
                id = R.string.all_content_sort_date_asc
            )

            is ObjectsListSort.ByName -> stringResource(id = R.string.all_content_sort_name_asc)
        }
    }

    DVSortType.DESC -> {
        when (sort) {
            is ObjectsListSort.ByDateCreated,
            is ObjectsListSort.ByDateUpdated,
            is ObjectsListSort.ByDateUsed -> stringResource(
                id = R.string.all_content_sort_date_desc
            )

            is ObjectsListSort.ByName -> stringResource(id = R.string.all_content_sort_name_desc)
        }
    }

    DVSortType.CUSTOM -> ""
}