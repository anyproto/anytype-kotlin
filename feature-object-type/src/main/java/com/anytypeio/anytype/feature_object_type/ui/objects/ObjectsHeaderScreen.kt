package com.anytypeio.anytype.feature_object_type.ui.objects

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.lists.objects.menu.ObjectsListMenuItem
import com.anytypeio.anytype.core_ui.lists.objects.menu.ObjectsListSortingMenuContainer
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.UiMenuSetItem
import com.anytypeio.anytype.feature_object_type.ui.UiMenuState
import com.anytypeio.anytype.feature_object_type.ui.UiObjectsAddIconState
import com.anytypeio.anytype.feature_object_type.ui.UiObjectsHeaderState
import com.anytypeio.anytype.feature_object_type.ui.UiObjectsSettingsIconState
import com.anytypeio.anytype.presentation.objects.MenuSortsItem
import com.anytypeio.anytype.presentation.objects.ObjectsListSort
import timber.log.Timber

@Composable
fun ObjectsHeader(
    modifier: Modifier,
    uiObjectsHeaderState: UiObjectsHeaderState,
    uiObjectsAddIconState: UiObjectsAddIconState,
    uiObjectsSettingsIconState: UiObjectsSettingsIconState,
    uiObjectsMenuState: UiMenuState,
    onTypeEvent: (TypeEvent) -> Unit
) {

    var isMenuExpanded by remember { mutableStateOf(false) }
    var isSortingExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier.matchParentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically),
                text = stringResource(R.string.objects),
                style = BodyBold,
                color = colorResource(R.color.text_primary)
            )
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(start = 8.dp),
                text = uiObjectsHeaderState.count,
                style = PreviewTitle1Regular,
                color = colorResource(R.color.text_secondary)
            )
        }
        Row(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            if (uiObjectsSettingsIconState is UiObjectsSettingsIconState.Visible) {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .width(40.dp)
                        .noRippleThrottledClickable {
                            isMenuExpanded = !isMenuExpanded
                        },
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Image(
                        modifier = Modifier.wrapContentSize(),
                        painter = painterResource(R.drawable.ic_space_list_dots),
                        contentDescription = "Objects settings icon"
                    )
                }
                DropdownMenu(
                    modifier = Modifier.width(252.dp),
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    shape = RoundedCornerShape(size = 16.dp),
                    containerColor = colorResource(id = R.color.background_primary),
                    shadowElevation = 5.dp,
                    border = BorderStroke(
                        width = 0.5.dp,
                        color = colorResource(id = R.color.background_secondary)
                    )
                ) {
                    when (val item = uiObjectsMenuState.objSetItem) {
                        UiMenuSetItem.CreateSet -> {
                            ObjectsListMenuItem(
                                title = stringResource(R.string.object_type_objects_menu_create_set),
                                isSelected = false,
                                modifier = Modifier
                            )
                            Divider(
                                height = 8.dp,
                                paddingStart = 0.dp,
                                paddingEnd = 0.dp,
                                color = colorResource(R.color.shape_secondary)
                            )
                        }

                        is UiMenuSetItem.OpenSet -> {
                            ObjectsListMenuItem(
                                title = stringResource(R.string.object_type_objects_menu_open_set),
                                isSelected = false,
                                modifier = Modifier
                            )
                            Divider(
                                height = 8.dp,
                                paddingStart = 0.dp,
                                paddingEnd = 0.dp,
                                color = colorResource(R.color.shape_secondary)
                            )
                        }

                        UiMenuSetItem.Hidden -> {}
                    }
                    ObjectsListSortingMenuContainer(
                        container = uiObjectsMenuState.container,
                        sorts = uiObjectsMenuState.sorts,
                        types = uiObjectsMenuState.types,
                        sortingExpanded = isSortingExpanded,
                        onSortClick = {
                        },
                        onChangeSortExpandedState = { isSortingExpanded = it }
                    )
                }
            }
            if (uiObjectsAddIconState is UiObjectsAddIconState.Visible) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(48.dp)
                        .width(32.dp)
                        .noRippleThrottledClickable {
                        },
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Image(
                        modifier = Modifier.wrapContentSize(),
                        painter = painterResource(R.drawable.ic_default_plus),
                        contentDescription = "Add",
                    )
                }
            }
        }
    }
}

@DefaultPreviews
@Composable
fun ObjectsHeaderPreview() {
    ObjectsHeader(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        uiObjectsHeaderState = UiObjectsHeaderState("3"),
        uiObjectsAddIconState = UiObjectsAddIconState.Visible,
        uiObjectsSettingsIconState = UiObjectsSettingsIconState.Visible,
        uiObjectsMenuState = UiMenuState(
            container = MenuSortsItem.Container(
                sort = ObjectsListSort.ByName(isSelected = true)
            ),
            sorts = listOf(
                MenuSortsItem.Sort(
                    sort = ObjectsListSort.ByName(isSelected = true)
                ),
            ),
            types = listOf(
                MenuSortsItem.SortType(
                    sort = ObjectsListSort.ByName(isSelected = true),
                    sortType = DVSortType.DESC,
                    isSelected = true
                ),
                MenuSortsItem.SortType(
                    sort = ObjectsListSort.ByDateCreated(isSelected = false),
                    sortType = DVSortType.ASC,
                    isSelected = false
                ),
            ),
            objSetItem = UiMenuSetItem.CreateSet
        ),
        onTypeEvent = {}
    )
}