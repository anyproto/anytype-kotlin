package com.anytypeio.anytype.feature_allcontent.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncError
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncNetwork
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.syncstatus.StatusBadge
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.feature_allcontent.R
import com.anytypeio.anytype.feature_allcontent.models.AllContentMenuMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.UiMenuState
import com.anytypeio.anytype.feature_allcontent.models.UiSyncStatusBadgeState
import com.anytypeio.anytype.feature_allcontent.models.UiTabsState
import com.anytypeio.anytype.feature_allcontent.models.UiTitleState
import com.anytypeio.anytype.presentation.objects.MenuSortsItem
import com.anytypeio.anytype.presentation.objects.ObjectsListSort

//region AllContentTopBarContainer
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllContentTopBarContainer(
    titleState: UiTitleState,
    uiSyncStatusBadgeState: UiSyncStatusBadgeState,
    uiMenuState: UiMenuState,
    onBackClick: () -> Unit,
    onTitleClick: () -> Unit,
    onSyncStatusClick: (SpaceSyncAndP2PStatusState) -> Unit,
    onModeClick: (AllContentMenuMode) -> Unit,
    onSortClick: (ObjectsListSort) -> Unit,
    onBinClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        // Back pill — circular button at start.
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(44.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    clip = false
                )
                .background(
                    color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.navigation_panel),
                    shape = CircleShape
                )
                .noRippleThrottledClickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.wrapContentSize(),
                painter = painterResource(R.drawable.ic_default_top_back),
                contentDescription = stringResource(R.string.content_desc_back_button)
            )
        }

        // Center title pill — tapping opens the widgets overlay.
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(start = 64.dp, end = 120.dp)
                .fillMaxHeight()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(22.dp),
                    clip = false
                )
                .background(
                    color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.navigation_panel),
                    shape = RoundedCornerShape(22.dp)
                )
                .noRippleThrottledClickable { onTitleClick() }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = when (titleState) {
                    UiTitleState.AllContent -> stringResource(id = R.string.all_content_title_all_content)
                    UiTitleState.OnlyUnlinked -> stringResource(id = R.string.all_content_title_only_unlinked)
                },
                style = PreviewTitle2Regular,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Trailing — optional sync badge pill + menu pill.
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (uiSyncStatusBadgeState is UiSyncStatusBadgeState.Visible) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = CircleShape,
                            clip = false
                        )
                        .background(
                            color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.navigation_panel),
                            shape = CircleShape
                        )
                        .noRippleThrottledClickable {
                            onSyncStatusClick(uiSyncStatusBadgeState.status)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    StatusBadge(
                        status = uiSyncStatusBadgeState.status,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            AllContentMenuButton(
                uiMenuState = uiMenuState,
                onModeClick = onModeClick,
                onSortClick = onSortClick,
                onBinClick = onBinClick
            )
        }
    }
}

@DefaultPreviews
@Composable
private fun AllContentTopBarContainerPreview() {
    AllContentTopBarContainer(
        titleState = UiTitleState.AllContent,
        uiSyncStatusBadgeState = UiSyncStatusBadgeState.Visible(
            status = SpaceSyncAndP2PStatusState.Success(
                spaceSyncUpdate = SpaceSyncUpdate.Update(
                    id = "1",
                    status = SpaceSyncStatus.SYNCING,
                    network = SpaceSyncNetwork.ANYTYPE,
                    error = SpaceSyncError.NULL,
                    syncingObjectsCounter = 2
                ),
                p2PStatusUpdate = P2PStatusUpdate.Initial
            )
        ),
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
        onBackClick = {},
        onTitleClick = {},
        onSyncStatusClick = {},
        onModeClick = {},
        onSortClick = {},
        onBinClick = {}
    )
}

@DefaultPreviews
@Composable
private fun AllContentTopBarContainerHiddenSyncPreview() {
    AllContentTopBarContainer(
        titleState = UiTitleState.OnlyUnlinked,
        uiSyncStatusBadgeState = UiSyncStatusBadgeState.Hidden,
        uiMenuState = UiMenuState.Hidden,
        onBackClick = {},
        onTitleClick = {},
        onSyncStatusClick = {},
        onModeClick = {},
        onSortClick = {},
        onBinClick = {}
    )
}
//endregion

//region AllContentMenuButton
@Composable
fun AllContentMenuButton(
    uiMenuState: UiMenuState,
    onModeClick: (AllContentMenuMode) -> Unit,
    onSortClick: (ObjectsListSort) -> Unit,
    onBinClick: () -> Unit,
) {

    var isMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(44.dp)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                clip = false
            )
            .background(
                color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.navigation_panel),
                shape = CircleShape
            )
            .noRippleThrottledClickable { isMenuExpanded = true },
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_space_list_dots),
            contentDescription = stringResource(id = R.string.more)
        )
        if (uiMenuState is UiMenuState.Visible) {
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
                AllContentMenu(
                    uiMenuState = uiMenuState,
                    onModeClick = onModeClick,
                    onSortClick = onSortClick,
                    onBinClick = onBinClick
                )
            }
        }
    }
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
        AllContentTab.LISTS -> stringResource(id = R.string.all_content_title_tab_lists)
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
                AllContentTab.BOOKMARKS
            ),
            selectedTab = AllContentTab.MEDIA
        ),
        onClick = {}
    )
}

//endregion
