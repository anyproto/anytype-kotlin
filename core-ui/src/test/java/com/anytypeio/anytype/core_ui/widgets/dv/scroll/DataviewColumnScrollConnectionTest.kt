package com.anytypeio.anytype.core_ui.widgets.dv.scroll

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataviewColumnScrollConnectionTest {
    private val coordinator = DataviewScrollCoordinator().apply { setRange(200f) }

    private fun connection(scope: CoroutineScope, owner: String = "a", stop: suspend () -> Unit = {}) =
        DataviewColumnScrollConnection(coordinator, owner, exponentialDecay(), scope, stop)

    @Test
    fun `pre collapses and post expands only residual without consuming x`() = runBlocking {
        coordinator.restoreProgress(0.75f)
        val connection = connection(this)
        val pre = connection.onPreScroll(Offset(-10f, -80f), NestedScrollSource.UserInput)
        assertEquals(Offset(0f, -50f), pre)
        assertEquals(Offset.Zero, connection.onPreScroll(Offset(1f, 80f), NestedScrollSource.UserInput))
        val post = connection.onPostScroll(Offset(0f, 30f), Offset(1f, 50f), NestedScrollSource.UserInput)
        assertEquals(Offset(0f, 50f), post)
        assertEquals(150f, coordinator.offset, 0f)
    }

    @Test
    fun `new touch invalidates old fling before next frame`() = runBlocking {
        var stopped = false
        val old = connection(this, stop = { stopped = true })
        old.onPreScroll(Offset(0f, -20f), NestedScrollSource.UserInput)
        coordinator.cancel()
        assertTrue(stopped)
        assertEquals(Offset.Zero, old.onPreScroll(Offset(0f, -20f), NestedScrollSource.SideEffect))
        assertEquals(Velocity.Zero, old.onPostFling(Velocity.Zero, Velocity(0f, -1000f)))
        assertEquals(20f, coordinator.offset, 0f)
    }

    @Test
    fun `simultaneous pointer from other column cannot take an established gesture`() = runBlocking {
        val a = connection(this, "a")
        val b = connection(this, "b")
        coordinator.beginTouch()
        a.onPointerDown()
        a.onPreScroll(Offset(0f, -20f), NestedScrollSource.UserInput)
        // A second finger arrives after the first column has already claimed the gesture.
        b.onPointerDown()
        assertEquals(Offset.Zero, b.onPreScroll(Offset(0f, -20f), NestedScrollSource.UserInput))
        assertEquals(20f, coordinator.offset, 0f)
    }

    @Test
    fun `accessibility input can begin without pointer down and SideEffect alone cannot`() = runBlocking {
        val connection = connection(this)
        assertEquals(Offset.Zero, connection.onPreScroll(Offset(0f, -20f), NestedScrollSource.SideEffect))
        assertEquals(Offset(0f, -20f), connection.onPreScroll(Offset(0f, -20f), NestedScrollSource.UserInput))
        assertEquals(Offset(0f, -10f), connection.onPreScroll(Offset(0f, -10f), NestedScrollSource.SideEffect))
    }

    @Test
    fun `programmatic and card autoscroll do not acquire or change header`() = runBlocking {
        val connection = connection(this)
        connection.withOrigin(MotionOrigin.Restoration) {
            assertEquals(Offset.Zero, connection.onPreScroll(Offset(0f, -20f), NestedScrollSource.UserInput))
        }
        connection.withOrigin(MotionOrigin.DragAutoScroll) {
            assertEquals(Offset.Zero, connection.onPreScroll(Offset(0f, -20f), NestedScrollSource.SideEffect))
        }
        assertEquals(0f, coordinator.offset, 0f)
    }

    @Test
    fun `residual continuation uses available velocity and consumes only y through bound`() {
        val clock = FrameClock()
        runBlocking(clock) {
            coordinator.setRange(40f)
            val connection = connection(this)
            connection.onPreScroll(Offset(0f, -1f), NestedScrollSource.UserInput)
            assertEquals(Velocity.Zero, connection.onPreFling(Velocity(20f, -9000f)))
            val result = connection.onPostFling(Velocity(20f, -9000f), Velocity(50f, -1800f))
            assertEquals(40f, coordinator.offset, 0f)
            assertEquals(0f, result.x, 0f)
            assertTrue(result.y < 0f)
            assertTrue(result.y > -1800f)
            assertTrue(clock.frames > 1)
        }
    }

    @Test
    fun `residual expansion keeps a natural partial position with animator scale zero`() {
        val clock = FrameClock()
        val disabledAnimations = object : MotionDurationScale { override val scaleFactor = 0f }
        runBlocking(clock + disabledAnimations) {
            coordinator.restoreProgress(0.5f)
            val connection = connection(this)
            connection.onPreScroll(Offset(0f, 1f), NestedScrollSource.UserInput)
            val result = connection.onPostFling(Velocity.Zero, Velocity(0f, 100f))
            assertTrue(coordinator.offset in 1f..99f)
            assertTrue(result.y in 1f..100f)
            assertTrue(clock.frames > 2)
            assertTrue(clock.scales.all { it == 1f })
        }
    }

    @Test
    fun `cancelled residual unwinds and cannot clear new owner`() {
        val clock = FrameClock()
        runBlocking(clock) {
            val old = connection(this)
            old.onPreScroll(Offset(0f, -1f), NestedScrollSource.UserInput)
            var next: DataviewScrollCoordinator.Session? = null
            clock.onFrame = { frame ->
                if (frame == 2) next = coordinator.begin("next")
            }
            var cancelled = false
            try {
                old.onPostFling(Velocity.Zero, Velocity(0f, -1800f))
            } catch (_: CancellationException) {
                cancelled = true
            }
            assertTrue(cancelled)
            assertTrue(coordinator.isCurrent(requireNotNull(next)))
            assertTrue(coordinator.offset < 200f)
            assertFalse(coordinator.isBlocked)
        }
    }

    @Test
    fun `new accessibility input on same column cancels residual without stopping new native input`() {
        val clock = FrameClock()
        runBlocking(clock) {
            var nativeStops = 0
            val connection = connection(this, stop = { nativeStops++ })
            connection.onPreScroll(Offset(0f, -1f), NestedScrollSource.UserInput)
            connection.onPreFling(Velocity(0f, -1800f))
            clock.onFrame = { frame ->
                if (frame == 2) {
                    connection.onPreScroll(Offset(0f, -10f), NestedScrollSource.UserInput)
                }
            }
            var cancelled = false
            try {
                connection.onPostFling(Velocity.Zero, Velocity(0f, -1800f))
            } catch (_: CancellationException) {
                cancelled = true
            }
            assertTrue(cancelled)
            assertEquals(11f, coordinator.offset, 0f)
            assertEquals(0, nativeStops)
            assertEquals(Offset(0f, -10f), connection.onPreScroll(Offset(0f, -10f), NestedScrollSource.UserInput))
        }
    }

    private class FrameClock : MonotonicFrameClock {
        var frames = 0
        val scales = mutableListOf<Float?>()
        var onFrame: (Int) -> Unit = {}

        override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
            frames++
            scales += currentCoroutineContext()[MotionDurationScale]?.scaleFactor
            this.onFrame(frames)
            return onFrame(frames * 16_666_667L)
        }
    }
}
