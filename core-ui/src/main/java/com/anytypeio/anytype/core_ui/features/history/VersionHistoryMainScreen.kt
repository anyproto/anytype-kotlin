package com.anytypeio.anytype.core_ui.features.history

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.primitives.TimestampInSeconds
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Header
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.history.ListState
import com.anytypeio.anytype.presentation.history.VersionHistoryGroup
import com.anytypeio.anytype.presentation.history.VersionHistoryState
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun VersionHistoryScreen(
    state: State<VersionHistoryState>,
    listState: State<ListState>,
    latestVisibleVersionId: State<String>,
    onLastItemScrolled: (String) -> Unit,
    onGroupItemClicked: (VersionHistoryGroup.Item) -> Unit,
) {

    val lazyListState = rememberLazyListState()

    val shouldStartPaging = remember {
        derivedStateOf {
            val lastItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastItem?.key == latestVisibleVersionId.value
        }
    }

    LaunchedEffect(key1 = shouldStartPaging) {
        snapshotFlow { shouldStartPaging.value }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                if (listState.value == ListState.IDLE) {
                    onLastItemScrolled(latestVisibleVersionId.value)
                }
            }
    }

    VersionHistoryMainScreen(
        state = state.value,
        lazyListState = lazyListState,
        onItemClick = onGroupItemClicked
    )
}

@Composable
private fun VersionHistoryMainScreen(
    state: VersionHistoryState,
    lazyListState: LazyListState,
    onItemClick: (VersionHistoryGroup.Item) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
        Header(text = stringResource(id = R.string.version_history_title))
        when (state) {
            is VersionHistoryState.Error.GetVersions -> VersionHistoryError(
                error = stringResource(
                    id = R.string.version_history_error_get_version
                )
            )

            VersionHistoryState.Error.NoVersions -> VersionHistoryError(
                error = stringResource(id = R.string.version_history_error_no_versions)
            )

            is VersionHistoryState.Error.SpaceMembers -> VersionHistoryError(
                error = stringResource(id = R.string.version_history_error_get_members)
            )

            VersionHistoryState.Loading -> VersionHistoryLoading()
            is VersionHistoryState.Success -> {
                VersionHistorySuccessState(
                    state = state,
                    onItemClick = onItemClick,
                    lazyListState = lazyListState,
                )
            }
        }
    }
}

@Composable
private fun VersionHistoryLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(24.dp),
            color = colorResource(R.color.shape_secondary),
            trackColor = colorResource(R.color.shape_primary)
        )
    }
}

@Composable
private fun VersionHistoryError(error: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            text = error,
            style = Caption1Medium,
            color = colorResource(id = R.color.palette_dark_red),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun VersionHistorySuccessState(
    state: VersionHistoryState.Success,
    onItemClick: (VersionHistoryGroup.Item) -> Unit,
    lazyListState: LazyListState
) {

    var expandedGroupId by remember {
        mutableStateOf(state.groups.firstOrNull { it.isExpanded }?.id)
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 0.dp,
            end = 20.dp,
            bottom = 56.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            count = state.groups.size,
            key = { idx -> state.groups[idx].id }
        ) { idx ->
            val group = state.groups[idx]
            val isExpanded = group.id == expandedGroupId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors().copy(
                    containerColor = colorResource(id = R.color.background_secondary),
                ),
                border = BorderStroke(
                    width = 0.5.dp, color = colorResource(id = R.color.shape_primary)
                )
            ) {
                Spacer(modifier = Modifier.height(if (isExpanded) 12.dp else 14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 16.dp)
                        .clickable {
                            expandedGroupId = if (expandedGroupId == group.id) null else group.id
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = setGroupTitle(groupTitle = group.title),
                        style = PreviewTitle2Regular,
                        color = colorResource(id = R.color.text_secondary)
                    )
                    if (!isExpanded) {
                        HeaderIcons(icons = group.icons)
                    }
                }
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(4.dp))
                    group.items.forEach {
                        GroupItem(item = it, onItemClick = onItemClick)
                    }
                }
                Spacer(modifier = Modifier.height(if (isExpanded) 12.dp else 14.dp))
            }
        }
    }
}

@Composable
private fun setGroupTitle(groupTitle: VersionHistoryGroup.GroupTitle): String {
    return when (groupTitle) {
        is VersionHistoryGroup.GroupTitle.Date -> groupTitle.date
        VersionHistoryGroup.GroupTitle.Today -> stringResource(id = R.string.today)
        VersionHistoryGroup.GroupTitle.Yesterday -> stringResource(id = R.string.yesterday)
    }
}

@Composable
private fun GroupItem(
    item: VersionHistoryGroup.Item,
    onItemClick: (VersionHistoryGroup.Item) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp)
            .clickable { onItemClick(item) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = item.timeFormatted,
                style = PreviewTitle2Medium,
                color = colorResource(id = R.color.text_primary)
            )
            Text(
                text = item.spaceMemberName,
                style = Caption1Regular,
                color = colorResource(id = R.color.text_secondary)
            )
        }
        if (item.icon != null) {
            ListWidgetObjectIcon(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically),
                icon = item.icon!!,
                iconSize = 24.dp
            )
        }
    }
}

