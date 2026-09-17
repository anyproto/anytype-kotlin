package com.anytypeio.anytype.features.sets.dv

import android.os.SystemClock
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.anytypeio.anytype.R
import com.anytypeio.anytype.features.sets.dv.DataviewComposeHeaderHarnessFragment.Companion.BACKGROUND
import com.anytypeio.anytype.features.sets.dv.DataviewComposeHeaderHarnessFragment.Companion.CHIP
import com.anytypeio.anytype.features.sets.dv.DataviewComposeHeaderHarnessFragment.Companion.LIST
import com.anytypeio.anytype.features.sets.dv.DataviewComposeHeaderHarnessFragment.Companion.SELECTABLE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A Compose object header that leaves the rows a 17dp strip, as a phone shows in landscape.
 * The header itself must accept the collapse drag, while Compose keeps every gesture that
 * one of its own handlers claims. Real pointer injection, so Compose's own arbitration runs.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class DataviewComposeHeaderHarnessTest {
    @Test
    fun dragOnTheHeaderBackgroundCollapsesItWhenRowsHaveNoRoom() = withHarness { scenario ->
        var strip = 0
        scenario.onFragment { fixture ->
            strip = fixture.rowsStripPx
            assertTrue(fixture.host.getChildAt(2).height <= strip + 1)
            assertEquals(0f, fixture.host.coordinator.offset, 0f)
        }
        drag(scenario, BACKGROUND, dy = -dp(160))
        await(scenario) { it.host.coordinator.offset > 0f }
        SystemClock.sleep(600)
        scenario.onFragment { fixture ->
            // Every delivered pixel past slop collapsed the header; the release only adds a fling.
            assertTrue(describe(fixture), fixture.host.coordinator.offset >= deliveredTravel(fixture) - slop - 1)
            assertTrue(describe(fixture), fixture.host.getChildAt(2).height > strip)
            // The empty background hit no Compose handler, so the host owned the stream at once.
            assertEquals(describe(fixture), listOf(MotionEvent.ACTION_DOWN), fixture.headerActions)
        }
    }

    @Test
    fun verticalDragOnAChipCollapsesTheHeaderWithoutAClick() = withHarness { scenario ->
        drag(scenario, CHIP, dy = -dp(160))
        await(scenario) { it.host.coordinator.offset > 0f }
        SystemClock.sleep(600)
        scenario.onFragment { fixture ->
            assertEquals(describe(fixture), 0, fixture.chipClicks)
            assertTrue(describe(fixture), fixture.host.coordinator.offset >= deliveredTravel(fixture) - slop - 1)
            // The chip saw the move first, did not ask to keep it, and was cancelled in the same event.
            assertEquals(describe(fixture), listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL), fixture.headerActions)
        }
    }

    @Test
    fun tapOnAChipClicksWithoutMovingTheHeader() = withHarness { scenario ->
        val (x, y) = screenPoint(scenario, CHIP)
        val downTime = SystemClock.uptimeMillis()
        inject(MotionEvent.ACTION_DOWN, downTime, downTime, x, y)
        SystemClock.sleep(50)
        inject(MotionEvent.ACTION_UP, downTime, SystemClock.uptimeMillis(), x, y)
        await(scenario) { it.chipClicks == 1 }
        scenario.onFragment { fixture ->
            assertEquals(0f, fixture.host.coordinator.offset, 0f)
            assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP), fixture.headerActions)
        }
    }

    @Test
    fun horizontalDragOnTheChipRowScrollsTheRowAndKeepsTheHeader() = withHarness { scenario ->
        drag(scenario, CHIP, dx = -dp(120))
        await(scenario) { (it.chipScroll?.value ?: 0) > 0 }
        scenario.onFragment { fixture ->
            assertEquals(0f, fixture.host.coordinator.offset, 0f)
            assertTrue(fixture.disallowRequests > 0)
            assertFalse(fixture.headerActions.contains(MotionEvent.ACTION_CANCEL))
            assertEquals(MotionEvent.ACTION_UP, fixture.headerActions.last())
        }
    }

    @Test
    fun verticalDragOnAComposeListScrollsTheListAndKeepsTheHeader() = withHarness { scenario ->
        drag(scenario, LIST, dy = -dp(80))
        await(scenario) {
            val state = it.listState
            state != null && (state.firstVisibleItemIndex > 0 || state.firstVisibleItemScrollOffset > 0)
        }
        scenario.onFragment { fixture ->
            // Both crossed slop on the same move. The list saw it first and asked to keep it.
            assertEquals(0f, fixture.host.coordinator.offset, 0f)
            assertTrue(fixture.disallowRequests > 0)
            assertFalse(fixture.headerActions.contains(MotionEvent.ACTION_CANCEL))
        }
    }

    @Test
    fun longPressDragOnSelectableTextKeepsTheSelectionGesture() = withHarness { scenario ->
        drag(
            scenario, SELECTABLE, dy = -dp(60),
            pressMs = ViewConfiguration.getLongPressTimeout() + 300L
        )
        SystemClock.sleep(300)
        scenario.onFragment { fixture ->
            assertEquals(0f, fixture.host.coordinator.offset, 0f)
            assertTrue(fixture.disallowRequests > 0)
            assertFalse(fixture.headerActions.contains(MotionEvent.ACTION_CANCEL))
            assertEquals(MotionEvent.ACTION_UP, fixture.headerActions.last())
        }
    }

    @Test
    fun dragDownOnAPartlyCollapsedHeaderExpandsIt() = withHarness { scenario ->
        drag(scenario, BACKGROUND, dy = -dp(120), holdMs = 300)
        await(scenario) { it.host.coordinator.offset > 0f }
        SystemClock.sleep(300)
        var held = 0f
        scenario.onFragment { fixture ->
            held = fixture.host.coordinator.offset
            assertTrue(describe(fixture), held < fixture.host.coordinator.range)
            fixture.headerActions.clear()
            fixture.hostEvents.clear()
        }
        // The selectable text is now near the top. Without a long press it claims nothing.
        drag(scenario, SELECTABLE, dy = dp(80), holdMs = 300)
        SystemClock.sleep(300)
        scenario.onFragment { fixture ->
            val expanded = deliveredTravel(fixture) - slop
            assertTrue("held=$held " + describe(fixture), fixture.host.coordinator.offset <= held - expanded + 1)
            assertEquals(describe(fixture), listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL), fixture.headerActions)
        }
    }

    private fun withHarness(block: (FragmentScenario<DataviewComposeHeaderHarnessFragment>) -> Unit) {
        val scenario = launchFragmentInContainer<DataviewComposeHeaderHarnessFragment>(themeResId = R.style.AppTheme)
        try {
            await(scenario) { fixture ->
                fixture.regionBounds.keys.containsAll(listOf(BACKGROUND, CHIP, LIST, SELECTABLE)) &&
                    fixture.chipScroll != null && fixture.listState != null && fixture.composeView.height > 0 &&
                    fixture.host.getChildAt(2).height in 1..(fixture.rowsStripPx + 1)
            }
            block(scenario)
        } finally {
            scenario.close()
        }
    }

    /** Distance between the delivered down and the last delivered move, in host pixels. */
    private fun deliveredTravel(fixture: DataviewComposeHeaderHarnessFragment): Float {
        val down = fixture.hostEvents.first { it.first == MotionEvent.ACTION_DOWN }.second
        val last = fixture.hostEvents.last { it.first == MotionEvent.ACTION_MOVE }.second
        return kotlin.math.abs(down - last)
    }

    private fun describe(fixture: DataviewComposeHeaderHarnessFragment): String =
        "offset=${fixture.host.coordinator.offset} range=${fixture.host.coordinator.range} slop=$slop " +
            "host=${fixture.host.height} viewport=${fixture.host.getChildAt(2).height} header=${fixture.headerActions} " +
            "events=" + fixture.hostEvents.joinToString { (action, y, offset) -> "$action@${y.toInt()}:${offset.toInt()}" }

    private val slop: Float
        get() = ViewConfiguration.get(InstrumentationRegistry.getInstrumentation().targetContext).scaledTouchSlop.toFloat()

    private fun dp(value: Int): Float =
        value * InstrumentationRegistry.getInstrumentation().targetContext.resources.displayMetrics.density

    private fun screenPoint(scenario: FragmentScenario<DataviewComposeHeaderHarnessFragment>, region: String): FloatArray {
        lateinit var point: FloatArray
        scenario.onFragment { fixture ->
            val bounds = fixture.regionBounds.getValue(region)
            val location = IntArray(2).also(fixture.composeView::getLocationOnScreen)
            point = floatArrayOf(location[0] + bounds.center.x, location[1] + bounds.center.y)
        }
        return point
    }

    /** Six moves at the input timebase; a hold before the release drains the fling velocity. */
    private fun drag(
        scenario: FragmentScenario<DataviewComposeHeaderHarnessFragment>,
        region: String,
        dx: Float = 0f,
        dy: Float = 0f,
        pressMs: Long = 0,
        holdMs: Long = 0
    ) {
        val (x, y) = screenPoint(scenario, region)
        val downTime = SystemClock.uptimeMillis()
        inject(MotionEvent.ACTION_DOWN, downTime, downTime, x, y)
        if (pressMs > 0) SystemClock.sleep(pressMs)
        for (step in 1..6) {
            SystemClock.sleep(16)
            inject(MotionEvent.ACTION_MOVE, downTime, SystemClock.uptimeMillis(), x + dx * step / 6, y + dy * step / 6)
        }
        if (holdMs > 0) SystemClock.sleep(holdMs)
        inject(MotionEvent.ACTION_UP, downTime, SystemClock.uptimeMillis(), x + dx, y + dy)
    }

    private fun inject(action: Int, downTime: Long, eventTime: Long, x: Float, y: Float) {
        val event = MotionEvent.obtain(downTime, eventTime, action, x, y, 0)
        try { DataviewNativeInput.injectMotionEvent(event) } finally { event.recycle() }
    }

    private fun await(
        scenario: FragmentScenario<DataviewComposeHeaderHarnessFragment>,
        predicate: (DataviewComposeHeaderHarnessFragment) -> Boolean
    ) {
        val deadline = SystemClock.uptimeMillis() + 5_000
        do {
            var satisfied = false
            scenario.onFragment { satisfied = predicate(it) }
            if (satisfied) return
            SystemClock.sleep(16)
        } while (SystemClock.uptimeMillis() < deadline)
        throw AssertionError("Compose header fixture did not reach the expected state within 5 seconds")
    }
}
