package com.anytypeio.anytype.core_ui.widgets.dv.board

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardFlingMomentumTest {
    private var time = 1_000L
    private val momentum = BoardFlingMomentum { time }

    @Test fun `cancelled animation preserves its decayed velocity for the next swipe`() {
        var frames = 0L
        val clock = object : MonotonicFrameClock {
            override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R = onFrame(++frames * 16_666_667L)
        }
        val behavior = BoardFlingBehavior(exponentialDecay(), momentum)
        val scroll = object : ScrollScope {
            override fun scrollBy(pixels: Float): Float {
                if (frames >= 4) throw CancellationException("new finger down")
                return pixels
            }
        }
        var cancelled = false
        runBlocking(clock) {
            try { with(behavior) { scroll.performFling(1000f) } }
            catch (_: CancellationException) { cancelled = true }
        }
        assertTrue(cancelled)
        momentum.down()
        momentum.up(true)
        val boosted = momentum.takeVelocity(1000f)
        assertTrue("Must add the remaining velocity, not the initial velocity: $boosted", boosted > 1000f && boosted < 2000f)
    }

    @Test fun `immediate same direction swipe adds remaining fling velocity once`() {
        momentum.interrupted(800f)
        momentum.down()
        time += 120
        momentum.up(true)
        assertEquals(1800f, momentum.takeVelocity(1000f), 0f)
        assertEquals(1000f, momentum.takeVelocity(1000f), 0f)
    }

    @Test fun `cancellation that unwinds after pointer down retains momentum`() {
        momentum.down()
        time += 16
        momentum.interrupted(-700f)
        time += 120
        momentum.up(true)
        assertEquals(-1700f, momentum.takeVelocity(-1000f), 0f)
    }

    @Test fun `reversal uses only the new swipe velocity`() {
        momentum.interrupted(800f)
        momentum.down()
        momentum.up(true)
        assertEquals(-1000f, momentum.takeVelocity(-1000f), 0f)
    }

    @Test fun `tap stops motion and cannot lend momentum to a later swipe`() {
        momentum.interrupted(800f)
        momentum.down()
        momentum.up(false)
        momentum.down()
        momentum.up(true)
        assertEquals(1000f, momentum.takeVelocity(1000f), 0f)
    }

    @Test fun `other column does not inherit interrupted velocity`() {
        momentum.interrupted(800f)
        val other = BoardFlingMomentum { time }
        other.down()
        other.up(true)
        assertEquals(1000f, other.takeVelocity(1000f), 0f)
    }

    @Test fun `stale cancellation long hold and zero release do not restart motion`() {
        momentum.interrupted(800f)
        time += 101
        momentum.down()
        momentum.up(true)
        assertEquals(1000f, momentum.takeVelocity(1000f), 0f)
        momentum.interrupted(800f)
        momentum.down()
        time += 501
        momentum.up(true)
        assertEquals(1000f, momentum.takeVelocity(1000f), 0f)
        momentum.interrupted(800f)
        momentum.down()
        momentum.up(true)
        assertEquals(0f, momentum.takeVelocity(0f), 0f)
    }
}
