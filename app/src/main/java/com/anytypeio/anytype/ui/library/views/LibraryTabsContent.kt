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
import com.anytypeio.anytype.ui.library.LibraryScreenConfig
import com.anytypeio.anytype.ui.library.views.list.LibraryListView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState

@ExperimentalPagerApi
@Composable
fun LibraryTabsContent(
    modifier: Modifier,
    pagerState: PagerState,
    configuration: List<LibraryScreenConfig>
) {
    HorizontalPager(modifier = modifier, state = pagerState, count = configuration.size) { page ->
        TabContentScreen(modifier = modifier, config = configuration[page])
    }
}

@ExperimentalPagerApi
@Composable
fun TabContentScreen(
    modifier: Modifier,
    config: LibraryScreenConfig,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 58.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            color = colorResource(id = R.color.black),
            text = stringResource(config.description),
            style = MaterialTheme.typography.h1,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp)
        )
        Box(Modifier.height(18.dp))
        Button(
            onClick = { /*TODO*/ },
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.black)),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(
                28.dp, 10.dp, 28.dp, 10.dp
            ),
            content = {
                Text(
                    text = stringResource(config.mainBtnTitle),
                    color = colorResource(id = R.color.library_action_btn_text_color),
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
        LibraryListView(libraryListConfig = config.listConfig)
    }

}