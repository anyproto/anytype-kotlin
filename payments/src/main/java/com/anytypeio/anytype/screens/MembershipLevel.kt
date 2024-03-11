package com.anytypeio.anytype.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.widgets.DragStates
import com.anytypeio.anytype.models.Tier
import com.anytypeio.anytype.peyments.R
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MembershipLevels(tier: Tier) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.shape_tertiary)),
        contentAlignment = Alignment.BottomStart,
    ) {

        val swipeableState = rememberSwipeableState(DragStates.VISIBLE)
        val keyboardController = LocalSoftwareKeyboardController.current

        if (swipeableState.isAnimationRunning && swipeableState.targetValue == DragStates.DISMISSED) {
            DisposableEffect(Unit) {
                onDispose {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            }
        }

        val sizePx =
            with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

        val tierResources = mapTierToResources(tier)

        AnimatedVisibility(
            visible = true,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .swipeable(state = swipeableState,
                    orientation = Orientation.Vertical,
                    anchors = mapOf(
                        0f to DragStates.VISIBLE, sizePx to DragStates.DISMISSED
                    ),
                    thresholds = { _, _ -> FractionalThreshold(0.3f) })
                .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
        ) {
            val brush = Brush.verticalGradient(
                listOf(
                    tierResources.colorGradient,
                    Color.Transparent
                )
            )

            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp)
                        .background(brush = brush, shape = RoundedCornerShape(16.dp)),
                    contentAlignment = androidx.compose.ui.Alignment.BottomStart
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(start = 16.dp),
                        painter = painterResource(id = tierResources.mediumIcon!!),
                        contentDescription = "logo",
                        tint = tierResources.radialGradient
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    text = tierResources.title,
                    color = colorResource(id = R.color.text_primary),
                    style = HeadlineTitle,
                    textAlign = TextAlign.Start
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 6.dp),
                    text = tierResources.subtitle,
                    color = colorResource(id = R.color.text_primary),
                    style = BodyCallout,
                    textAlign = TextAlign.Start
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 22.dp),
                    text = stringResource(id = R.string.payments_details_whats_included),
                    color = colorResource(id = R.color.text_secondary),
                    style = BodyCallout,
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(6.dp))
                tierResources.benefits.forEach { benefit ->
                    Benefit(benefit = benefit)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Spacer(modifier = Modifier.height(30.dp))
                NamePicker()
            }
        }
    }
}

@Composable
fun NamePicker() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                shape = RoundedCornerShape(8.dp),
                color = colorResource(id = R.color.background_primary)
            )
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 26.dp),
            text = stringResource(id = R.string.payments_details_name_title),
            color = colorResource(id = R.color.text_primary),
            style = BodyBold,
            textAlign = TextAlign.Start
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 6.dp),
            text = stringResource(id = R.string.payments_details_name_subtitle),
            color = colorResource(id = R.color.text_primary),
            style = BodyCallout,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun Benefit(benefit: String) {
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Image(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterStart),
            painter = painterResource(id = R.drawable.ic_check_16),
            contentDescription = "text check icon"
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp)
                .align(Alignment.CenterStart),
            text = benefit,
            style = BodyCallout,
            color = colorResource(id = R.color.text_primary)
        )
    }
}


@Preview()
@Composable
fun MyLevel() {
    MembershipLevels(tier = Tier.Explorer("121", true))
}