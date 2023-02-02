package com.anytypeio.anytype.ui.library.views.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.library.LibraryEvent
import com.anytypeio.anytype.presentation.library.LibraryScreenState
import com.anytypeio.anytype.presentation.navigation.LibraryView
import com.anytypeio.anytype.ui.library.LibraryListConfig
import com.anytypeio.anytype.ui.library.views.list.items.CreateNewTypeItem
import com.anytypeio.anytype.ui.library.views.list.items.ItemDefaults
import com.anytypeio.anytype.ui.library.views.list.items.LibRelationItem
import com.anytypeio.anytype.ui.library.views.list.items.LibTypeItem
import com.anytypeio.anytype.ui.library.views.list.items.MyRelationItem
import com.anytypeio.anytype.ui.library.views.list.items.MyTypeItem
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import timber.log.Timber

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
        .padding(start = 4.dp, end = 4.dp)

    HorizontalPager(modifier = modifier, state = pagerState, count = configuration.size) { index ->
        val data = when (configuration[index]) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
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
                                    installed = item.installed,
                                    modifier = itemModifier
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
                                    installed = item.installed
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
                                    modifier = itemModifier,
                                    name = item.name
                                )
                            }
                            is LibraryView.UnknownView -> {
                                // do nothing
                            }
                        }
                        if (ix < data.items.lastIndex) {
                            Divider(
                                thickness = 1.dp,
                                modifier = Modifier.padding(start = 4.dp, end = 4.dp),
                                color = colorResource(id = R.color.shape_primary)
                            )
                        }
                    }
                )
            }
        }
    }
}