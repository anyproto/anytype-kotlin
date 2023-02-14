package com.anytypeio.anytype.ui.library.views.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.library.DependentData
import com.anytypeio.anytype.presentation.library.LibraryEvent
import com.anytypeio.anytype.presentation.library.LibraryScreenState
import com.anytypeio.anytype.presentation.library.LibraryView
import com.anytypeio.anytype.ui.library.LibraryListConfig
import com.anytypeio.anytype.ui.library.views.list.items.CreateNewTypeItem
import com.anytypeio.anytype.ui.library.views.list.items.ItemDefaults
import com.anytypeio.anytype.ui.library.views.list.items.LibRelationItem
import com.anytypeio.anytype.ui.library.views.list.items.LibTypeItem
import com.anytypeio.anytype.ui.library.views.list.items.LibraryTypesEmptyItem
import com.anytypeio.anytype.ui.library.views.list.items.MyRelationItem
import com.anytypeio.anytype.ui.library.views.list.items.MyTypeItem
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState

@ExperimentalPagerApi
@Composable
fun LibraryListTabsContent(
    modifier: Modifier,
    pagerState: PagerState,
    configuration: List<LibraryListConfig>,
    tabs: LibraryScreenState.Tabs,
    vmEventStream: (LibraryEvent) -> Unit,
) {

    val itemModifier = Modifier
        .fillMaxWidth()
        .height(ItemDefaults.ITEM_HEIGHT)
        .padding(start = LibraryListDefaults.ItemPadding, end = LibraryListDefaults.ItemPadding)

    HorizontalPager(modifier = modifier, state = pagerState, count = configuration.size) { index ->
        val config = configuration[index]
        val data = when (config) {
            is LibraryListConfig.Types, is LibraryListConfig.Relations -> tabs.my
            is LibraryListConfig.TypesLibrary, is LibraryListConfig.RelationsLibrary -> tabs.lib
        }
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            LibraryListSearchWidget(vmEventStream = vmEventStream, config = configuration[index])
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = LibraryListDefaults.ListPadding,
                    end = LibraryListDefaults.ListPadding
                )
            ) {

                items(
                    count = data.items.size,
                    key = { index ->
                        data.items[index].id
                    },
                    itemContent = { ix ->
                        when (val item = data.items[ix]) {
                            is LibraryView.LibraryTypeView -> {
                                LibTypeItem(
                                    name = item.name,
                                    icon = item.icon,
                                    installed = item.dependentData is DependentData.Model,
                                    modifier = itemModifier,
                                    onClick = {
                                        vmEventStream.invoke(
                                            LibraryEvent.ToggleInstall.Type(item)
                                        )
                                    }
                                )
                            }
                            is LibraryView.MyTypeView -> {
                                MyTypeItem(
                                    name = item.name,
                                    icon = item.icon,
                                    readOnly = item.readOnly,
                                    modifier = itemModifier
                                )
                            }
                            is LibraryView.LibraryRelationView -> {
                                LibRelationItem(
                                    modifier = itemModifier,
                                    name = item.name,
                                    format = item.format,
                                    installed = item.dependentData is DependentData.Model,
                                    onClick = {
                                        vmEventStream.invoke(
                                            LibraryEvent.ToggleInstall.Relation(item)
                                        )
                                    }
                                )
                            }
                            is LibraryView.MyRelationView -> {
                                MyRelationItem(
                                    modifier = itemModifier,
                                    name = item.name,
                                    readOnly = item.readOnly,
                                    format = item.format
                                )
                            }
                            is LibraryView.CreateNewTypeView -> {
                                CreateNewTypeItem(
                                    modifier = itemModifier.clickable {
                                        vmEventStream.invoke(
                                            LibraryEvent.CreateType(
                                                item.name
                                            )
                                        )
                                    },
                                    name = item.name
                                )
                            }
                            is LibraryView.LibraryTypesPlaceholderView -> {
                                LibraryTypesEmptyItem(item.name)
                            }
                            is LibraryView.UnknownView -> {
                                // do nothing
                            }
                        }
                        if (ix < data.items.lastIndex) {
                            Divider(
                                thickness = LibraryListDefaults.DividerThickness,
                                modifier = Modifier.padding(
                                    start = LibraryListDefaults.DividerPadding,
                                    end = LibraryListDefaults.DividerPadding
                                ),
                                color = colorResource(id = R.color.shape_primary)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Immutable
private object LibraryListDefaults {
    val ItemPadding = 4.dp
    val DividerPadding = 4.dp
    val ListPadding = 16.dp
    val DividerThickness = 0.5.dp
}