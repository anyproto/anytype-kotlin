package com.anytypeio.anytype.core_ui.views.animations

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max

// The animation comprises of 5 rotations around the circle forming a 5 pointed star.
// After the 5th rotation, we are back at the beginning of the circle.
private const val RotationsPerCycle = 5

// Each rotation is 1 and 1/3 seconds, but 1332ms divides more evenly
private const val RotationDuration = 1332

// When the rotation is at its beginning (0 or 360 degrees) we want it to be drawn at 12 o clock,
// which means 270 degrees when drawing.
private const val StartAngleOffset = -90f

// How far the base point moves around the circle
private const val BaseRotationAngle = 286f

// How far the head and tail should jump forward during one rotation past the base point
private const val JumpRotationAngle = 290f

// Each rotation we want to offset the start position by this much, so we continue where
// the previous rotation ended. This is the maximum angle covered during one rotation.
private const val RotationAngleOffset = (BaseRotationAngle + JumpRotationAngle) % 360f

// The head animates for the first half of a rotation, then is static for the second half
// The tail is static for the first half and then animates for the second half
private const val HeadAndTailAnimationDuration = (RotationDuration * 0.5).toInt()
private const val HeadAndTailDelayDuration = HeadAndTailAnimationDuration

// The easing for the head and tail jump
private val CircularEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

// CircularProgressIndicator Material specs
// Diameter of the indicator circle
private val CircularIndicatorDiameter = 40.dp

@Composable
fun LoadingIndicator(
    containerModifier: Modifier = Modifier,
    containerSize: Dp,
    colorStart: Color = colorResource(id = R.color.glyph_active),
    colorEnd: Color = Color.Transparent,
    withCircleBackground: Boolean = true
) {
    val strokeWidth = (0.0611 * containerSize.value + 0.2648).dp

    val stroke = with(LocalDensity.current) {
        Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Square)
    }

    val transition = rememberInfiniteTransition()
    // The current rotation around the circle, so we know where to start the rotation from
    val currentRotation by transition.animateValue(
        0,
        RotationsPerCycle,
        Int.VectorConverter,
        infiniteRepeatable(
            animation = tween(
                durationMillis = RotationDuration * RotationsPerCycle,
                easing = LinearEasing
            )
        )
    )
    // How far forward (degrees) the base point should be from the start point
    val baseRotation by transition.animateFloat(
        0f,
        BaseRotationAngle,
        infiniteRepeatable(
            animation = tween(
                durationMillis = RotationDuration,
                easing = LinearEasing
            )
        )
    )
    // How far forward (degrees) both the head and tail should be from the base point
    val endAngle by transition.animateFloat(
        0f,
        JumpRotationAngle,
        infiniteRepeatable(
            animation = keyframes {
                durationMillis = HeadAndTailAnimationDuration + HeadAndTailDelayDuration
                0f at 0 using CircularEasing
                JumpRotationAngle at HeadAndTailAnimationDuration
            }
        )
    )

    val startAngle by transition.animateFloat(
        0f,
        JumpRotationAngle,
        infiniteRepeatable(
            animation = keyframes {
                durationMillis = HeadAndTailAnimationDuration + HeadAndTailDelayDuration
                0f at HeadAndTailDelayDuration using CircularEasing
                JumpRotationAngle at durationMillis
            }
        )
    )

    val circleSize = 0.4889 * containerSize.value + 2.14

    val calculatedRadius = calculateRadius(containerSize)

    Box(
        modifier = containerModifier
            .size(containerSize)
            .conditionalBackground(
                condition = containerSize > 20.dp
            ) {
                val shape = if (withCircleBackground) CircleShape else RoundedCornerShape(calculatedRadius)
                background(
                    color = colorResource(R.color.shape_tertiary),
                    shape = shape
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            Modifier
                .size(circleSize.dp)
                .progressSemantics()
                .size(CircularIndicatorDiameter)
        ) {
            val currentRotationAngleOffset = (currentRotation * RotationAngleOffset) % 360f

            // How long a line to draw using the start angle as a reference point
            val sweep = abs(endAngle - startAngle)

            // Offset by the constant offset and the per rotation offset
            val offset = StartAngleOffset + currentRotationAngleOffset + baseRotation
            drawIndeterminateCircularIndicator(
                startAngle + offset,
                strokeWidth,
                sweep,
                colorStart,
                colorEnd,
                stroke
            )
        }
    }
}

private fun DrawScope.drawCircularIndicator(
    startAngle: Float,
    sweep: Float,
    colorStart: Color,
    colorEnd: Color,
    stroke: Stroke
) {
    // To draw this circle we need a rect with edges that line up with the midpoint of the stroke.
    // To do this we need to remove half the stroke width from the total diameter for both sides.
    val diameterOffset = stroke.width / 2
    val arcDimen = size.width - 2 * diameterOffset
    drawArc(
        brush = Brush.linearGradient(listOf(colorStart, colorEnd)),
        startAngle = startAngle,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(diameterOffset, diameterOffset),
        size = Size(arcDimen, arcDimen),
        style = stroke
    )
}

private fun DrawScope.drawIndeterminateCircularIndicator(
    startAngle: Float,
    strokeWidth: Dp,
    sweep: Float,
    colorStart: Color,
    colorEnd: Color,
    stroke: Stroke
) {
    val strokeCapOffset = if (stroke.cap == StrokeCap.Butt) {
        0f
    } else {
        // Length of arc is angle * radius
        // Angle (radians) is length / radius
        // The length should be the same as the stroke width for calculating the min angle
        (180.0 / PI).toFloat() * (strokeWidth / (CircularIndicatorDiameter / 2)) / 2f
    }

    // Adding a stroke cap draws half the stroke width behind the start point, so we want to
    // move it forward by that amount so the arc visually appears in the correct place
    val adjustedStartAngle = startAngle + strokeCapOffset

    // When the start and end angles are in the same place, we still want to draw a small sweep, so
    // the stroke caps get added on both ends and we draw the correct minimum length arc
    val adjustedSweep = max(sweep, 0.1f)

    drawCircularIndicator(adjustedStartAngle, adjustedSweep, colorStart, colorEnd, stroke)
}

@Composable
fun Modifier.conditionalBackground(
    condition: Boolean,
    modifier: @Composable Modifier.() -> Modifier
): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

@Composable
fun calculateRadius(boxSize: Dp): Dp {
    return when {
        // For sizes below or equal to 20dp, we assume 4dp as the minimum.
        boxSize <= 20.dp -> 4.dp
        boxSize <= 40.dp -> {
            // Linear interpolation from 20dp (4dp) to 40dp (6dp)
            // radius = 0.1 * boxSize + 2
            (0.1f * boxSize.value + 2f).dp
        }
        boxSize <= 48.dp -> {
            // Linear interpolation from 40dp (6dp) to 48dp (8dp)
            // radius = 0.25 * boxSize - 4
            (0.25f * boxSize.value - 4f).dp
        }
        boxSize <= 64.dp -> {
            // Constant radius of 8dp for these sizes
            8.dp
        }
        boxSize <= 80.dp -> {
            // Linear interpolation from 64dp (8dp) to 80dp (12dp)
            // radius = 0.25 * boxSize - 8
            (0.25f * boxSize.value - 8f).dp
        }
        else -> {
            // For sizes greater than 80dp (including 96dp and 112dp), use 12dp.
            12.dp
        }
    }
}