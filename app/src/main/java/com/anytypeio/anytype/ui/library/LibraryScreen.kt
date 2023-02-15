@file:OptIn(ExperimentalAnimationApi::class)

package com.anytypeio.anytype.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.library.LibraryViewModel
import com.anytypeio.anytype.ui.library.views.LibraryTabs
import com.anytypeio.anytype.ui.library.views.LibraryTabsContent
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.FlowPreview


@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@FlowPreview
@ExperimentalPagerApi
@Composable
fun LibraryScreen(configuration: LibraryConfiguration, viewModel: LibraryViewModel) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(INITIAL_TAB)
    val modifier = Modifier.background(color = colorResource(id = R.color.background_primary))

    val screenState = remember { mutableStateOf(ScreenState.CONTENT) }

    Column(modifier = modifier) {
        LibraryTabs(
            modifier = modifier,
            pagerState = pagerState,
            configuration = configuration,
            screenState = screenState,
        )
        LibraryTabsContent(
            modifier = modifier,
            pagerState = pagerState,
            configuration = listOf(configuration.types, configuration.relations),
            state = uiState,
            vmEventStream = viewModel::eventStream,
            screenState = screenState,
        )
    }

}

@Composable
fun WrapWithLibraryAnimation(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically() ,
        exit = fadeOut()+ shrinkVertically()
    ) { content() }
}

enum class ScreenState {
    CONTENT,
    SEARCH;
    fun visible() = this == CONTENT
}

private const val INITIAL_TAB = 0