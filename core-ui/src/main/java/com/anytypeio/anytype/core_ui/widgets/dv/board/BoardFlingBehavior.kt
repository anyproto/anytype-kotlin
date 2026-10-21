package com.anytypeio.anytype.core_ui.widgets.dv.board

import android.os.SystemClock
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.NativeScrollDurationScale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sign

/** Native spline decay with a flywheel for consecutive column swipes. */
internal class BoardFlingBehavior(
    private val decay: DecayAnimationSpec<Float>,
    internal val momentum: BoardFlingMomentum = BoardFlingMomentum()
) : FlingBehavior {
    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        val velocity = momentum.takeVelocity(initialVelocity)
        if (abs(velocity) <= 1f) return initialVelocity
        val animation = AnimationState(initialValue = 0f, initialVelocity = velocity)
        var previous = 0f
        try {
            withContext(NativeScrollDurationScale) {
                animation.animateDecay(decay) {
                    val delta = value - previous
                    previous = value
                    val consumed = scrollBy(delta)
                    if (abs(delta - consumed) > .5f) cancelAnimation()
                }
            }
        } catch (cancelled: CancellationException) {
            // Capture the instantaneous velocity before the old mutation unwinds.
            momentum.interrupted(animation.velocity)
            throw cancelled
        }
        // Added momentum must not manufacture negative consumption in nested-scroll handoff.
        return animation.velocity.coerceIn(-abs(initialVelocity), abs(initialVelocity))
    }
}

internal class BoardFlingMomentum(private val now: () -> Long = SystemClock::uptimeMillis) {
    private var interruptedVelocity = 0f
    private var interruptedAt = Long.MIN_VALUE
    private var gestureStartedAt = Long.MIN_VALUE
    private var dragging = false
    private var carry = 0f
    private var released = false

    fun interrupted(velocity: Float) {
        if (!velocity.isFinite()) return
        if (dragging && now() - gestureStartedAt in 0..100) {
            carry = velocity
        } else {
            interruptedVelocity = velocity
            interruptedAt = now()
        }
    }

    fun down() {
        gestureStartedAt = now()
        carry = if (gestureStartedAt - interruptedAt in 0..100) interruptedVelocity else 0f
        interruptedVelocity = 0f
        dragging = true
        released = false
    }

    fun up(swiped: Boolean) {
        dragging = false
        released = swiped
        if (!swiped) carry = 0f
    }

    fun takeVelocity(velocity: Float): Float {
        val extra = if (released && now() - gestureStartedAt in 0..500 &&
            abs(velocity) > 1f && sign(velocity) == sign(carry)
        ) carry else 0f
        carry = 0f
        released = false
        interruptedVelocity = 0f
        return velocity + extra
    }
}

internal fun Modifier.boardFlingTouchObserver(
    behavior: BoardFlingBehavior,
    orientation: Orientation = Orientation.Vertical
): Modifier =
    pointerInput(behavior, orientation) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            behavior.momentum.down()
            var swiped = false
            var released = false
            try {
                do {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    event.changes.firstOrNull { it.id == down.id }?.let {
                        val travel = it.position - down.position
                        val along = if (orientation == Orientation.Vertical) travel.y else travel.x
                        val across = if (orientation == Orientation.Vertical) travel.x else travel.y
                        if (abs(along) > viewConfiguration.touchSlop && abs(along) > abs(across)) {
                            swiped = true
                        }
                    }
                    released = event.changes.none { it.pressed }
                } while (!released)
            } finally {
                behavior.momentum.up(released && swiped)
            }
        }
    }
