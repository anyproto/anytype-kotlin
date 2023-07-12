package com.anytypeio.anytype.ui.onboarding.screens.signup

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.ColorButtonPrimaryText
import com.anytypeio.anytype.core_ui.ColorSoulConnectorLine
import com.anytypeio.anytype.core_ui.ColorSpaceBackground
import com.anytypeio.anytype.core_ui.OnBoardingTextPrimaryColor
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSoulCreationAnimViewModel
import kotlinx.coroutines.delay

@Composable
fun CreateSoulAnimWrapper(
    contentPaddingTop: Int,
    viewModel: OnboardingSoulCreationAnimViewModel,
    onAnimationComplete: () -> Unit,
) {
    val state = viewModel.accountData.collectAsStateWithLifecycle().value
    CreateSoulAnimScreen(state, onAnimationComplete, contentPaddingTop)
}

@Composable
private fun CreateSoulAnimScreen(
    state: Resultat<OnboardingSoulCreationAnimViewModel.AccountData>,
    onAnimationComplete: () -> Unit,
    contentPaddingTop: Int
) {
    val isBoxVisible = remember { mutableStateOf(false) }
    val targetElementsAlpha by animateFloatAsState(
        targetValue = if (isBoxVisible.value) 1f else 0f,
        animationSpec = tween(durationMillis = ANIMATION_DURATION)
    )
    Box(modifier = Modifier.fillMaxSize()) {
        state.fold(
            onSuccess = {
                val gradient = Brush.radialGradient(
                    listOf(
                        Color(it.icon.from.toColorInt()),
                        Color(it.icon.to.toColorInt())
                    )
                )
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
                    Spacer(modifier = Modifier.height(contentPaddingTop.dp))
                    CreateSoulAnimationTitle(targetElementsAlpha)
                    CreateSoulAnimation(
                        isBoxVisible,
                        targetElementsAlpha,
                        gradient,
                        it.name
                    )
                }
                LaunchedEffect(Unit) {
                    delay(ANIMATION_START_DELAY)
                    isBoxVisible.value = true
                }
                LaunchedEffect(Unit) {
                    delay(ANIMATION_COMPLETE_DELAY)
                    onAnimationComplete.invoke()
                }
            }
        )
    }
}

@Composable
private fun CreateSoulAnimation(
    isBoxVisible: MutableState<Boolean>,
    targetElementsAlpha: Float,
    gradient: Brush,
    name: String
) {

    val circleOffset = animateDpAsState(
        targetValue = if (isBoxVisible.value) 96.dp else 0.dp,
        animationSpec = tween(durationMillis = ANIMATION_DURATION)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {

        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 152.dp, height = 48.dp)
        ) {
            if (isBoxVisible.value) {
                drawLine(
                    color = ColorSoulConnectorLine,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width / 2 + circleOffset.value.toPx(), size.height / 2),
                    strokeWidth = 2.dp.toPx(),
                    alpha = targetElementsAlpha
                )
            }
        }

        if (isBoxVisible.value) {
            Box(
                modifier = Modifier
                    .offset(x = circleOffset.value)
                    .alpha(targetElementsAlpha)
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(ColorSpaceBackground, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(gradient, CircleShape)
                )
            }

            Text(
                text = stringResource(id = R.string.onboarding_soul_creation_personal_space),
                style = PreviewTitle2Medium.copy(
                    color = ColorButtonPrimaryText
                ),
                modifier = Modifier
                    .offset(x = circleOffset.value)
                    .alpha(targetElementsAlpha)
                    .padding(top = 88.dp)
                    .align(Alignment.Center)
            )
        }

        Box(
            modifier = Modifier
                .offset(x = -circleOffset.value)
                .align(Alignment.Center)
                .size(48.dp)
                .background(gradient, CircleShape),
            contentAlignment = Alignment.Center
        ) {}

        Text(
            text = name,
            style = PreviewTitle2Medium.copy(
                color = ColorButtonPrimaryText
            ),
            modifier = Modifier
                .offset(x = -circleOffset.value)
                .padding(top = 88.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun CreateSoulAnimationTitle(targetElementsAlpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 48.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            CreateSoulAnimTitle(
                modifier = Modifier.alpha(1 - targetElementsAlpha),
                text = stringResource(id = R.string.onboarding_soul_creation)
            )
            CreateSoulAnimTitle(
                modifier = Modifier.alpha(targetElementsAlpha),
                text = stringResource(id = R.string.onboarding_soul_space_creation)
            )
        }
    }
}

@Composable
fun CreateSoulAnimTitle(modifier: Modifier, text: String) {
    Text(modifier = modifier, text = text, style = Title1.copy(color = OnBoardingTextPrimaryColor))
}

private const val ANIMATION_START_DELAY = 2000L
private const val ANIMATION_COMPLETE_DELAY = 5000L
private const val ANIMATION_DURATION = 800