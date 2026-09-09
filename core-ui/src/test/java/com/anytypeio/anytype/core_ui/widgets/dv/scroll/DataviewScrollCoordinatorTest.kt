package com.anytypeio.anytype.core_ui.widgets.dv.scroll

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DataviewScrollCoordinatorTest {
    private val state = DataviewScrollCoordinator().apply { setRange(200f) }

    @Test fun `crossing both boundaries conserves the complete input`() {
        state.restoreProgress(.75f)
        val session = state.begin("A")
        val pre = state.consumePreScroll(80f, session)
        val child = 80f - pre
        assertEquals(50f, pre)
        assertEquals(30f, child)
        assertEquals(200f, state.offset)
        assertEquals(0f, state.consumePreScroll(-80f, session))
        val childBack = -child
        val post = state.consumePostScroll(-80f - childBack, session)
        assertEquals(-50f, post)
        assertEquals(-80f, childBack + post)
        assertEquals(150f, state.offset)
    }

    @Test fun `deep reversal and other column positions cannot move the header`() {
        state.restoreProgress(1f)
        val a = state.begin("A")
        assertEquals(0f, state.consumePreScroll(-60f, a))
        assertEquals(0f, state.consumePostScroll(0f, a))
        assertEquals(200f, state.offset)
        val b = state.begin("B")
        assertEquals(-60f, state.consumePostScroll(-60f, b))
        assertEquals(140f, state.offset)
        assertEquals(0f, state.consumePostScroll(-60f, a))
    }

    @Test fun `excess pull is returned to the active edge`() {
        state.restoreProgress(.2f)
        val session = state.begin("A")
        val used = state.consumePostScroll(-70f, session)
        assertEquals(-40f, used)
        assertEquals(-30f, -70f - used)
        assertEquals(0f, state.offset)
    }

    @Test fun `new owner invalidates old session before invoking cancellation`() {
        lateinit var old: DataviewScrollCoordinator.Session
        var cancels = 0
        old = state.begin("A", cancel = {
            cancels++
            assertFalse(state.isCurrent(old))
            assertEquals(0f, state.consumePreScroll(100f, old))
        })
        val next = state.begin("B")
        assertEquals(1, cancels)
        state.end(old)
        assertTrue(state.isCurrent(next))
        assertEquals(10f, state.consumePreScroll(10f, next))
    }

    @Test fun `native session normalizes once to already displayed pixels`() {
        state.restoreProgress(199.75f / 200f)
        val native = state.begin("rv", integerPixels = true)
        assertEquals(200f, state.offset)
        assertEquals(0f, state.consumePreScroll(1f, native))
        assertEquals(-1f, state.consumePostScroll(-1f, native))
        val compose = state.begin("column")
        repeat(100) {
            state.consumePreScroll(.125f, compose)
            state.consumePostScroll(-.125f, compose)
        }
        assertEquals(199f, state.offset)
        assertEquals(1f, state.consumePreScroll(1f, state.begin("rv", integerPixels = true)))
    }

    @Test fun `partial state rebases only at idle geometry while endpoints remain exact`() {
        state.restoreProgress(.5f)
        state.setRange(400f)
        assertEquals(200f, state.offset)
        val session = state.begin("A")
        state.setRange(600f)
        assertEquals(200f, state.offset)
        state.end(session)
        state.setExpanded(false)
        state.setRange(300f)
        assertEquals(300f, state.offset)
        state.setExpanded(true)
        state.setRange(100f)
        assertEquals(0f, state.offset)
    }

    @Test fun `saved progress waits for measurement and zero range consumes nothing`() {
        val empty = DataviewScrollCoordinator()
        assertEquals(0f, empty.consumePreScroll(100f, empty.begin("A")))
        empty.restoreProgress(.75f)
        empty.setRange(200f)
        assertEquals(150f, empty.offset)
        empty.setRange(0f)
        assertEquals(0f, empty.offset)
        assertEquals(.75f, empty.savedProgress)
        empty.setRange(100f)
        assertEquals(75f, empty.offset)
    }

    @Test fun `new input cancels pending header restore while geometry does not`() {
        val pending = DataviewScrollCoordinator()
        pending.restoreProgress(.75f)
        val generation = pending.inputGeneration
        pending.cancel()
        assertEquals(generation, pending.inputGeneration)
        pending.beginTouch()
        assertTrue(pending.inputGeneration > generation)
        pending.setRange(200f)
        assertEquals(0f, pending.offset)
    }

    @Test fun `drag freeze rejects both existing inertia and autoscroll`() {
        val user = state.begin("A")
        state.consumePreScroll(70f, user)
        state.setBlocked(MotionOrigin.CardDrag, true)
        assertEquals(0f, state.consumePreScroll(20f, user))
        val drag = state.begin("drag", MotionOrigin.DragAutoScroll)
        assertEquals(0f, state.consumePostScroll(-70f, drag))
        state.setBlocked(MotionOrigin.CardDrag, false)
        assertEquals(0f, state.consumePostScroll(-70f, drag))
        assertEquals(70f, state.offset)
    }

    @Test fun `restoration and reveal cannot masquerade as native inertia`() {
        for (origin in listOf(MotionOrigin.Restoration, MotionOrigin.ProgrammaticReveal)) {
            assertEquals(0f, state.consumePreScroll(50f, state.begin("script", origin)))
        }
        assertEquals(50f, state.consumePreScroll(50f, state.begin("talkback", MotionOrigin.Accessibility)))
    }
}
