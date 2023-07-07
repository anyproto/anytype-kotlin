package com.anytypeio.anytype.core_ui.views.animations

import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.tween

interface AnimationSpecs {
    val duration: Int
    val delay: Int
    val initialValue: Float
    val targetValue: Float
    val animationSpec: DurationBasedAnimationSpec<Float>
}

class FadeAnimationSpecs(size: Int) : AnimationSpecs {
    private val fadeAnimationDurationMillis = 600

    override val duration = fadeAnimationDurationMillis
    override val delay = duration / size
    override val initialValue = 1f
    override val targetValue = 0.2f
    override val animationSpec = tween<Float>(durationMillis = duration)
}

enum class AnimationType(val specs: AnimationSpecs) {
    FADE_3_DOTS(FadeAnimationSpecs(3))
}
