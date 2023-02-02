package com.anytypeio.anytype.ui.library.views

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.library.LibraryEvent
import com.anytypeio.anytype.presentation.library.LibraryScreenState
import com.anytypeio.anytype.ui.library.LibraryScreenConfig
import com.anytypeio.anytype.ui.library.views.list.LibraryListView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalPagerApi
@Composable
fun LibraryTabsContent(
    modifier: Modifier,
    pagerState: PagerState,
    configuration: List<LibraryScreenConfig>,
    state: LibraryScreenState,
    vmEventStream: (LibraryEvent) -> Unit,
) {
    HorizontalPager(modifier = modifier, state = pagerState, count = configuration.size) { page ->
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
            vmEventStream = vmEventStream
        )
    }
}

@FlowPreview
@ExperimentalPagerApi
@Composable
fun TabContentScreen(
    modifier: Modifier,
    config: LibraryScreenConfig,
    tabs: LibraryScreenState.Tabs,
    vmEventStream: (LibraryEvent) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 58.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            color = colorResource(id = R.color.text_primary),
            text = stringResource(config.description),
            style = MaterialTheme.typography.h1,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp)
        )
        Box(Modifier.height(18.dp))
        Button(
            onClick = { /*TODO*/ },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.glyph_selected)
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(
                28.dp, 10.dp, 28.dp, 10.dp
            ),
            content = {
                Text(
                    text = stringResource(config.mainBtnTitle),
                    color = colorResource(id = R.color.text_white),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        )
        Box(Modifier.height(48.dp))
        LibraryListView(
            libraryListConfig = config.listConfig,
            tabs = tabs,
            vmEventStream = vmEventStream
        )
    }

}