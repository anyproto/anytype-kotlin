package com.anytypeio.anytype.ui.library.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.ui.library.LibraryConfiguration
import com.anytypeio.anytype.ui.library.LibraryScreenConfig
import com.anytypeio.anytype.ui.library.ScreenState
import com.anytypeio.anytype.ui.library.WrapWithLibraryAnimation
import com.anytypeio.anytype.ui.library.styles.TabTitleStyle
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.launch

@ExperimentalPagerApi
@Composable
fun LibraryTabs(
    modifier: Modifier,
    pagerState: PagerState,
    configuration: LibraryConfiguration,
    screenState: MutableState<ScreenState>
) = WrapWithLibraryAnimation(visible = screenState.value.visible()) {
    val coroutineScope = rememberCoroutineScope()

    val density = LocalDensity.current
    val tabWidths = remember {
        val tabWidthStateList = mutableStateListOf(0.dp, 0.dp)
        tabWidthStateList
    }

    TabRow(
        modifier = modifier,
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = colorResource(id = R.color.background_primary),
        divider = {},
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier
                    .libraryTabOffset(
                        currentTabPosition = tabPositions[pagerState.currentPage],
                        tabWidth = tabWidths[pagerState.currentPage]
                    ),
                color = colorResource(id = R.color.black)
            )
        },
        tabs = {
            CompositionLocalProvider(LocalRippleTheme provides LibraryTabsTheme) {
                LibraryTab(
                    modifier = modifier,
                    config = configuration.types,
                    pagerState = pagerState,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    onTextLayout = { tlResult ->
                        tabWidths[0] = with(density) { tlResult.size.width.toDp() }
                    }
                )
                LibraryTab(
                    modifier = modifier,
                    config = configuration.relations,
                    pagerState = pagerState,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    onTextLayout = { tlResult ->
                        tabWidths[1] = with(density) { tlResult.size.width.toDp() }
                    }
                )
            }
        }
    )

}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun LibraryTab(
    modifier: Modifier,
    config: LibraryScreenConfig,
    pagerState: PagerState,
    onClick: () -> Unit,
    onTextLayout: (TextLayoutResult) -> Unit
) {

    Tab(
        modifier = modifier,
        selectedContentColor = colorResource(id = R.color.glyph_selected),
        unselectedContentColor = colorResource(id = R.color.glyph_active),
        text = {
            Column(
                horizontalAlignment = config.titleAlignment,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = config.titlePaddingStart,
                        end = config.titlePaddingEnd,
                    )
            ) {
                Text(
                    text = stringResource(id = config.mainTitle),
                    style = TabTitleStyle,
                    onTextLayout = onTextLayout::invoke
                )
            }
        },
        selected = pagerState.currentPage == config.index,
        onClick = onClick,
    )
}