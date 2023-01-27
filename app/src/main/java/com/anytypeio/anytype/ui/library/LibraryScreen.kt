package com.anytypeio.anytype.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.anytypeio.anytype.R
import com.anytypeio.anytype.ui.library.views.LibraryTabs
import com.anytypeio.anytype.ui.library.views.LibraryTabsContent
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState

@ExperimentalPagerApi
@Composable
fun LibraryScreen(configuration: List<LibraryScreenConfig>) {
    val pagerState = rememberPagerState(INITIAL_TAB)
    val modifier = Modifier.background(
        color = colorResource(id = R.color.background_primary)
    )
    Column(modifier = modifier) {
        LibraryTabs(modifier, pagerState, configuration)
        LibraryTabsContent(modifier, pagerState, configuration)
    }
}

private const val INITIAL_TAB = 0