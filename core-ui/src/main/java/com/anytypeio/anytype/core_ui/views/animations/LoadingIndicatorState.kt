package com.anytypeio.anytype.core_ui.views.animations

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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