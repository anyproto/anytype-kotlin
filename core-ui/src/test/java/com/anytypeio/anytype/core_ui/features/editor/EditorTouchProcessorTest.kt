package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import android.os.Looper
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class EditorTouchProcessorTest {

    private lateinit var processor: EditorTouchProcessor
    private lateinit var view: View

    private var longClickCount = 0
    private var dndTriggerCount = 0
    private var fallbackCount = 0

    private val touchSlop = 10

    @Before
    fun setup() {
        longClickCount = 0
        dndTriggerCount = 0
        fallbackCount = 0

        processor = EditorTouchProcessor(
            touchSlop = touchSlop,
            fallback = { fallbackCount++; false },
            onLongClick = { longClickCount++ },
            onDragAndDropTrigger = { dndTriggerCount++ }
        )

        view = View(ApplicationProvider.getApplicationContext()).apply {
            setOnLongClickListener { true }
        }
    }

    // -- Helpers --

    private fun downEvent(y: Float = 100f): MotionEvent {
        val time = SystemClock.uptimeMillis()
        return MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, 0f, y, 0)
    }

    private fun moveEvent(y: Float): MotionEvent {
        val time = SystemClock.uptimeMillis()
        return MotionEvent.obtain(time, time, MotionEvent.ACTION_MOVE, 0f, y, 0)
    }

    private fun upEvent(y: Float = 100f): MotionEvent {
        val time = SystemClock.uptimeMillis()
        return MotionEvent.obtain(time, time, MotionEvent.ACTION_UP, 0f, y, 0)
    }

    private fun advanceTime(millis: Long) {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(millis))
    }

    // -- Tests --

    @Test
    fun `hold still for 500ms then release triggers selection`() {
        processor.process(view, downEvent())

        advanceTime(EditorTouchProcessor.LONG_PRESS_TIMEOUT)

        processor.process(view, upEvent())

        assertEquals(1, longClickCount, "onLongClick should have been called")
        assertEquals(0, dndTriggerCount, "onDragAndDropTrigger should not have been called")
    }

    @Test
    fun `hold still for more than DND_TIMEOUT then release triggers selection not drag`() {
        processor.process(view, downEvent())

        // Advance past DND_TIMEOUT — the timeout runnable fires but should NOT trigger DnD
        advanceTime(EditorTouchProcessor.DND_TIMEOUT + 100)

        processor.process(view, upEvent())

        assertEquals(1, longClickCount, "onLongClick should have been called")
        assertEquals(0, dndTriggerCount, "onDragAndDropTrigger should not have been called")
    }

    @Test
    fun `micro-trembling within drag slop treated as still hold - triggers selection`() {
        val dragSlop = touchSlop * 2
        val microMovement = (dragSlop - 1).toFloat()

        processor.process(view, downEvent(y = 100f))

        // Simulate small finger movements within drag slop
        advanceTime(100)
        processor.process(view, moveEvent(y = 100f + microMovement / 2))
        advanceTime(100)
        processor.process(view, moveEvent(y = 100f + microMovement))

        // Wait past long press timeout
        advanceTime(EditorTouchProcessor.DND_TIMEOUT)

        processor.process(view, upEvent(y = 100f + microMovement))

        assertEquals(1, longClickCount, "onLongClick should have been called")
        assertEquals(0, dndTriggerCount, "Micro-trembling should not trigger drag")
    }

    @Test
    fun `deliberate movement beyond drag slop after long press triggers drag`() {
        val dragSlop = touchSlop * 2
        val bigMovement = (dragSlop + 5).toFloat()

        processor.process(view, downEvent(y = 100f))

        // Wait past long press timeout
        advanceTime(EditorTouchProcessor.LONG_PRESS_TIMEOUT + 100)

        // First move to register baseline
        processor.process(view, moveEvent(y = 100f))
        // Big move beyond drag slop
        processor.process(view, moveEvent(y = 100f + bigMovement))

        assertEquals(1, dndTriggerCount, "onDragAndDropTrigger should have been called")
        assertEquals(0, longClickCount, "onLongClick should not have been called")
    }

    @Test
    fun `movement beyond drag slop before long press timeout - drag starts at timeout`() {
        val dragSlop = touchSlop * 2
        val bigMovement = (dragSlop + 5).toFloat()

        processor.process(view, downEvent(y = 100f))

        // Move beyond threshold quickly (before LONG_PRESS_TIMEOUT)
        advanceTime(100)
        processor.process(view, moveEvent(y = 100f))
        processor.process(view, moveEvent(y = 100f + bigMovement))

        // DnD should not have triggered yet (long press not elapsed)
        assertEquals(0, dndTriggerCount, "DnD should not trigger before long press timeout")

        // Now let the DND_TIMEOUT fire
        advanceTime(EditorTouchProcessor.DND_TIMEOUT)

        assertEquals(1, dndTriggerCount, "DnD should trigger when timeout fires with isDragging=true")
        assertEquals(0, longClickCount, "onLongClick should not have been called")
    }

    @Test
    fun `quick tap does not trigger selection or drag`() {
        processor.process(view, downEvent())

        // Release quickly (before LONG_PRESS_TIMEOUT)
        advanceTime(100)

        processor.process(view, upEvent())

        assertEquals(0, longClickCount, "Quick tap should not trigger selection")
        assertEquals(0, dndTriggerCount, "Quick tap should not trigger drag")
        assertTrue(fallbackCount > 0, "Quick tap should fall through to fallback")
    }

    @Test
    fun `drag is not double-triggered by both timeout and ACTION_MOVE`() {
        val dragSlop = touchSlop * 2
        val bigMovement = (dragSlop + 5).toFloat()

        processor.process(view, downEvent(y = 100f))

        // Move beyond threshold early
        advanceTime(100)
        processor.process(view, moveEvent(y = 100f))
        processor.process(view, moveEvent(y = 100f + bigMovement))

        // Let timeout fire (isDragging is true, so timeout also triggers DnD)
        advanceTime(EditorTouchProcessor.DND_TIMEOUT)

        // Only one DnD trigger should have happened total
        assertEquals(1, dndTriggerCount, "DnD should only be triggered once")
    }

    @Test
    fun `hold still past DND_TIMEOUT then move triggers drag immediately`() {
        val dragSlop = touchSlop * 2
        val bigMovement = (dragSlop + 5).toFloat()

        processor.process(view, downEvent(y = 100f))

        // Hold still past DND_TIMEOUT
        advanceTime(EditorTouchProcessor.DND_TIMEOUT + 100)

        assertEquals(0, dndTriggerCount, "No DnD yet — finger hasn't moved")

        // Now start moving
        processor.process(view, moveEvent(y = 100f))
        processor.process(view, moveEvent(y = 100f + bigMovement))

        assertEquals(1, dndTriggerCount, "DnD should trigger immediately on movement")
        assertEquals(0, longClickCount, "onLongClick should not have been called")
    }
}