@Composable
private fun RowScope.HeaderIcons(
    icons: List<ObjectIcon>
) {
    LazyRow(
        modifier = Modifier
            .weight(1f)
            .padding(start = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(
            space = (-8).dp,
            alignment = Alignment.End
        ),
        reverseLayout = true
    ) {
        items(
            count = icons.size,
            key = { idx -> icons[idx].toString() }
        ) { idx ->
            when (idx) {
                in (0 until MEMBERS_ICONS_MAX_SIZE) -> {
                    val icon = icons[idx]
                    Box(
                        modifier = Modifier
                            .size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ListWidgetObjectIcon(
                            modifier = Modifier.size(24.dp),
                            icon = icon,
                            iconSize = 24.dp
                        )
                    }
                }
            }
        }
    }
    if (icons.size - MEMBERS_ICONS_MAX_SIZE > 0) {
        Text(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 4.dp),
            text = "+${icons.size - MEMBERS_ICONS_MAX_SIZE}",
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode"
)
@Preview(
    showBackground = true,
    backgroundColor = 0xFF1F1F1F,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
private fun SpaceListScreenPreview() {
    VersionHistoryMainScreen(
        state = VersionHistoryState.Success(
            groups = listOf(
                VersionHistoryGroup(
                    id = "1",
                    title = VersionHistoryGroup.GroupTitle.Today,
                    isExpanded = true,
                    items = listOf(
                        VersionHistoryGroup.Item(
                            id = "1",
                            timeFormatted = "12:00 PM",
                            spaceMemberName = "John Doe",
                            icon = ObjectIcon.Profile.Avatar("A"),
                            spaceMember = "1",
                            timeStamp = TimestampInSeconds(23423423L),
                            versions = emptyList(),
                            dateFormatted = "Today",

                            ),
                        VersionHistoryGroup.Item(
                            id = "2",
                            timeFormatted = "12:10 PM",
                            spaceMemberName = "Alice Doe",
                            icon = ObjectIcon.Profile.Avatar("B"),
                            spaceMember = "1",
                            timeStamp = TimestampInSeconds(23423423L),
                            versions = emptyList(),
                            dateFormatted = "Today",
                        ),
                        VersionHistoryGroup.Item(
                            id = "3",
                            timeFormatted = "12:20 PM",
                            spaceMemberName = "Bob Doe",
                            icon = ObjectIcon.Profile.Avatar("C"),
                            spaceMember = "1",
                            timeStamp = TimestampInSeconds(23423423L),
                            versions = emptyList(),
                            dateFormatted = "Today",
                        ),
                    ),
                    icons = listOf(ObjectIcon.Profile.Avatar("A"), ObjectIcon.Profile.Avatar("B"))
                ),
                VersionHistoryGroup(
                    id = "2",
                    title = VersionHistoryGroup.GroupTitle.Yesterday,
                    items = emptyList(),
                    icons = listOf(
                        ObjectIcon.Profile.Avatar("C"),
                        ObjectIcon.Profile.Avatar("D"),
                        ObjectIcon.Profile.Avatar("E"),
                        ObjectIcon.Profile.Avatar("F"),
                        ObjectIcon.Profile.Avatar("G"),
                        ObjectIcon.Profile.Avatar("H"),
                        ObjectIcon.Profile.Avatar("I"),
                        ObjectIcon.Profile.Avatar("J"),
                        ObjectIcon.Profile.Avatar("K"),
                        ObjectIcon.Profile.Avatar("L"),
                        ObjectIcon.Profile.Avatar("M"),
                        ObjectIcon.Profile.Avatar("N"),
                        ObjectIcon.Profile.Avatar("O"),
                        ObjectIcon.Profile.Avatar("P"),
                        ObjectIcon.Profile.Avatar("Q"),
                        ObjectIcon.Profile.Avatar("R"),
                        ObjectIcon.Profile.Avatar("S"),
                        ObjectIcon.Profile.Avatar("T"),
                        ObjectIcon.Profile.Avatar("U"),
                        ObjectIcon.Profile.Avatar("V"),
                        ObjectIcon.Profile.Avatar("W"),
                        ObjectIcon.Profile.Avatar("X"),
                        ObjectIcon.Profile.Avatar("Y")
                    )
                ),
                VersionHistoryGroup(
                    id = "3",
                    title = VersionHistoryGroup.GroupTitle.Date("2021-10-10"),
                    items = emptyList(),
                    icons = listOf(ObjectIcon.Profile.Avatar("C"), ObjectIcon.Profile.Avatar("D"))
                )
            )
        ),
        onItemClick = {},
        lazyListState = rememberLazyListState()
    )
}

const val MEMBERS_ICONS_MAX_SIZE = 3

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode"
)
@Preview(
    showBackground = true,
    backgroundColor = 0x000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
private fun SpaceListScreenPreviewLoading() {
    VersionHistoryMainScreen(
        state = VersionHistoryState.Error.NoVersions,
        onItemClick = {},
        lazyListState = rememberLazyListState()
    )
}