package com.anytypeio.anytype.core_ui.widgets.dv.scroll

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * Direct Compose route to the shared header. Do not also attach View nested-scroll interop.
 * The native list owns its fling until onPostFling supplies the remaining velocity.
 */
class DataviewColumnScrollConnection internal constructor(
    private val coordinator: DataviewScrollCoordinator,
    private val owner: String,
    private val decay: DecayAnimationSpec<Float>,
    private val scope: CoroutineScope,
    private val stopNativeScroll: suspend () -> Unit
) : NestedScrollConnection {
    private var session: DataviewScrollCoordinator.Session? = null
    private var continuation: Job? = null
    private var pointerDown = false
    private var pointerGeneration: Long? = null
    private var suppressedOrigins = 0
    private var flinging = false

    internal fun onPointerDown() {
        pointerDown = true
        pointerGeneration = coordinator.touchGeneration ?: coordinator.generation
    }

    internal fun onPointerUp() {
        pointerDown = false
        // Keep the session alive until native and residual fling have both finished.
    }

    private fun sessionFor(source: NestedScrollSource): DataviewScrollCoordinator.Session? {
        if (suppressedOrigins > 0 || coordinator.isBlocked) return null
        if (source == NestedScrollSource.UserInput && flinging) {
            // Keyboard/accessibility/wheel input has no down event. Its native mutation has
            // already started, so stop only the old residual coroutine, not this new mutation.
            continuation?.cancel()
            session?.let(coordinator::end)
            flinging = false
        }
        session?.takeIf(coordinator::isCurrent)?.let { return it }
        // A cancelled fling must never reclaim the header. Accessibility/keyboard scrolling
        // uses UserInput in Foundation 1.8.2, so it can start without a finger-down event.
        if (source != NestedScrollSource.UserInput) return null
        if (pointerDown && pointerGeneration != coordinator.generation) return null
        return coordinator.begin(
            owner = owner,
            origin = if (pointerDown) MotionOrigin.User else MotionOrigin.Accessibility,
            cancel = {
                continuation?.cancel()
                // Start immediately: invalidate the old native mutation before another frame,
                // including a new touch that ultimately becomes horizontal navigation.
                scope.launch(start = CoroutineStart.UNDISPATCHED) { stopNativeScroll() }
            }
        ).also { session = it }
    }

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (available.y == 0f) return Offset.Zero
        val current = sessionFor(source) ?: return Offset.Zero
        val consumed = coordinator.consumePreScroll(-available.y, current)
        return if (consumed == 0f) Offset.Zero else Offset(0f, -consumed)
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (available.y == 0f) return Offset.Zero
        val current = sessionFor(source) ?: return Offset.Zero
        val accepted = coordinator.consumePostScroll(-available.y, current)
        return if (accepted == 0f) Offset.Zero else Offset(0f, -accepted)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        flinging = true
        return Velocity.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        val current = session ?: return Velocity.Zero
        if (suppressedOrigins > 0 || !coordinator.isCurrent(current)) return Velocity.Zero
        try {
            if (!available.y.isFinite() || abs(available.y) <= 1f ||
                !coordinator.canConsume(-available.y, current)
            ) return Velocity.Zero

            return coroutineScope {
                val job = currentCoroutineContext().job
                continuation = job
                var remaining = available.y
                var lastValue = 0f
                var moved = false
                // Match Foundation's native scroll timebase even when system animator scale
                // is zero. This applies to inertia, not explicit UI expand/collapse commands.
                withContext(NativeScrollDurationScale) {
                    AnimationState(initialValue = 0f, initialVelocity = available.y)
                        .animateDecay(decay) {
                            job.ensureActive()
                            if (!coordinator.isCurrent(current)) {
                                cancelAnimation()
                                return@animateDecay
                            }
                            val delta = value - lastValue
                            lastValue = value
                            val accepted = if (delta < 0f) {
                                -coordinator.consumePreScroll(-delta, current)
                            } else {
                                -coordinator.consumePostScroll(-delta, current)
                            }
                            moved = moved || accepted != 0f
                            remaining = velocity
                            // A clipped frame retains this frame's remaining velocity for
                            // the originating list's edge effect. No other column is flung.
                            if (abs(delta - accepted) > 0.001f ||
                                !coordinator.canConsume(-available.y, current)
                            ) cancelAnimation()
                        }
                }
                if (moved) Velocity(0f, available.y - remaining) else Velocity.Zero
            }
        } finally {
            // Cancellation propagates through the owning suspend operation. A stale finally
            // cannot end a newer gesture, nor fabricate full velocity consumption.
            if (session == current) {
                continuation = null
                flinging = false
            }
            coordinator.end(current)
        }
    }

    /** Scope content restoration/reveal/autoscroll explicitly; SideEffect is also inertia. */
    suspend fun <T> withOrigin(origin: MotionOrigin, block: suspend () -> T): T {
        val suppress = origin != MotionOrigin.User && origin != MotionOrigin.Inertia &&
            origin != MotionOrigin.Accessibility
        if (!suppress) return block()
        suppressedOrigins++
        session?.let(coordinator::cancel)
        return try { block() } finally { suppressedOrigins-- }
    }

    fun dispose() {
        session?.let(coordinator::cancel)
        continuation?.cancel()
        session = null
        pointerGeneration = null
    }
}

internal object NativeScrollDurationScale : MotionDurationScale {
    override val scaleFactor: Float = 1f
}

@Composable
fun rememberDataviewColumnScrollConnection(
    coordinator: DataviewScrollCoordinator,
    viewerId: String,
    columnId: String?,
    scrollableState: ScrollableState
): DataviewColumnScrollConnection {
    val decay = rememberSplineBasedDecay<Float>()
    val scope = rememberCoroutineScope()
    val connection = remember(coordinator, viewerId, columnId, scrollableState, decay, scope) {
        DataviewColumnScrollConnection(
            coordinator = coordinator,
            owner = "compose:$viewerId:${columnId ?: "background"}",
            decay = decay,
            scope = scope,
            // Cancellation of an old fling must never outrank the incoming finger drag.
            stopNativeScroll = { scrollableState.stopScroll(MutatePriority.Default) }
        )
    }
    DisposableEffect(connection) { onDispose(connection::dispose) }
    return connection
}

/** Observe the first board touch before child detectors, without consuming any pointer. */
fun Modifier.dataviewBoardTouchObserver(
    coordinator: DataviewScrollCoordinator,
    onTouch: () -> Unit = {}
): Modifier = pointerInput(coordinator) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        coordinator.beginTouch()
        onTouch()
        try {
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
            } while (event.changes.any { it.pressed })
        } finally {
            coordinator.endTouch()
        }
    }
}

/** Captures the board's touch generation so simultaneous columns cannot steal ownership. */
fun Modifier.dataviewColumnTouchObserver(
    connection: DataviewColumnScrollConnection
): Modifier = pointerInput(connection) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        connection.onPointerDown()
        try {
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
            } while (event.changes.any { it.pressed })
        } finally {
            connection.onPointerUp()
        }
    }
}

/** Use only on a gutter/no-column region, never on an ancestor covering native column lists. */
@Composable
fun Modifier.dataviewHeaderScrollEmitter(
    coordinator: DataviewScrollCoordinator,
    viewerId: String,
    enabled: Boolean = true
): Modifier {
    val state = rememberScrollableState { 0f }
    val connection = rememberDataviewColumnScrollConnection(coordinator, viewerId, null, state)
    return dataviewColumnTouchObserver(connection)
        .nestedScroll(connection)
        .scrollable(state, Orientation.Vertical, enabled = enabled)
}
