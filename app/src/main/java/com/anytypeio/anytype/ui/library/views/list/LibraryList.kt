package com.anytypeio.anytype.ui.library.views.list

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.library.LibraryAnalyticsEvent
import com.anytypeio.anytype.presentation.library.LibraryEvent
import com.anytypeio.anytype.presentation.library.LibraryScreenState
import com.anytypeio.anytype.presentation.library.LibraryViewModel
import com.anytypeio.anytype.ui.library.LibraryListConfig
import com.anytypeio.anytype.ui.library.ScreenState
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState

@ExperimentalAnimationApi
@ExperimentalPagerApi
@Composable
fun LibraryListView(
    libraryListConfig: List<LibraryListConfig>,
    tabs: LibraryScreenState.Tabs,
    vmEventStream: (LibraryEvent) -> Unit,
    vmAnalyticsStream: (LibraryAnalyticsEvent.Ui) -> Unit,
    screenState: MutableState<ScreenState>,
    effects: LibraryViewModel.Effect,
) {
    val pagerState = rememberPagerState(INITIAL_TAB)
    val modifier = Modifier.background(
        color = colorResource(id = R.color.background_primary)
    )
    Column {
        LibraryListTabs(pagerState, libraryListConfig, modifier)
        LibraryListTabsContent(
            modifier = modifier,
            pagerState = pagerState,
            configuration = libraryListConfig,
            tabs = tabs,
            vmEventStream = vmEventStream,
            vmAnalyticsStream = vmAnalyticsStream,
            screenState = screenState,
            effects = effects
        )
    }
}

private const val INITIAL_TAB = 0