package com.anytypeio.anytype.ui.library.views

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.library.LibraryAnalyticsEvent
import com.anytypeio.anytype.presentation.library.LibraryEvent
import com.anytypeio.anytype.presentation.library.LibraryScreenState
import com.anytypeio.anytype.presentation.library.LibraryViewModel
import com.anytypeio.anytype.ui.library.LibraryScreenConfig
import com.anytypeio.anytype.ui.library.ScreenState
import com.anytypeio.anytype.ui.library.WrapWithLibraryAnimation
import com.anytypeio.anytype.ui.library.views.list.LibraryListView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.FlowPreview

@ExperimentalAnimationApi
@FlowPreview
@ExperimentalPagerApi
@Composable
fun LibraryTabsContent(
    modifier: Modifier,
    pagerState: PagerState,
    configuration: List<LibraryScreenConfig>,
    state: LibraryScreenState,
    vmEventStream: (LibraryEvent) -> Unit,
    vmAnalyticsStream: (LibraryAnalyticsEvent.Ui) -> Unit,
    screenState: MutableState<ScreenState>,
    effects: LibraryViewModel.Effect,
) {
    HorizontalPager(modifier = modifier, state = pagerState, count = 2) { page ->
        val dataTabs = when (configuration[page]) {
            is LibraryScreenConfig.Types -> {
                state.types
            }
            is LibraryScreenConfig.Relations -> {
                state.relations
            }
        }
        TabContentScreen(
            modifier = modifier,
            config = configuration[page],
            tabs = dataTabs,
            vmEventStream = vmEventStream,
            vmAnalyticsStream = vmAnalyticsStream,
            screenState = screenState,
            effects = effects
        )
    }
}

@ExperimentalAnimationApi
@FlowPreview
@ExperimentalPagerApi
@Composable
fun TabContentScreen(
    modifier: Modifier,
    config: LibraryScreenConfig,
    tabs: LibraryScreenState.Tabs,
    vmEventStream: (LibraryEvent) -> Unit,
    vmAnalyticsStream: (LibraryAnalyticsEvent.Ui) -> Unit,
    screenState: MutableState<ScreenState>,
    effects: LibraryViewModel.Effect
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Header(config, vmEventStream, screenState)
        LibraryListView(
            libraryListConfig = config.listConfig,
            tabs = tabs,
            vmEventStream = vmEventStream,
            vmAnalyticsStream = vmAnalyticsStream,
            screenState = screenState,
            effects = effects
        )
    }

}

@Composable
private fun Header(
    config: LibraryScreenConfig,
    vmEventStream: (LibraryEvent) -> Unit,
    screenState: MutableState<ScreenState>
) = WrapWithLibraryAnimation(visible = screenState.value.visible()) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            color = colorResource(id = R.color.text_primary),
            text = stringResource(config.description),
            style = MaterialTheme.typography.h1,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                top = 58.dp,
                start = HeaderDefaults.HeaderPadding,
                end = HeaderDefaults.HeaderPadding
            )
        )
        Box(Modifier.height(18.dp))
        Button(
            onClick = {
                when (config) {
                    is LibraryScreenConfig.Types -> {
                        vmEventStream.invoke(LibraryEvent.Type.Create())
                    }
                    is LibraryScreenConfig.Relations -> {
                        vmEventStream.invoke(LibraryEvent.Relation.Create())
                    }
                }
            },
            modifier = Modifier.padding(bottom = 48.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.glyph_selected)
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = HeaderDefaults.ButtonContentPaddingValues,
            content = {
                Text(
                    text = stringResource(config.mainBtnTitle),
                    color = colorResource(id = R.color.glyph_label),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        )
    }
}

private object HeaderDefaults {
    val HeaderPadding = 20.dp
    val ButtonContentPaddingValues = PaddingValues(
        28.dp, 10.dp, 28.dp, 10.dp
    )
}