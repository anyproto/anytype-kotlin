package com.anytypeio.anytype.ui.multiplayer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Screen() {
    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (title, first, second, third, pager, btn) = createRefs()

        Text(
            text = "Collaborate on spaces",
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
                .constrainAs(pager) {
                    top.linkTo(title.bottom)
                    bottom.linkTo(first.top)
                    height = Dimension.fillToConstraints
                }
                .fillMaxSize()
                .background(Color.Red)
        ) {
            Text(
                text = "Hello",
            )
        }


        Text(
            text = "1. Tap the Space widget to access settings",
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .padding(bottom = 8.dp, start = 24.dp, end = 24.dp)
                .constrainAs(first) {
                    bottom.linkTo(second.top)
                }
        )

        Text(
            text = "2. Open Share section",
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .padding(bottom = 8.dp, start = 24.dp, end = 24.dp)
                .constrainAs(second) {
                    bottom.linkTo(third.top)
                }
        )

        Text(
            text = "3. Generate an invite link and share it",
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 30.dp)
                .constrainAs(third) {
                    bottom.linkTo(btn.top)
                }
        )

        ButtonSecondary(
            onClick = { /*TODO*/ },
            size = ButtonSize.Large,
            text = "Next",
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


@Preview
@Composable
fun ScreenPreview() {
    Screen()
}