package com.anytypeio.anytype.ui.vault

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
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import kotlinx.coroutines.launch

@Deprecated("To be deleted")
@Composable
fun IntroduceVaultScreen(
    onDoneClicked: () -> Unit
) {
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {

        val coroutineScope = rememberCoroutineScope()
        val (title, first,  pager, dots, btn, dragger) = createRefs()

        val pagerState = rememberPagerState(pageCount = { 2 })

        Dragger(
            modifier = Modifier.padding(
                vertical = 6.dp
            ).constrainAs(dragger) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        Text(
            text = when(val page = pagerState.currentPage) {
                0 -> {
                    stringResource(R.string.introduce_vault_welcome_to_the_vault)
                }
                1 -> {
                    stringResource(R.string.introduce_vault_simple_flexible)
                }
                else -> ""
            },
            style = HeadlineHeading,
            textAlign = TextAlign.Center,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .padding(top = 40.dp)
                .constrainAs(title) {}
                .fillMaxWidth()
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(bottom = 20.dp, top = 42.dp)
                .fillMaxWidth()
                .constrainAs(pager) {
                    top.linkTo(title.bottom)
                    bottom.linkTo(dots.top)
                    height = Dimension.fillToConstraints
                }
                .fillMaxSize()
        ) { page ->
            when(page) {
                0 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.align(Alignment.TopCenter)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_introduce_vault_1),
                                contentDescription = "Screenshot 1",
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    }
                }
                1 -> {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_introduce_vault_2),
                            contentDescription = "Screenshot 2",
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp)
                .constrainAs(dots) {
                    bottom.linkTo(first.top)
                },
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(2) { iteration ->
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
            text =  when(val page = pagerState.currentPage) {
                0 -> {
                    stringResource(R.string.introduce_vault_text_1)
                }
                1 -> {
                    stringResource(R.string.introduce_vault_text_2)
                }
                else -> ""
            },
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .padding(bottom = 30.dp, start = 24.dp, end = 24.dp)
                .constrainAs(first) {
                    bottom.linkTo(btn.top)
                },
            textAlign = TextAlign.Center,
            minLines = 3
        )

        ButtonSecondary(
            onClick = {
                coroutineScope.launch {
                    if (pagerState.currentPage == 1) {
                        onDoneClicked()
                    } else {
                        pagerState.animateScrollToPage(pagerState.currentPage.inc(), 0f)
                    }
                }
            },
            size = ButtonSize.LargeSecondary,
            text = if (pagerState.currentPage == 1)
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
    IntroduceVaultScreen(
        onDoneClicked = {}
    )
}