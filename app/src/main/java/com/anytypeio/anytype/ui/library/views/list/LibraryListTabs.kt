package com.anytypeio.anytype.ui.library.views.list

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.ui.library.LibraryListConfig
import com.anytypeio.anytype.ui.library.views.LibraryTabsTheme
import com.anytypeio.anytype.ui.library.styles.TabSubTitleStyle
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@ExperimentalPagerApi
@Composable
fun LibraryListTabs(
    pagerState: PagerState,
    configuration: List<LibraryListConfig>,
    modifier: Modifier
) {
    val scope = rememberCoroutineScope()
    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = colorResource(id = R.color.background_primary),
        indicator = {},
        divider = {},
        edgePadding = 0.dp,
        modifier = modifier,
        tabs = {
            CompositionLocalProvider(LocalRippleTheme provides LibraryTabsTheme) {
                configuration.forEachIndexed { index, it ->
                    LibraryListTab(
                        config = it,
                        pagerState = pagerState,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        index = index,
                        modifier
                    )
                }
            }
        }
    )
}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun LibraryListTab(
    config: LibraryListConfig,
    pagerState: PagerState,
    onClick: () -> Unit,
    index: Int,
    modifier: Modifier
) {
    Tab(
        selectedContentColor = colorResource(id = R.color.glyph_selected),
        unselectedContentColor = colorResource(id = R.color.glyph_active),
        modifier = modifier.wrapContentWidth(),
        text = {
            Text(
                text = stringResource(id = config.title),
                style = TabSubTitleStyle,
                modifier = modifier
                    .wrapContentWidth()
                    .offset(x = config.subtitleTabOffset),
            )
        },
        selected = pagerState.currentPage == index,
        onClick = onClick,
    )
}

@ExperimentalPagerApi
@Preview
@Composable
fun LibraryListTabsPreview() {
    val pagerState = rememberPagerState(0)
    LibraryListTabs(
        pagerState = pagerState,
        configuration = listOf(
            LibraryListConfig.Types,
            LibraryListConfig.TypesLibrary
        ),
        Modifier
    )
}