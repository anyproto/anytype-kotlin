package com.anytypeio.anytype.features.sets.dv

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.anytypeio.anytype.R
import com.anytypeio.anytype.features.sets.dv.DataviewComposeHeaderHarnessFragment.Companion.BACKGROUND
import com.anytypeio.anytype.features.sets.dv.DataviewComposeHeaderHarnessFragment.Companion.CHIP
import com.anytypeio.anytype.features.sets.dv.DataviewComposeHeaderHarnessFragment.Companion.LIST
import com.anytypeio.anytype.features.sets.dv.DataviewComposeHeaderHarnessFragment.Companion.SELECTABLE
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private typealias Fixture = DataviewComposeHeaderHarnessFragment

/**
 * A Compose object header, the native controls row and the grid's chrome, laid out so the
 * rows keep a 17dp strip as a phone shows in landscape. Every surface that cannot scroll
 * vertically must accept the collapse drag, while each child keeps every gesture that it
 * claims itself. Real pointer injection, so Compose's and RecyclerView's own arbitration runs.
 * Each test runs in both orientations; the activity asks for its orientation itself, because
 * the emulator's rotation setting does not reach a test activity reliably.
 */
@RunWith(Parameterized::class)
@LargeTest
class DataviewComposeHeaderHarnessTest(private val orientation: Int) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun orientations() = listOf(Configuration.ORIENTATION_PORTRAIT, Configuration.ORIENTATION_LANDSCAPE)
    }

    // Object header (child 0).

    @Test
    fun dragOnTheHeaderBackgroundCollapsesItWhenRowsHaveNoRoom() = withHarness { scenario ->
        var viewportBefore = 0
        scenario.onFragment { fixture ->
            viewportBefore = fixture.host.getChildAt(2).height
            assertTrue(describe(fixture), viewportBefore - fixture.viewportChromePx <= fixture.rowsStripPx + 1)
            assertEquals(0f, fixture.host.coordinator.offset, 0f)
        }
        drag(scenario, region(BACKGROUND), dy = -dp(160))
        await(scenario) { it.host.coordinator.offset > 0f }
        SystemClock.sleep(600)
        scenario.onFragment { fixture ->
            // Every delivered pixel past slop collapsed the header; the release only adds a fling.
            assertCollapsedByDeliveredTravel(fixture)
            assertTrue(describe(fixture), fixture.host.getChildAt(2).height > viewportBefore)
            // The empty background hit no Compose handler, so the host owned the stream at once.
            assertEquals(describe(fixture), listOf(MotionEvent.ACTION_DOWN), fixture.headerActions)
        }
    }

    @Test
    fun verticalDragOnAChipCollapsesTheHeaderWithoutAClick() = withHarness { scenario ->
        drag(scenario, region(CHIP), dy = -dp(160))
        await(scenario) { it.host.coordinator.offset > 0f }
        SystemClock.sleep(600)
        scenario.onFragment { fixture ->
            assertEquals(describe(fixture), 0, fixture.chipClicks)
            assertCollapsedByDeliveredTravel(fixture)
            // The chip saw the move first, did not ask to keep it, and was cancelled in the same event.
            assertEquals(describe(fixture), listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL), fixture.headerActions)
        }
    }

    @Test
    fun tapOnAChipClicksWithoutMovingTheHeader() = withHarness { scenario ->
        tap(scenario, region(CHIP))
        await(scenario) { it.chipClicks == 1 }
        scenario.onFragment { fixture ->
            assertEquals(0f, fixture.host.coordinator.offset, 0f)
            assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP), fixture.headerActions)
        }
    }

    @Test
    fun horizontalDragOnTheChipRowScrollsTheRowAndKeepsTheHeader() = withHarness { scenario ->
        drag(scenario, region(CHIP), dx = -dp(120))
        await(scenario) { (it.chipScroll?.value ?: 0) > 0 }
        scenario.onFragment { fixture ->
            assertEquals(0f, fixture.host.coordinator.offset, 0f)
            assertTrue(fixture.disallowRequests > 0)
            assertKept(fixture.headerActions, fixture)
        }
    }

    @Test
    fun verticalDragOnAComposeListScrollsTheListAndKeepsTheHeader() = withHarness { scenario ->
        drag(scenario, region(LIST), dy = -dp(80))
        await(scenario) {
            val state = it.listState
            state != null && (state.firstVisibleItemIndex > 0 || state.firstVisibleItemScrollOffset > 0)
        }
        scenario.onFragment { fixture ->
            // Both crossed slop on the same move. The list saw it first and asked to keep it.
            assertEquals(0f, fixture.host.coordinator.offset, 0f)
            assertTrue(fixture.disallowRequests > 0)
            assertKept(fixture.headerActions, fixture)
        }
    }

    @Test
    fun longPressDragOnSelectableTextKeepsTheSelectionGesture() = withHarness { scenario ->
        drag(scenario, region(SELECTABLE), dy = -dp(60), pressMs = ViewConfiguration.getLongPressTimeout() + 300L)
        SystemClock.sleep(300)
        scenario.onFragment { fixture ->
            assertEquals(0f, fixture.host.coordinator.offset, 0f)
            assertTrue(fixture.disallowRequests > 0)
            assertKept(fixture.headerActions, fixture)
        }
    }

    @Test
    fun dragDownOnAPartlyCollapsedHeaderExpandsIt() = withHarness { scenario ->
        drag(scenario, region(BACKGROUND), dy = -dp(120), holdMs = 300)
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
        drag(scenario, region(SELECTABLE), dy = dp(80), holdMs = 300)
        SystemClock.sleep(300)
        scenario.onFragment { fixture ->
            val expanded = deliveredTravel(fixture) - slop
            assertTrue("held=$held " + describe(fixture), fixture.host.coordinator.offset <= held - expanded + 1)
            assertEquals(describe(fixture), listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL), fixture.headerActions)
        }
    }

    // Pinned controls row (child 1).

    @Test
    fun verticalDragOnTheControlsRowBackgroundCollapsesTheHeader() = withHarness { scenario ->
        drag(scenario, view { it.controlsSpacer }, dy = -dp(160))
        await(scenario) { it.host.coordinator.offset > 0f }
        SystemClock.sleep(600)
        scenario.onFragment { fixture ->
            assertCollapsedByDeliveredTravel(fixture)
            assertEquals(describe(fixture), listOf(MotionEvent.ACTION_DOWN), fixture.controlsActions)
        }
    }

    @Test
    fun verticalDragOnTheViewSelectorCollapsesTheHeaderWithoutAClick() = withHarness { scenario ->
        drag(scenario, view { it.viewSelector }, dy = -dp(160))
        await(scenario) { it.host.coordinator.offset > 0f }
        SystemClock.sleep(600)
        scenario.onFragment { fixture ->
            assertEquals(describe(fixture), 0, fixture.selectorClicks)
            assertCollapsedByDeliveredTravel(fixture)
            assertEquals(describe(fixture), listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL), fixture.controlsActions)
        }
    }

    @Test
    fun tapsOnTheControlsRowClickWithoutMovingTheHeader() = withHarness { scenario ->
        tap(scenario, view { it.viewSelector })
        await(scenario) { it.selectorClicks == 1 }
        tap(scenario, view { it.filterIcon })
        await(scenario) { it.filterClicks == 1 }
        tap(scenario, view { it.newButton })
        await(scenario) { it.newClicks == 1 }
        scenario.onFragment { fixture ->
            assertEquals(0f, fixture.host.coordinator.offset, 0f)
            assertFalse(fixture.controlsActions.contains(MotionEvent.ACTION_CANCEL))
        }
    }

    @Test
    fun horizontalDragOnTheControlsRowKeepsTheHeader() = withHarness { scenario ->
        drag(scenario, view { it.viewSelector }, dx = dp(120), dy = dp(4))
        SystemClock.sleep(300)
        scenario.onFragment { fixture ->
            assertEquals(describe(fixture), 0f, fixture.host.coordinator.offset, 0f)
            assertKept(fixture.controlsActions, fixture)
        }
    }

    // Viewer chrome (child 2).

    @Test
    fun verticalDragOnTheColumnHeaderRowCollapsesTheHeader() = withHarness { scenario ->
        var viewportBefore = 0
        scenario.onFragment { viewportBefore = it.host.getChildAt(2).height }
        drag(scenario, view { it.columns.getChildAt(0) }, dy = -dp(160))
        await(scenario) { it.host.coordinator.offset > 0f }
        SystemClock.sleep(600)
        scenario.onFragment { fixture ->
            assertEquals(describe(fixture), 0, fixture.columnClicks)
            assertCollapsedByDeliveredTravel(fixture)
            assertTrue(describe(fixture), fixture.host.getChildAt(2).height > viewportBefore)
            // The column row scrolls only horizontally, so it saw the move and lost the stream.
            assertEquals(describe(fixture), listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL), fixture.viewportActions)
        }
    }

    @Test
    fun horizontalDragOnTheColumnHeaderRowScrollsTheColumnsAndKeepsTheHeader() = withHarness { scenario ->
        drag(scenario, view { it.columns.getChildAt(0) }, dx = -dp(120))
        await(scenario) { it.horizontalScroll.scrollX > 0 }
        scenario.onFragment { fixture ->
            assertEquals(describe(fixture), 0f, fixture.host.coordinator.offset, 0f)
            assertTrue(fixture.disallowRequests > 0)
            assertKept(fixture.viewportActions, fixture)
        }
    }

    @Test
    fun verticalDragOnTheRowsCollapsesThroughTheNestedScroll() = withHarness { scenario ->
        // The strip under the column row lies under the navigation bar, where the system
        // refuses to inject, so open some room first with a held drag on the header.
        drag(scenario, region(BACKGROUND), dy = -dp(120), holdMs = 300)
        await(scenario) { it.host.coordinator.offset > 0f }
        SystemClock.sleep(300)
        var held = 0f
        scenario.onFragment { fixture ->
            held = fixture.host.coordinator.offset
            fixture.viewportActions.clear()
            fixture.hostEvents.clear()
        }
        drag(scenario, view(yFraction = 0f, yOffsetDp = 24) { it.rows }, dy = -dp(120))
        await(scenario) { it.host.coordinator.offset > held }
        SystemClock.sleep(600)
        scenario.onFragment { fixture ->
            // The rows keep the stream and feed the host through nested scrolling instead.
            assertTrue("held=$held " + describe(fixture), fixture.host.coordinator.offset > held)
            assertKept(fixture.viewportActions, fixture)
        }
    }

    private fun withHarness(block: (FragmentScenario<Fixture>) -> Unit) {
        val scenario = launchFragmentInContainer<Fixture>(themeResId = R.style.AppTheme)
        try {
            scenario.onFragment { fixture ->
                fixture.requireActivity().requestedOrientation =
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            await(scenario) { fixture ->
                fixture.resources.configuration.orientation == orientation &&
                    fixture.regionBounds.keys.containsAll(listOf(BACKGROUND, CHIP, LIST, SELECTABLE)) &&
                    fixture.chipScroll != null && fixture.listState != null && fixture.composeView.height > 0 &&
                    fixture.columns.childCount > 0 && fixture.rows.childCount > 0 &&
                    fixture.host.getChildAt(2).height - fixture.viewportChromePx in 1..(fixture.rowsStripPx + 1)
            }
            block(scenario)
        } finally {
            scenario.close()
        }
    }

    private fun assertCollapsedByDeliveredTravel(fixture: Fixture) {
        assertTrue(describe(fixture), fixture.host.coordinator.offset >= deliveredTravel(fixture) - slop - 1)
    }

    /** The child kept the whole stream: no cancel, and the release reached it. */
    private fun assertKept(actions: List<Int>, fixture: Fixture) {
        assertFalse(describe(fixture), actions.contains(MotionEvent.ACTION_CANCEL))
        assertEquals(describe(fixture), MotionEvent.ACTION_UP, actions.last())
    }

    /** Distance between the delivered down and the last delivered move, in host pixels. */
    private fun deliveredTravel(fixture: Fixture): Float {
        val down = fixture.hostEvents.first { it.first == MotionEvent.ACTION_DOWN }.second
        val last = fixture.hostEvents.last { it.first == MotionEvent.ACTION_MOVE }.second
        return abs(down - last)
    }

    private fun describe(fixture: Fixture): String =
        "orientation=${fixture.resources.configuration.orientation} offset=${fixture.host.coordinator.offset} " +
            "range=${fixture.host.coordinator.range} slop=$slop host=${fixture.host.height} viewport=${fixture.host.getChildAt(2).height} " +
            "header=${fixture.headerActions} controls=${fixture.controlsActions} viewport=${fixture.viewportActions} " +
            "events=" + fixture.hostEvents.joinToString { (action, y, offset) -> "$action@${y.toInt()}:${offset.toInt()}" }

    private val slop: Float
        get() = ViewConfiguration.get(InstrumentationRegistry.getInstrumentation().targetContext).scaledTouchSlop.toFloat()

    private fun dp(value: Int): Float =
        value * InstrumentationRegistry.getInstrumentation().targetContext.resources.displayMetrics.density

    /** Screen point at the centre of a Compose region of the header. */
    private fun region(name: String): (Fixture) -> FloatArray = { fixture ->
        val bounds = fixture.regionBounds.getValue(name)
        val location = IntArray(2).also(fixture.composeView::getLocationOnScreen)
        floatArrayOf(location[0] + bounds.center.x, location[1] + bounds.center.y)
    }

    /**
     * Screen point inside the visible part of a native view, at fractions of that part plus
     * an offset. The grid is wider than the screen and the rows are clipped to a strip.
     */
    private fun view(
        xFraction: Float = .5f,
        yFraction: Float = .5f,
        yOffsetDp: Int = 0,
        target: (Fixture) -> View
    ): (Fixture) -> FloatArray = { fixture ->
        val view = target(fixture)
        val visible = Rect().also { check(view.getLocalVisibleRect(it)) { "$view is not visible" } }
        val location = IntArray(2).also(view::getLocationOnScreen)
        floatArrayOf(
            location[0] + visible.left + visible.width() * xFraction,
            location[1] + visible.top + visible.height() * yFraction + dp(yOffsetDp)
        )
    }

    private fun screenPoint(scenario: FragmentScenario<Fixture>, target: (Fixture) -> FloatArray): FloatArray {
        lateinit var point: FloatArray
        scenario.onFragment { point = target(it) }
        lastGeometry = geometry(scenario, point)
        return point
    }

    private var lastGeometry = ""

    /** Where the fixture sits on the screen, for a refused injection. */
    private fun geometry(scenario: FragmentScenario<Fixture>, point: FloatArray): String {
        var text = ""
        scenario.onFragment { fixture ->
            fun place(name: String, view: View): String {
                val location = IntArray(2).also(view::getLocationOnScreen)
                return "$name@(${location[0]},${location[1]})+${view.width}x${view.height}"
            }
            val metrics = fixture.resources.displayMetrics
            text = "point=(${point[0].toInt()},${point[1].toInt()}) screen=${metrics.widthPixels}x${metrics.heightPixels} " +
                "orientation=${fixture.resources.configuration.orientation} offset=${fixture.host.coordinator.offset} " + listOf(
                    place("host", fixture.host), place("viewport", fixture.host.getChildAt(2)),
                    place("scroll", fixture.horizontalScroll), place("columns", fixture.columns), place("rows", fixture.rows)
                ).joinToString(" ")
        }
        return text
    }

    private fun tap(scenario: FragmentScenario<Fixture>, target: (Fixture) -> FloatArray) {
        val (x, y) = screenPoint(scenario, target)
        val downTime = SystemClock.uptimeMillis()
        inject(MotionEvent.ACTION_DOWN, downTime, downTime, x, y)
        SystemClock.sleep(50)
        inject(MotionEvent.ACTION_UP, downTime, SystemClock.uptimeMillis(), x, y)
    }

    /** Six moves at the input timebase; a hold before the release drains the fling velocity. */
    private fun drag(
        scenario: FragmentScenario<Fixture>,
        target: (Fixture) -> FloatArray,
        dx: Float = 0f,
        dy: Float = 0f,
        pressMs: Long = 0,
        holdMs: Long = 0
    ) {
        val (x, y) = screenPoint(scenario, target)
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
        try {
            DataviewNativeInput.injectMotionEvent(event)
        } catch (refused: IllegalArgumentException) {
            throw AssertionError("Injection of action $action at (${x.toInt()},${y.toInt()}) refused; $lastGeometry", refused)
        } finally {
            event.recycle()
        }
    }

    private fun await(scenario: FragmentScenario<Fixture>, predicate: (Fixture) -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 5_000
        do {
            var satisfied = false
            scenario.onFragment { satisfied = predicate(it) }
            if (satisfied) return
            SystemClock.sleep(16)
        } while (SystemClock.uptimeMillis() < deadline)
        throw AssertionError("Dataview chrome fixture did not reach the expected state within 5 seconds")
    }
}
