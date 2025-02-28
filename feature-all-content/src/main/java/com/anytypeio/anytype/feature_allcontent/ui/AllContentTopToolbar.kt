package com.anytypeio.anytype.feature_allcontent.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.bouncingClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.feature_allcontent.R
import com.anytypeio.anytype.feature_allcontent.models.AllContentMenuMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.UiMenuState
import com.anytypeio.anytype.feature_allcontent.models.UiTabsState
import com.anytypeio.anytype.feature_allcontent.models.UiTitleState
import com.anytypeio.anytype.presentation.objects.MenuSortsItem
import com.anytypeio.anytype.presentation.objects.ObjectsListSort

//region AllContentTopBarContainer
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllContentTopBarContainer(
    titleState: UiTitleState,
    uiMenuState: UiMenuState,
    onModeClick: (AllContentMenuMode) -> Unit,
    onSortClick: (ObjectsListSort) -> Unit,
    onBinClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        modifier = Modifier.fillMaxWidth(),
        expandedHeight = 48.dp,
        title = { AllContentTitle(state = titleState) },
        navigationIcon = {
            Image(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .noRippleClickable {
                        onBackClick()
                    }
                ,
                painter = painterResource(R.drawable.ic_default_top_back),
                contentDescription = stringResource(R.string.content_desc_back_button)
            )
        },
        actions = {
            AllContentMenuButton(
                onClick = { isMenuExpanded = true }
            )
            if (uiMenuState is UiMenuState.Visible) {
                DropdownMenu(
                    modifier = Modifier.width(252.dp),
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    shape = RoundedCornerShape(size = 16.dp),
                    containerColor = colorResource(id = R.color.background_primary),
                    shadowElevation = 5.dp
                ) {
                    AllContentMenu(
                        uiMenuState = uiMenuState,
                        onModeClick = onModeClick,
                        onSortClick = onSortClick,
                        onBinClick = onBinClick
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = colorResource(id = R.color.background_primary)
        ),
    )
}

@DefaultPreviews
@Composable
private fun AllContentTopBarContainerPreview() {
    AllContentTopBarContainer(
        titleState = UiTitleState.OnlyUnlinked,
        uiMenuState = UiMenuState.Visible(
            mode = listOf(
                AllContentMenuMode.AllContent(isSelected = true),
                AllContentMenuMode.Unlinked()
            ),
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
            )
        ),
        onModeClick = {},
        onSortClick = {},
        onBinClick = {},
        onBackClick = {}
    )
}
//endregion

//region AllContentTitle
@Composable
fun AllContentTitle(state: UiTitleState) {
    when (state) {
        UiTitleState.AllContent -> {
            Text(
                modifier = Modifier
                    .wrapContentSize(),
                text = stringResource(id = R.string.all_content_title_all_content),
                style = Title1,
                color = colorResource(id = R.color.text_primary)
            )
        }

        UiTitleState.OnlyUnlinked -> {
            Text(
                modifier = Modifier
                    .wrapContentSize(),
                text = stringResource(id = R.string.all_content_title_only_unlinked),
                style = Title1,
                color = colorResource(id = R.color.text_primary)
            )
        }
    }
}
//endregion

//region AllContentMenuButton
@Composable
fun AllContentMenuButton(onClick: () -> Unit) {
    Image(
        modifier = Modifier
            .padding(end = 12.dp)
            .size(32.dp)
            .bouncingClickable { onClick() },
        painter = painterResource(id = R.drawable.ic_space_list_dots),
        contentDescription = "Menu icon",
        contentScale = ContentScale.Inside
    )
}
//endregion

//region AllContentTabs
@Composable
fun AllContentTabs(
    tabsViewState: UiTabsState,
    onClick: (AllContentTab) -> Unit
) {
    val scrollState = rememberLazyListState()
    var selectedTab by remember { mutableStateOf(tabsViewState.selectedTab) }

    val snapFlingBehavior = rememberSnapFlingBehavior(scrollState)

    LazyRow(
        state = scrollState,
        //todo Disabled because, after the scroll animation, the tabs sometimes don’t respond to clicks
        //flingBehavior = snapFlingBehavior,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp)
    ) {
        items(
            count = tabsViewState.tabs.size,
            key = { index -> tabsViewState.tabs[index].ordinal },
        ) { index ->
            val tab = tabsViewState.tabs[index]
            AllContentTabText(
                tab = tab,
                isSelected = tab == selectedTab,
                onClick = {
                    selectedTab = tab
                    onClick(tab)
                }
            )
        }
    }
}

@Composable
private fun AllContentTabText(
    tab: AllContentTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .height(40.dp)
            .noRippleThrottledClickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp),
            text = getTabText(tab),
            style = Title2,
            color = if (isSelected) colorResource(id = R.color.glyph_button) else colorResource(id = R.color.glyph_active),
            maxLines = 1
        )
    }
}

@Composable
private fun getTabText(tab: AllContentTab): String {
    return when (tab) {
        AllContentTab.PAGES -> stringResource(id = R.string.all_content_title_tab_pages)
        AllContentTab.FILES -> stringResource(id = R.string.all_content_title_tab_files)
        AllContentTab.MEDIA -> stringResource(id = R.string.all_content_title_tab_media)
        AllContentTab.BOOKMARKS -> stringResource(id = R.string.all_content_title_tab_bookmarks)
        AllContentTab.TYPES -> stringResource(id = R.string.all_content_title_tab_objetc_types)
        AllContentTab.LISTS -> stringResource(id = R.string.all_content_title_tab_lists)
        AllContentTab.RELATIONS -> stringResource(id = R.string.all_content_title_tab_relations)
    }
}

@DefaultPreviews
@Composable
private fun AllContentTabsPreview() {
    AllContentTabs(
        tabsViewState = UiTabsState(
            tabs = listOf(
                AllContentTab.PAGES,
                AllContentTab.FILES,
                AllContentTab.MEDIA,
                AllContentTab.BOOKMARKS,
                AllContentTab.TYPES,
                AllContentTab.RELATIONS
            ),
            selectedTab = AllContentTab.MEDIA
        ),
        onClick = {}
    )
}

//endregion