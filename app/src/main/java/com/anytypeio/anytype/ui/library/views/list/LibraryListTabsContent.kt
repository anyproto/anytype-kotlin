package com.anytypeio.anytype.ui.library.views.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ObjectTypeIds.OBJECT_TYPE as MY_TYPE
import com.anytypeio.anytype.core_models.ObjectTypeIds.RELATION as MY_RELATION
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds.OBJECT_TYPE as LIB_TYPE
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds.RELATION as LIB_RELATION
import com.anytypeio.anytype.presentation.library.LibraryEvent
import com.anytypeio.anytype.presentation.library.LibraryScreenState
import com.anytypeio.anytype.ui.library.LibraryListConfig
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
                items(data.items.size) { index ->
                    val item = data.items[index]
                    when (item.type) {
                        LIB_TYPE -> {
                            LibTypeItem(modifier = itemModifier, item = item)
                        }
                        MY_TYPE -> {
                            MyTypeItem(modifier = itemModifier, item = item)
                        }
                        LIB_RELATION -> {
                            LibRelationItem(modifier = itemModifier, item = item)
                        }
                        MY_RELATION -> {
                            MyRelationItem(modifier = itemModifier, item = item)
                        }
                        else -> {
                            Timber.d("Unknown item type: ${item.type}")
                        }
                    }
                    if (index < data.items.size.minus(1)) {
                        Divider(thickness = 1.dp, modifier = Modifier.padding(start = 4.dp, end = 4.dp))
                    }
                }
            }
        }
    }
}