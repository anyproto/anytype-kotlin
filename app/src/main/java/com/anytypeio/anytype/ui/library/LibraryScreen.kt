package com.anytypeio.anytype.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.library.LibraryViewModel
import com.anytypeio.anytype.ui.library.views.LibraryTabs
import com.anytypeio.anytype.ui.library.views.LibraryTabsContent
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.FlowPreview


@FlowPreview
@ExperimentalLifecycleComposeApi
@ExperimentalPagerApi
@Composable
fun LibraryScreen(configuration: List<LibraryScreenConfig>, viewModel: LibraryViewModel) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(INITIAL_TAB)
    val modifier = Modifier.background(color = colorResource(id = R.color.background_primary))

    Column(modifier = modifier) {
        LibraryTabs(
            modifier = modifier,
            pagerState = pagerState,
            configuration = configuration
        )
        LibraryTabsContent(
            modifier = modifier,
            pagerState = pagerState,
            configuration = configuration,
            state = uiState,
            vmEventStream = viewModel::eventStream
        )
    }
}

private const val INITIAL_TAB = 0