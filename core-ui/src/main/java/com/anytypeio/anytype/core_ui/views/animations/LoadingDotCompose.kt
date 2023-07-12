package com.anytypeio.anytype.core_ui.views.animations

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.views.ButtonSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
fun DotsLoadingIndicator(
    animating: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    animationSpecs: AnimationSpecs,
    size: ButtonSize
) {
    val (dotSize, dotSpacing) = when (size) {
        ButtonSize.XSmall,
        ButtonSize.XSmallSecondary -> 6.dp to 2.dp
        ButtonSize.Small,
        ButtonSize.SmallSecondary -> 6.dp to 4.dp
        ButtonSize.Medium,
        ButtonSize.MediumSecondary -> 6.dp to 6.dp
        ButtonSize.Large,
        ButtonSize.LargeSecondary -> 6.dp to 6.dp
    }
    val state = rememberLoadingIndicatorState(animating, animationSpecs)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(animationSpecs.itemCount) { index ->
            LoadingDot(
                modifier = Modifier
                    .padding(horizontal = dotSpacing)
                    .width(dotSize)
                    .aspectRatio(1f)
                    .then(Modifier.graphicsLayer { alpha = state[index] }),
                color = color
            )
        }
    }
}

@Composable
private fun LoadingDot(
    color: Color,
    modifier: Modifier = Modifier,
    delay: Long = 300L
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
            radius = size.minDimension / 2
        )
    }
}

@Stable
interface LoadingIndicatorState {
    operator fun get(index: Int): Float
    fun start(animationSpecs: AnimationSpecs, scope: CoroutineScope)
}

class LoadingIndicatorStateImpl(private val itemsCount: Int) : LoadingIndicatorState {
    private val animatedValues = List(itemsCount) { mutableStateOf(0f) }

    override fun get(index: Int): Float = animatedValues[index].value

    override fun start(animationSpecs: AnimationSpecs, scope: CoroutineScope) {
        repeat(itemsCount) { index ->
            scope.launch {
                animate(
                    initialValue = animationSpecs.initialValue,
                    targetValue = animationSpecs.targetValue,
                    animationSpec = infiniteRepeatable(
                        animation = animationSpecs.animationSpec,
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(animationSpecs.delay * index)
                    ),
                ) { value, _ -> animatedValues[index].value = value }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoadingIndicatorStateImpl

        if (animatedValues != other.animatedValues) return false

        return true
    }

    override fun hashCode(): Int {
        return animatedValues.hashCode()
    }
}

