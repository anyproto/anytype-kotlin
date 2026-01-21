package com.anytypeio.anytype.feature_vault.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.feature_vault.R
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SpacesIntroductionScreen(
    onDismiss: () -> Unit = {},
    onComplete: () -> Unit = {},
    onPageChanged: (step: Int) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage + 1)
    }

    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxSize()
                .background(
                    shape = RoundedCornerShape(12.dp),
                    color = colorResource(id = R.color.background_secondary)
                )
        ) {
            // Drag indicator
            Dragger(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp)
            )

            // 48dp spacing after drag indicator (matching iOS)
            Spacer(modifier = Modifier.height(48.dp))

            // Carousel
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(0.7f),
            ) { page ->
                when (page) {
                    0 -> IntroductionPage(
                        imageLightRes = R.drawable.introduce_chat_1,
                        imageDarkRes = R.drawable.introduce_chat_1_dark,
                        titleRes = R.string.spaces_introduction_page1_title,
                        descriptionRes = R.string.spaces_introduction_page1_description
                    )

                    1 -> IntroductionPage(
                        imageLightRes = R.drawable.introduce_chat_2,
                        imageDarkRes = R.drawable.introduce_chat_2_dark,
                        titleRes = R.string.spaces_introduction_page2_title,
                        descriptionRes = R.string.spaces_introduction_page2_description
                    )

                    2 -> IntroductionPage(
                        imageLightRes = R.drawable.introduce_chat_3,
                        imageDarkRes = R.drawable.introduce_chat_3_dark,
                        titleRes = R.string.spaces_introduction_page3_title,
                        descriptionRes = R.string.spaces_introduction_page3_description
                    )

                    3 -> IntroductionPage(
                        imageLightRes = R.drawable.introduce_chat_4,
                        imageDarkRes = R.drawable.introduce_chat_4_dark,
                        titleRes = R.string.spaces_introduction_page4_title,
                        descriptionRes = R.string.spaces_introduction_page4_description
                    )
                }
            }

            // 34dp spacing between carousel and page indicator (matching iOS)
            Spacer(modifier = Modifier.height(34.dp))

            // Page indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index)
                                    colorResource(id = R.color.glyph_active)
                                else
                                    colorResource(id = R.color.shape_transparent_secondary)
                            )
                    )
                    if (index < 3) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            // 16dp spacing between page indicator and button (matching iOS)
            Spacer(modifier = Modifier.height(16.dp))

            // Action button
            ButtonSecondary(
                onClick = {
                    if (pagerState.currentPage < 3) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onComplete()
                    }
                },
                text = if (pagerState.currentPage < 3) {
                    stringResource(id = R.string.next)
                } else {
                    stringResource(id = R.string.spaces_introduction_try_it)
                },
                size = ButtonSize.Large,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            // 20dp bottom spacing (matching iOS)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Single page content following iOS spacing specifications:
 * - 40dp between image and title
 * - 9dp between title and description
 * - 24dp horizontal padding for text
 *
 * @param imageLightRes Image resource for light theme
 * @param imageDarkRes Image resource for dark theme
 * @param titleRes Title string resource
 * @param descriptionRes Description string resource
 */
@Composable
private fun IntroductionPage(
    imageLightRes: Int,
    imageDarkRes: Int,
    titleRes: Int,
    descriptionRes: Int
) {
    val imageRes = if (isSystemInDarkTheme()) imageDarkRes else imageLightRes
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image - takes maximum available height
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // 40dp spacing between image and title (matching iOS)
        Spacer(modifier = Modifier.height(40.dp))

        // Title
        Text(
            text = stringResource(id = titleRes),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )

        // 9dp spacing between title and description (matching iOS)
        Spacer(modifier = Modifier.height(9.dp))

        // Description with 24dp horizontal padding (matching iOS)
        Text(
            text = stringResource(id = descriptionRes),
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
            maxLines = 3
        )
    }
}

@DefaultPreviews
@Composable
private fun SpacesIntroductionScreenPreview() {
    SpacesIntroductionScreen()
}

@DefaultPreviews
@Composable
private fun Introduction1ScreenPreview() {
    IntroductionPage(
        imageLightRes = R.drawable.introduce_chat_1,
        imageDarkRes = R.drawable.introduce_chat_1_dark,
        titleRes = R.string.spaces_introduction_page1_title,
        descriptionRes = R.string.spaces_introduction_page1_description
    )
}
