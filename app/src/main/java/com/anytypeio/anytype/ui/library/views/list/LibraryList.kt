package com.anytypeio.anytype.ui.library.views.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.anytypeio.anytype.R
import com.anytypeio.anytype.ui.library.LibraryListConfig
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState

@ExperimentalPagerApi
@Composable
fun LibraryListView(libraryListConfig: List<LibraryListConfig>) {
    val pagerState = rememberPagerState(INITIAL_TAB)
    val modifier = Modifier.background(
        color = colorResource(id = R.color.background_primary)
    )
    Column {
        LibraryListTabs(pagerState, libraryListConfig, modifier)
        LibraryListTabsContent(modifier, pagerState, libraryListConfig)
    }
}

private const val INITIAL_TAB = 0