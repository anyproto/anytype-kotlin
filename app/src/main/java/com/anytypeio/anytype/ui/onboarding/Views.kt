package com.anytypeio.anytype.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.ColorPagerIndicator
import com.anytypeio.anytype.core_ui.ColorPagerIndicatorSelected
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PagerIndicator(pageCount: Int, pagerState: PagerState) {
    Row {
        repeat(pageCount) { iteration ->
            val color = if (pagerState.currentPage == iteration) {
                ColorPagerIndicatorSelected
            }  else {
                ColorPagerIndicator
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(4.dp)
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Preview
@Composable
fun PagerIndicatorPreview() {
    val pageCount = 4
    val pagerState = rememberPagerState()
    PagerIndicator(pageCount, pagerState)
}