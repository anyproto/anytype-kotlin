package com.agileburo.anytype.core_ui.features.page.scrollandmove

import androidx.annotation.FloatRange

/**
 * @property [position] position, with with target is associated
 * @property [ratio] ratio describes how the view is targeted (above, below, middle).
 * @see ScrollAndMoveTargetDescriptor.END_RANGE
 * @see ScrollAndMoveTargetDescriptor.START_RANGE
 */
data class ScrollAndMoveTarget(
    @FloatRange(from = 0.0, to = 1.0) val ratio: Float,
    val position: Int
)