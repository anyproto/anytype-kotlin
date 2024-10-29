package com.anytypeio.anytype.ui.splash

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews

@Composable
fun PulsatingCircleScreen() {
    val context = LocalContext.current

    var initialAnimationFinished by remember { mutableStateOf(false) }

    var circleSize by remember { mutableStateOf(INITIAL_CIRCLE_SIZE.dp) }

    LaunchedEffect(Unit) {
        animate(
            initialValue = INITIAL_CIRCLE_SIZE,
            targetValue = TARGET_CIRCLE_SIZE,
            animationSpec = tween(durationMillis = 1000, easing = FastOutLinearInEasing)
        ) { value, _ ->
            circleSize = value.dp
        }
        initialAnimationFinished = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = TRANSITION_LABEL)
    val animatedSize by infiniteTransition.animateValue(
        initialValue = MID_CIRCLE_SIZE.dp,
        targetValue = TARGET_CIRCLE_SIZE.dp,
        Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = TRANSITION_LABEL
    )

    val finalSize = if (initialAnimationFinished) animatedSize else circleSize
    val finalBrush =
        if (circleSize < 17.dp) SolidColor(Color(context.getColor(R.color.glyph_inactive))) else Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.0f),
                Color(context.getColor(R.color.glyph_inactive))
            )
        )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            SimpleCircleShape(
                size = finalSize,
                gradient = finalBrush
            )
        }
    }
}

@Composable
fun SimpleCircleShape(
    size: Dp,
    gradient: Brush,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent
) {
    Column(
        modifier = Modifier
            .wrapContentSize(Alignment.Center)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(brush = gradient, shape = CircleShape)
                .border(borderWidth, borderColor)
        )
    }
}

@DefaultPreviews
@Composable
fun PreviewPulsatingCircles() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        PulsatingCircleScreen()
    }
}

private const val INITIAL_CIRCLE_SIZE = 16f
private const val MID_CIRCLE_SIZE = 64f
private const val TARGET_CIRCLE_SIZE = 128f
private const val TRANSITION_LABEL = "Pulsating transition"