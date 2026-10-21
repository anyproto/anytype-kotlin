package com.anytypeio.anytype.core_ui.foundation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews

/**
 * A soft halo that breathes around a rounded element to pull the eye to it once,
 * the way Material's feature-discovery pattern pulses a tap target.
 *
 * The halo is a stack of rounded-rect strokes fanning outward from the element's
 * edge, each fainter than the last, whose alpha rises and falls on an infinite
 * reverse tween. Stacked strokes rather than a blur mask so the look is identical
 * on every API level and needs no offscreen layer. Drawn outside the element's
 * bounds, so the caller must leave [spread] of room around it.
 *
 * When [visible] is false the modifier is a no-op: no animation is started, so an
 * already-dismissed hint costs nothing per frame.
 */
@Composable
fun Modifier.breathingGlow(
    visible: Boolean,
    cornerRadius: Dp,
    color: Color = colorResource(id = R.color.color_accent),
    spread: Dp = 8.dp
): Modifier {
    if (!visible) return this

    val transition = rememberInfiniteTransition(label = "breathing glow")
    val breath by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = BREATH_MILLIS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing glow alpha"
    )

    return this.drawBehind {
        val spreadPx = spread.toPx()
        val radiusPx = cornerRadius.toPx()
        // The halo fades with distance from the edge; the whole stack fades with
        // the breath. A thin, brighter line sits right on the edge so the field
        // reads as outlined even at the bottom of the breath.
        for (layer in 0 until LAYERS) {
            val fraction = (layer + 1f) / LAYERS
            val inset = spreadPx * fraction
            val alpha = MAX_EDGE_ALPHA * (1f - fraction) * (MIN_BREATH + (1f - MIN_BREATH) * breath)
            drawRoundRect(
                color = color.copy(alpha = alpha),
                topLeft = Offset(-inset, -inset),
                size = Size(size.width + inset * 2, size.height + inset * 2),
                cornerRadius = CornerRadius(radiusPx + inset),
                style = Stroke(width = spreadPx / LAYERS * 2)
            )
        }
        drawRoundRect(
            color = color.copy(alpha = OUTLINE_ALPHA * (MIN_BREATH + (1f - MIN_BREATH) * breath)),
            cornerRadius = CornerRadius(radiusPx),
            style = Stroke(width = OUTLINE_WIDTH.toPx())
        )
    }
}

private const val BREATH_MILLIS = 1100
private const val LAYERS = 6
private const val MAX_EDGE_ALPHA = 0.35f
private const val OUTLINE_ALPHA = 0.9f
private const val MIN_BREATH = 0.15f
private val OUTLINE_WIDTH = 1.5.dp

@DefaultPreviews
@Composable
private fun BreathingGlowPreview() {
    Box(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .height(40.dp)
            .breathingGlow(visible = true, cornerRadius = 10.dp)
            .background(
                color = colorResource(id = R.color.shape_transparent),
                shape = RoundedCornerShape(10.dp)
            )
    )
}
