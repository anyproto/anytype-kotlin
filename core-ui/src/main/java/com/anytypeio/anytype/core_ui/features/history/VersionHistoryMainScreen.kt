package com.anytypeio.anytype.core_ui.features.history

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_models.primitives.TimeInSeconds
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Header
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.history.VersionHistoryGroup
import com.anytypeio.anytype.presentation.history.VersionHistoryState
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@Composable
fun VersionHistoryScreen(
    state: VersionHistoryState
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
            is VersionHistoryState.Error.GetVersions -> TODO()
            VersionHistoryState.Error.NoVersions -> TODO()
            is VersionHistoryState.Error.SpaceMembers -> TODO()
            VersionHistoryState.Loading -> TODO()
            is VersionHistoryState.Success -> {
                VersionHistorySuccessState(state = state)
            }
        }

    }
}

@Composable
private fun VersionHistorySuccessState(state: VersionHistoryState.Success) {

    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth(),
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
            if (group.isExpanded) {
                GroupItemExpanded(group = group)
            } else {
                GroupItemCollapsed(group = group)
            }
        }

    }
}

@Composable
private fun GroupItemExpanded(group: VersionHistoryGroup) {
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
        ),
    ) {
        Text(
            modifier = Modifier
                .padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                .wrapContentSize(),
            text = group.title,
            style = PreviewTitle2Regular,
            color = colorResource(id = R.color.text_secondary)
        )
        group.items.forEach {
            GroupItem(item = it)
        }
    }
}

@Composable
private fun GroupItem(item: VersionHistoryGroup.Item) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(start = 16.dp),
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
                iconSize = 24.dp,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun GroupItemCollapsed(group: VersionHistoryGroup) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors().copy(
            containerColor = colorResource(id = R.color.background_secondary),
        ),
        border = BorderStroke(
            width = 0.5.dp, color = colorResource(id = R.color.shape_primary)
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.wrapContentSize(),
                text = group.title,
                style = PreviewTitle2Regular,
                color = colorResource(id = R.color.text_secondary)
            )
            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(
                    space = (-4).dp,
                    alignment = Alignment.End
                )
            ) {
                items(
                    count = group.icons.size,
                ) { idx ->
                    val icon = group.icons[idx]
                    ListWidgetObjectIcon(
                        modifier = Modifier.size(24.dp),
                        icon = icon,
                        iconSize = 24.dp,
                        fontSize = 16.sp
                    )
                }
            }
        }
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
    backgroundColor = 0x000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
private fun SpaceListScreenPreview() {
    VersionHistoryScreen(
        state = VersionHistoryState.Success(
            groups = listOf(
                VersionHistoryGroup(
                    id = "1",
                    title = "Today",
                    isExpanded = true,
                    items = listOf(
                        VersionHistoryGroup.Item(
                            id = "1",
                            timeFormatted = "12:00 PM",
                            spaceMemberName = "John Doe",
                            icon = ObjectIcon.Profile.Avatar("A"),
                            spaceMember = "1",
                            timeStamp = TimeInSeconds(23423423L),
                            versions = emptyList()

                        ),
                        VersionHistoryGroup.Item(
                            id = "2",
                            timeFormatted = "12:10 PM",
                            spaceMemberName = "Alice Doe",
                            icon = ObjectIcon.Profile.Avatar("B"),
                            spaceMember = "1",
                            timeStamp = TimeInSeconds(23423423L),
                            versions = emptyList()
                        ),
                        VersionHistoryGroup.Item(
                            id = "3",
                            timeFormatted = "12:20 PM",
                            spaceMemberName = "Bob Doe",
                            icon = ObjectIcon.Profile.Avatar("C"),
                            spaceMember = "1",
                            timeStamp = TimeInSeconds(23423423L),
                            versions = emptyList()
                        ),
                    ),
                    icons = listOf(ObjectIcon.Profile.Avatar("A"), ObjectIcon.Profile.Avatar("B"))
                ),
                VersionHistoryGroup(
                    id = "2",
                    title = "Yesterday",
                    items = emptyList(),
                    icons = listOf(ObjectIcon.Profile.Avatar("C"), ObjectIcon.Profile.Avatar("D"))
                ),
                VersionHistoryGroup(
                    id = "3",
                    title = "Yesterday",
                    items = emptyList(),
                    icons = listOf(ObjectIcon.Profile.Avatar("C"), ObjectIcon.Profile.Avatar("D"))
                )
            )

        )
    )
}