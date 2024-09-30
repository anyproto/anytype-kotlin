package com.anytypeio.anytype.ui.multiplayer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntroduceSpaceSharingScreen(
    onDoneClicked: () -> Unit
) {
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFFEF2C6),
                    ),
                    start = Offset(
                        screenWidth.value * 2,
                        screenHeight.value * 2.2f
                    )
                )
            )
    ) {

        val coroutineScope = rememberCoroutineScope()
        val (title, first, second, third, pager, dots, btn) = createRefs()

        Text(
            text = stringResource(R.string.multiplayer_collaborate_on_spaces),
            style = HeadlineHeading,
            textAlign = TextAlign.Center,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .padding(top = 40.dp)
                .constrainAs(title) {}
                .fillMaxWidth()
        )

        val pagerState = rememberPagerState(pageCount = { 3 })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(bottom = 16.dp, top = 38.dp)
                .constrainAs(pager) {
                    top.linkTo(title.bottom)
                    bottom.linkTo(dots.top)
                    height = Dimension.fillToConstraints
                }
                .fillMaxSize()
        ) { page ->
            when(page) {
                0 -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_sharing_step_first),
                            contentDescription = "Screenshot 1",
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
                1 -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_sharing_step_second),
                            contentDescription = "Screenshot 2",
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
                2 -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_sharing_step_third),
                            contentDescription = "Screenshot 3",
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 46.dp)
                .constrainAs(dots) {
                    bottom.linkTo(first.top)
                },
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { iteration ->
                val color = if (pagerState.currentPage == iteration)
                    colorResource(id = R.color.glyph_active)
                else
                    colorResource(id = R.color.glyph_inactive)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .background(color, CircleShape)
                        .size(6.dp)
                )
            }
        }

        Text(
            text = stringResource(R.string.multiplayer_collaborate_step_1),
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .padding(bottom = 8.dp, start = 24.dp, end = 24.dp)
                .constrainAs(first) {
                    bottom.linkTo(second.top)
                }
        )

        Text(
            text = stringResource(R.string.multiplayer_collaborate_step_2),
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .padding(bottom = 8.dp, start = 24.dp, end = 24.dp)
                .constrainAs(second) {
                    bottom.linkTo(third.top)
                }
        )

        Text(
            text = stringResource(R.string.multiplayer_collaborate_step_3),
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 30.dp)
                .constrainAs(third) {
                    bottom.linkTo(btn.top)
                }
        )

        ButtonSecondary(
            onClick = {
                coroutineScope.launch {
                    if (pagerState.currentPage == 2) {
                        onDoneClicked()
                    } else {
                        pagerState.animateScrollToPage(pagerState.currentPage.inc(), 0f)
                    }
                }
            },
            size = ButtonSize.LargeSecondary,
            text = if (pagerState.currentPage == 2)
                stringResource(id = R.string.done)
            else
                stringResource(id = R.string.next),
            modifier = Modifier
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 32.dp
                )
                .fillMaxWidth()
                .constrainAs(btn) {
                    bottom.linkTo(parent.bottom)
                }
        )
    }
}


@DefaultPreviews
@Composable
private fun ScreenPreview() {
    IntroduceSpaceSharingScreen(
        onDoneClicked = {}
    )
}