package com.anytypeio.anytype.core_ui.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.animations.AnimationSpecs
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs
import com.anytypeio.anytype.core_ui.views.animations.LoadingIndicatorState
import com.anytypeio.anytype.core_ui.views.animations.LoadingIndicatorStateImpl
import kotlinx.coroutines.delay


@Composable
fun LoadingButtonPrimary(
    text: String = "",
    modifier: Modifier = Modifier,
    size: ButtonSize,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val contentAlpha by animateFloatAsState(targetValue = if (loading) 0f else 1f)
    val loadingAlpha by animateFloatAsState(targetValue = if (loading) 1f else 0f)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val backgroundColor =
        if (isPressed.value) colorResource(id = R.color.button_pressed) else colorResource(
            id = R.color.glyph_selected
        )

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Box(contentAlignment = Alignment.Center) {
            Button(
                onClick = onClick,
                interactionSource = interactionSource,
                enabled = enabled,
                shape = RoundedCornerShape(size.cornerSize),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = backgroundColor,
                    contentColor = colorResource(id = R.color.button_text),
                    disabledBackgroundColor = colorResource(id = R.color.shape_tertiary),
                    disabledContentColor = colorResource(id = R.color.text_tertiary)
                ),
                modifier = modifier
                    .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                ),
                contentPadding = size.contentPadding
            ) {
                Text(
                    text = text,
                    modifier = Modifier.graphicsLayer { alpha = contentAlpha }
                )
            }
            LoadingIndicator(
                animating = loading,
                modifier = Modifier.graphicsLayer { alpha = loadingAlpha },
                animationSpecs = FadeAnimationSpecs(itemCount = 3),
                color = colorResource(id = R.color.button_text)
            )
        }
    }
}

@Composable
fun rememberLoadingIndicatorState(
    animating: Boolean,
    animationSpecs: AnimationSpecs
): LoadingIndicatorState {
    val state = remember { LoadingIndicatorStateImpl(animationSpecs.itemCount) }
    LaunchedEffect(key1 = animating) {
        if (animating) {
            state.start(animationSpecs, this)
        }
    }
    return state
}

@Composable
private fun LoadingIndicator(
    animating: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    indicatorSpacing: Dp = 16.dp,
    animationSpecs: AnimationSpecs
) {
    val state = rememberLoadingIndicatorState(animating, animationSpecs)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(animationSpecs.itemCount) { index ->
            LoadingDot(
                modifier = Modifier
                    .padding(horizontal = indicatorSpacing)
                    .width(DotSize)
                    .aspectRatio(1f)
                    .then(Modifier.graphicsLayer { alpha = state[index] }),
                color = color,
            )
        }
    }
}

@Composable
private fun LoadingDot(
    color: Color,
    modifier: Modifier = Modifier,
    delay: Long = 300
) {
    val colorState = remember { mutableStateOf(color) }
    val filledColor = color.copy(alpha = 1f)
    val transparentColor = color.copy(alpha = 0f)

    LaunchedEffect(key1 = colorState.value) {
        delay(delay)
        while (true) {
            colorState.value = transparentColor
            delay(delay)
            colorState.value = filledColor
            delay(delay)
        }
    }

    Canvas(
        modifier = modifier
    ) {
        drawCircle(
            color = color,
            style = Stroke(width = 1.dp.toPx()),
            radius = 3.dp.toPx()
        )
    }
}

private val DotSize = 12.dp
private val DotSpacing = 16.dp
private val DotRadius = 3.dp

@Preview
@Composable
fun previewDot() {
    LoadingDot(
        color = colorResource(id = R.color.text_primary)
    )
}

