@file:OptIn(ExperimentalAnimationApi::class)

package com.anytypeio.anytype.ui.library

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationMenu
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.presentation.library.LibraryEvent
import com.anytypeio.anytype.presentation.library.LibraryViewModel
import com.anytypeio.anytype.ui.library.views.LibraryTabs
import com.anytypeio.anytype.ui.library.views.LibraryTabsContent
import kotlinx.coroutines.FlowPreview


@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@FlowPreview
@Composable
fun LibraryScreen(
    configuration: LibraryConfiguration,
    viewModel: LibraryViewModel,
    onBackPressed: () -> Unit,
    onCreateObjectLongClicked: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val effects by viewModel.effects.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(
        initialPage = INITIAL_TAB,
        pageCount = { 2 }
    )
    val modifier = Modifier
        .background(color = colorResource(id = R.color.background_primary))

    val screenState = remember { mutableStateOf(ScreenState.CONTENT) }

    Scaffold(
        bottomBar = {
            Box(
                modifier = modifier
                    .then(
                        if (BuildConfig.USE_EDGE_TO_EDGE && Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                            Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                        else
                            Modifier
                    )
                    .fillMaxWidth()
            ) {
                Menu(
                    viewModel,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    screenState = screenState,
                    onCreateObjectLongClicked = onCreateObjectLongClicked
                )
            }
        }
    ) {
        println(it)
        Column(
            modifier = modifier.then(
                if (BuildConfig.USE_EDGE_TO_EDGE && Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier.windowInsetsPadding(WindowInsets.systemBars)
                else
                    Modifier
            ),
        ) {
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
                vmAnalyticsStream = viewModel::analyticsStream,
                screenState = screenState,
                effects = effects
            )
        }
    }

    BackHandler {
        if (screenState.value == ScreenState.SEARCH) {
            screenState.value = ScreenState.CONTENT
        } else {
            onBackPressed.invoke()
        }
    }

}

@Composable
fun Menu(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier,
    screenState: MutableState<ScreenState>,
    onCreateObjectLongClicked: () -> Unit = {}
) {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    if (isImeVisible) return
    BottomNavigationMenu(
        modifier = modifier,
        backClick = {
            if (screenState.value == ScreenState.SEARCH) {
                screenState.value = ScreenState.CONTENT
            } else {
                viewModel.eventStream(LibraryEvent.BottomMenu.Back)
            }
        },
        homeClick = { viewModel.eventStream(LibraryEvent.BottomMenu.Back) },
        searchClick = { viewModel.eventStream(LibraryEvent.BottomMenu.Search) },
        addDocClick = { viewModel.eventStream(LibraryEvent.BottomMenu.CreateObject) },
        onCreateObjectLongClicked = onCreateObjectLongClicked,
        onProfileClicked = {
            viewModel.eventStream(LibraryEvent.BottomMenu.OpenProfile)
        },
        profileIcon = viewModel.icon.collectAsState().value
    )
}

@Composable
fun WrapWithLibraryAnimation(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val animationSpecFade = TweenSpec<Float>(
        easing = FastOutLinearInEasing,
        durationMillis = FADE_DURATION_MILLIS
    )
    val animationSpecSlide = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntSize.VisibilityThreshold
    )
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpecFade) + expandVertically(animationSpecSlide),
        exit = fadeOut(animationSpecFade) + shrinkVertically(animationSpecSlide)
    ) { content() }
}

enum class ScreenState {
    CONTENT,
    SEARCH;

    fun visible() = this == CONTENT
}

private const val INITIAL_TAB = 0
private const val FADE_DURATION_MILLIS = 50