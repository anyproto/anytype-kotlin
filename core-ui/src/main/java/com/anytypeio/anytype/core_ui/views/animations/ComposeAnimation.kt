package com.anytypeio.anytype.core_ui.views.animations

import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.tween

interface AnimationSpecs {
    val duration: Int
    val delay: Int
    val initialValue: Float
    val targetValue: Float
    val animationSpec: DurationBasedAnimationSpec<Float>
    val itemCount: Int
}

class FadeAnimationSpecs(
    override val itemCount: Int = 3,
    fadeAnimationDurationMillis: Int = 600
) : AnimationSpecs {

    override val duration = fadeAnimationDurationMillis
    override val delay = duration / itemCount
    override val initialValue = 1f
    override val targetValue = 0.2f
    override val animationSpec = tween<Float>(durationMillis = duration)
}
