package com.anytypeio.anytype.gallery_experience.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anytypeio.anytype.core_models.ManifestInfo
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationState

@Composable
fun GalleryInstallationScreen(
    state: GalleryInstallationState,
    onInstallClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .nestedScroll(rememberNestedScrollInteropConnection())
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (state) {
                GalleryInstallationState.Hidden -> {}
                GalleryInstallationState.Loading -> {
                    LoadingScreen()
                }
                is GalleryInstallationState.Success -> {
                    SuccessScreen(state, onInstallClicked)
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFF5F5F5),
            Color(0xFFEBEBEB),
            Color(0xFFF5F5F5)
        ),
        start = Offset(10f, 10f),
        end = Offset(translateAnim, translateAnim)
    )
    Spacer(modifier = Modifier.height(24.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(horizontal = 20.dp)
            .background(shape = RoundedCornerShape(12.dp), brush = brush)
    )
    Spacer(modifier = Modifier.height(24.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(start = 20.dp, end = 52.dp)
            .background(brush = brush, shape = RoundedCornerShape(4.dp))
    )
    Spacer(modifier = Modifier.height(12.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .padding(start = 20.dp, end = 20.dp)
            .background(brush = brush, shape = RoundedCornerShape(4.dp))
    )
    Spacer(modifier = Modifier.height(12.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .padding(start = 20.dp, end = 95.dp)
            .background(brush = brush, shape = RoundedCornerShape(4.dp))
    )
    Spacer(modifier = Modifier.height(12.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .padding(start = 20.dp, end = 35.dp)
            .background(brush = brush, shape = RoundedCornerShape(4.dp))
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun SuccessScreen(
    state: GalleryInstallationState.Success,
    onInstallClicked: () -> Unit
) {
    val dotCurrentColor = colorResource(id = R.color.glyph_active)
    val dotColor = colorResource(id = R.color.glyph_inactive)
    val pagerState = rememberPagerState {
        state.info.screenshots.size
    }
    Spacer(modifier = Modifier.height(71.dp))
    HorizontalPager(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 20.dp),
        state = pagerState
    ) { index ->
        val screenshotUrl = state.info.screenshots[index]
        val imageModifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(8.dp))
        AsyncImage(
            modifier = imageModifier,
            model = screenshotUrl,
            contentDescription = "Gallery experience screenshot",
            placeholder = ColorPainter(colorResource(id = R.color.background_secondary)),
            error = ColorPainter(colorResource(id = R.color.background_secondary)),
            onError = { _ -> imageModifier.shadow(4.dp) },
        )
    }
    Spacer(modifier = Modifier.height(60.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .height(24.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(state.info.screenshots.size) { iteration ->
            val color =
                if (pagerState.currentPage == iteration) dotCurrentColor else dotColor
            Box(
                modifier = Modifier
                    .padding(horizontal = 9.dp)
                    .background(color, CircleShape)
                    .size(8.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        text = state.info.title,
        style = HeadlineTitle,
        color = colorResource(id = R.color.text_primary)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        text = state.info.description,
        style = UXBody,
        color = colorResource(id = R.color.text_primary)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        text = "${stringResource(id = R.string.gallery_experience_made)} @${state.info.author}",
        style = Caption1Regular,
        color = colorResource(id = R.color.text_secondary)
    )
    Spacer(modifier = Modifier.height(16.dp))

    FlowRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        state.info.categories.forEach { category ->
            CategoryItem(item = category)
        }
    }
    Spacer(modifier = Modifier.height(39.dp))
    ButtonPrimary(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        onClick = { onInstallClicked() },
        size = ButtonSize.Large,
        text = stringResource(id = R.string.gallery_experience_install)
    )
}

@Composable
private fun CategoryItem(item: String) {
    Text(
        text = item,
        color = colorResource(id = R.color.text_secondary),
        modifier = Modifier
            .wrapContentWidth()
            .background(
                color = colorResource(id = R.color.shape_tertiary),
                shape = RoundedCornerShape(size = 4.dp)
            )
            .padding(start = 6.dp, end = 6.dp, top = 1.dp, bottom = 1.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = Caption1Medium
    )
}

@Preview
@Composable
private fun GalleryInstallationScreenPreview() {
    GalleryInstallationScreen(
        onInstallClicked = {},
        state = GalleryInstallationState.Success(
            info = ManifestInfo(
                schema = "placerat",
                id = "vix",
                name = "Agnes Lucas",
                author = "constituto",
                license = "accommodare",
                title = "reprimique",
                description = "pretium",
                screenshots = listOf("1", "2", "3", "4"),
                downloadLink = "lobortis",
                fileSize = 1213,
                categories = listOf("tag1", "tag2", "tag3312212112", "tag421312312", "tag5", "tag6"),
                language = "nisi"
            )
        )
    )
}