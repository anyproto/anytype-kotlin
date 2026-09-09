package com.anytypeio.anytype.features.sets.dv

import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import androidx.core.view.ViewCompat
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.MotionOrigin
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Native gestures retain animation settings; these tests deliberately do not disable animations. */
@RunWith(AndroidJUnit4::class)
@LargeTest
class DataviewScrollHarnessTest {
    @Test
    fun headerBackgroundReleaseKeepsPartialPositionAndDoesNotFlingAColumn() = withHarness { scenario ->
        lateinit var header: View
        scenario.onFragment { header = it.host.getChildAt(0) }
        performDataviewNativeAction(header, swipe(upward = true, distance = .15f, xFraction = .7f))
        SystemClock.sleep(600)
        var settled = 0f
        scenario.onFragment { fixture ->
            settled = fixture.host.coordinator.offset
            assertTrue(settled > 0f && settled < fixture.host.coordinator.range)
            assertEquals(0, fixture.nativeOffset)
            assertEquals(0, fixture.columnStates.getValue(DataviewScrollHarnessFragment.TALL).firstVisibleItemScrollOffset)
        }
        SystemClock.sleep(300)
        scenario.onFragment { fixture -> assertEquals(settled, fixture.host.coordinator.offset, 0f) }
    }

    @Test
    fun nativeDispatchReportsMovingViewportInTheSameOffsetInWindowTransaction() = withHarness { scenario ->
        scenario.onFragment { fixture ->
            fixture.host.coordinator.restoreProgress(0f)
            assertTrue(fixture.nativeList.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH))
            val consumed = IntArray(2)
            val offsetInWindow = IntArray(2)
            fixture.nativeList.dispatchNestedPreScroll(0, 40, consumed, offsetInWindow, ViewCompat.TYPE_TOUCH)
            assertEquals(40, consumed[1])
            assertEquals(0, offsetInWindow[0])
            assertEquals(-40, offsetInWindow[1])
            fixture.nativeList.stopNestedScroll(ViewCompat.TYPE_TOUCH)
        }
    }

    @Test
    fun nativePointerBoundaryCrossingConservesPhysicalScreenDisplacement() = withHarness { scenario ->
        lateinit var fixture: DataviewScrollHarnessFragment
        scenario.onFragment { fixture = it }
        performDataviewNativeAction(fixture.nativeList.parent as View, object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription() = "cross the header boundary in one native pointer movement"
            override fun perform(uiController: UiController, view: View) {
                val range = fixture.host.coordinator.range
                checkDataviewMain { fixture.host.coordinator.restoreProgress((range - 120f).coerceAtLeast(0f) / range) }
                uiController.loopMainThreadForAtLeast(32)
                val visible = android.graphics.Rect()
                assertTrue(view.getGlobalVisibleRect(visible))
                val x = visible.left + visible.width() * .5f
                val y = visible.top + visible.height() * .8f
                val downTime = SystemClock.uptimeMillis()
                fun inject(action: Int, nextY: Float) {
                    val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, nextY, 0)
                    try { assertTrue(uiController.injectMotionEvent(event)) } finally { event.recycle() }
                    uiController.loopMainThreadForAtLeast(32)
                }
                inject(MotionEvent.ACTION_DOWN, y)
                inject(MotionEvent.ACTION_MOVE, y - 80f) // Native interception may only establish its baseline here.
                inject(MotionEvent.ACTION_MOVE, y - 100f) // Ensure one real nested frame before measuring a crossing.
                val (headerBefore, childBefore) = checkDataviewMain { fixture.host.coordinator.offset to fixture.nativeOffset }
                assertTrue(headerBefore < range)
                inject(MotionEvent.ACTION_MOVE, y - 220f)
                val (headerAfter, childAfter) = checkDataviewMain { fixture.host.coordinator.offset to fixture.nativeOffset }
                val headerDelta = headerAfter - headerBefore
                val childDelta = childAfter - childBefore
                assertTrue("before=$headerBefore headerDelta=$headerDelta childDelta=$childDelta range=$range trace=${fixture.nativeTrace}", headerDelta > 0f && childDelta > 0)
                assertEquals("Physical screen delta must be consumed exactly once at the moving origin",
                    120f, headerDelta + childDelta, 1f)
                val collapsedHeader = headerAfter
                val scrolledChild = childAfter
                inject(MotionEvent.ACTION_MOVE, y - 100f)
                val (reverseHeader, reverseChild) = checkDataviewMain {
                    fixture.host.coordinator.offset - collapsedHeader to fixture.nativeOffset - scrolledChild
                }
                assertTrue(reverseHeader < 0f && reverseChild < 0)
                assertEquals("Downward crossing must spend child distance before header residual",
                    -120f, reverseHeader + reverseChild, 1f)
                inject(MotionEvent.ACTION_CANCEL, y - 100f)
            }
        })
    }

    @Test
    fun nativeHeldGestureGeometryChangeCancelsTailAndFreshDownResumes() = withHarness { scenario ->
        lateinit var fixture: DataviewScrollHarnessFragment
        scenario.onFragment { fixture = it }
        val view = fixture.nativeList.parent as View
        fun bounds() = checkDataviewMain { android.graphics.Rect().also { assertTrue(view.getGlobalVisibleRect(it)) } }
        var visible = bounds()
        val x = visible.centerX().toFloat()
        var y = visible.top + visible.height() * .8f
        var downTime = SystemClock.uptimeMillis()
        fun inject(action: Int, nextY: Float) {
            val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, nextY, 0)
            try { DataviewNativeInput.injectMotionEvent(event) } finally { event.recycle() }
            SystemClock.sleep(32)
        }
        inject(MotionEvent.ACTION_DOWN, y)
        inject(MotionEvent.ACTION_MOVE, y - 80f)
        inject(MotionEvent.ACTION_MOVE, y - 120f)
        checkDataviewMain {
            assertTrue(fixture.host.coordinator.offset in 1f..(fixture.host.coordinator.range - 1f))
            val header = fixture.host.getChildAt(0)
            header.layoutParams = header.layoutParams.apply { height += 100 }
            fixture.host.requestLayout()
        }
        SystemClock.sleep(100)
        val afterGeometry = checkDataviewMain { fixture.host.coordinator.offset to fixture.nativeOffset }
        inject(MotionEvent.ACTION_MOVE, y - 240f)
        inject(MotionEvent.ACTION_MOVE, y - 340f)
        inject(MotionEvent.ACTION_UP, y - 340f)
        SystemClock.sleep(250)
        checkDataviewMain {
            assertEquals("Geometry-invalidated stream must not move the header", afterGeometry.first, fixture.host.coordinator.offset, 0f)
            assertEquals("Geometry-invalidated stream must not scroll native rows", afterGeometry.second, fixture.nativeOffset)
            assertEquals(RecyclerView.SCROLL_STATE_IDLE, fixture.nativeList.scrollState)
        }
        visible = bounds()
        y = visible.top + visible.height() * .8f
        downTime = SystemClock.uptimeMillis()
        inject(MotionEvent.ACTION_DOWN, y)
        inject(MotionEvent.ACTION_MOVE, y - 80f)
        inject(MotionEvent.ACTION_MOVE, y - 160f)
        checkDataviewMain {
            assertTrue("Fresh native DOWN must resume collapse", fixture.host.coordinator.offset > afterGeometry.first)
            assertEquals("Fresh upward motion still collapses before native rows", afterGeometry.second, fixture.nativeOffset)
        }
        inject(MotionEvent.ACTION_CANCEL, y - 160f)
    }

    @Test
    fun parent3ConservesBoundaryDeltasAndPreservesAccumulatedConsumption() = withHarness { scenario ->
        scenario.onFragment { fixture ->
            with(fixture.host) {
                coordinator.setRange(200f)
                coordinator.restoreProgress(.75f)
                fixture.startNativeSession()
                val pre = intArrayOf(7, 9)
                onNestedPreScroll(fixture.nativeList, 100, 80, pre, ViewCompat.TYPE_TOUCH)
                assertEquals(7, pre[0])
                assertEquals(59, pre[1])
                assertEquals(200f, coordinator.offset, 0f)

                val reverse = intArrayOf(0, 0)
                onNestedPreScroll(fixture.nativeList, 0, -80, reverse, ViewCompat.TYPE_TOUCH)
                assertEquals(0, reverse[1])
                // Child spends 30px reaching its top. Only its 50px residual expands the header.
                val post = intArrayOf(11, 13)
                onNestedScroll(fixture.nativeList, 0, -30, 0, -50, ViewCompat.TYPE_TOUCH, post)
                assertEquals(11, post[0])
                assertEquals(-37, post[1])
                assertEquals(150f, coordinator.offset, 0f)
                onStopNestedScroll(fixture.nativeList, ViewCompat.TYPE_TOUCH)
            }
        }
    }

    @Test
    fun nativeNonTouchSessionUsesTheSameBoundaryAndRejectsComposeInterop() = withHarness { scenario ->
        scenario.onFragment { fixture ->
            with(fixture.host) {
                coordinator.setRange(200f)
                coordinator.restoreProgress(.5f)
                fixture.startNativeSession(ViewCompat.TYPE_NON_TOUCH)
                val pre = intArrayOf(0, 0)
                onNestedPreScroll(fixture.nativeList, 0, 170, pre, ViewCompat.TYPE_NON_TOUCH)
                assertEquals(100, pre[1])
                val post = intArrayOf(0, 0)
                onNestedScroll(fixture.nativeList, 0, 0, 0, -250, ViewCompat.TYPE_NON_TOUCH, post)
                assertEquals(-200, post[1])
                assertEquals(0f, coordinator.offset, 0f)
                assertFalse(onStartNestedScroll(
                    fixture.composeView, fixture.composeView,
                    ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH
                ))
                assertFalse(onStartNestedScroll(
                    fixture.nativeList, fixture.nativeList,
                    ViewCompat.SCROLL_AXIS_HORIZONTAL, ViewCompat.TYPE_TOUCH
                ))
                onStopNestedScroll(fixture.nativeList, ViewCompat.TYPE_NON_TOUCH)
            }
        }
    }

    @Test
    fun tableWrapperRoutesNativeGesturesAndDeepReversalKeepsHeaderCollapsed() = withHarness { scenario ->
        swipeNative(scenario, upward = true)
        waitForNativeIdle(scenario)
        scenario.onFragment { fixture ->
            assertEquals(fixture.host.coordinator.range, fixture.host.coordinator.offset, 1f)
            assertTrue(fixture.nativeOffset > 0)
            (fixture.nativeList.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(80, 0)
        }
        swipeNative(scenario, upward = false, distance = .15f)
        waitForNativeIdle(scenario)
        scenario.onFragment { fixture ->
            assertEquals(fixture.host.coordinator.range, fixture.host.coordinator.offset, 1f)
            assertTrue((fixture.nativeList.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() > 0)
        }
    }

    @Test
    fun emptyFullHeightColumnCollapsesAndExpandsWithoutMovingItsSibling() = withHarness { scenario ->
        swipeColumn(scenario, DataviewScrollHarnessFragment.EMPTY, upward = true)
        await(scenario) { it.host.coordinator.offset == it.host.coordinator.range }
        swipeColumn(scenario, DataviewScrollHarnessFragment.EMPTY, upward = false)
        await(scenario) { it.host.coordinator.offset == 0f }
        scenario.onFragment { fixture ->
            val tall = fixture.columnStates.getValue(DataviewScrollHarnessFragment.TALL)
            assertEquals(0, tall.firstVisibleItemIndex)
            assertEquals(0, tall.firstVisibleItemScrollOffset)
            assertTrue(fixture.headerTrace.all { it >= 0f && it <= fixture.host.coordinator.range })
            assertTrue(fixture.composeView.height > 0)
        }
    }

    @Test
    fun recyclerViewOwnsFlingAcrossHeaderBoundary() = withHarness { scenario ->
        scenario.onFragment { fixture ->
            fixture.host.coordinator.restoreProgress(.5f)
            assertTrue(fixture.nativeList.fling(0, 8_000))
        }
        waitForNativeIdle(scenario)
        scenario.onFragment { fixture ->
            assertEquals(fixture.host.coordinator.range, fixture.host.coordinator.offset, 1f)
            assertTrue(fixture.nativeOffset > 0)
        }
    }

    @Test
    fun nativeTopPullThenReverseKeepsHeaderBoundedAndSiblingStationary() = withHarness { scenario ->
        swipeNative(scenario, upward = false, distance = .25f)
        scenario.onFragment { assertEquals(0f, it.host.coordinator.offset, 0f) }
        swipeNative(scenario, upward = true, distance = .35f)
        waitForNativeIdle(scenario)
        scenario.onFragment { fixture ->
            assertTrue(fixture.host.coordinator.offset > 0f)
            assertTrue(fixture.headerTrace.all { it in 0f..fixture.host.coordinator.range })
            val sibling = fixture.columnStates.getValue(DataviewScrollHarnessFragment.TALL)
            assertEquals(0, sibling.firstVisibleItemIndex)
            assertEquals(0, sibling.firstVisibleItemScrollOffset)
        }
    }

    @Test
    fun nativeTopPullThenFlingDoesNotLoseRemainingHeaderTravel() = withHarness { scenario ->
        // Reverse before lifting: edge relaxation cannot complete in an inter-gesture idle gap.
        lateinit var fixture: DataviewScrollHarnessFragment
        scenario.onFragment { fixture = it }
        performDataviewNativeAction(fixture.nativeList.parent as View, pullThenReverse(beforeReverse = {
                if (android.os.Build.VERSION.SDK_INT >= 31) {
                    val edge = fixture.nativeEdges[RecyclerView.EdgeEffectFactory.DIRECTION_TOP]
                    assertTrue("Continuous native pull must establish stretch before reversal",
                        edge != null && androidx.core.widget.EdgeEffectCompat.getDistance(edge) > 0f)
                }
            }))
        waitForNativeIdle(scenario)
        scenario.onFragment { fixture ->
            assertEquals(fixture.host.coordinator.range, fixture.host.coordinator.offset, 1f)
            assertTrue(fixture.headerTrace.all { it in 0f..fixture.host.coordinator.range })
        }
    }

    @Test
    fun composeTopPullThenReverseKeepsSharedTravelBounded() = withHarness { scenario ->
        lateinit var fixture: DataviewScrollHarnessFragment
        scenario.onFragment { fixture = it }
        performDataviewNativeAction(fixture.composeView, pullThenReverse(xFraction = .25f, beforeReverse = {
                if (android.os.Build.VERSION.SDK_INT >= 31) {
                    assertTrue("Continuous Compose pull must establish stretch before reversal",
                        fixture.overscrollEffects.getValue(DataviewScrollHarnessFragment.TALL)?.isInProgress == true)
                }
            }))
        await(scenario) { it.host.coordinator.offset > 0f }
        scenario.onFragment { fixture ->
            assertTrue(fixture.headerTrace.all { it in 0f..fixture.host.coordinator.range })
            assertEquals(0, fixture.columnStates.getValue(DataviewScrollHarnessFragment.EMPTY).firstVisibleItemIndex)
        }
    }

    @Test
    fun composeNativeReleaseContinuesHeaderAtTopAndBottom() = withHarness { scenario ->
        for (atBottom in listOf(false, true)) {
            val positioned = AtomicReference(false)
            scenario.onFragment { fixture ->
                val column = DataviewScrollHarnessFragment.TALL
                fixture.composeScope.launch {
                    fixture.connections.getValue(column).withOrigin(MotionOrigin.Restoration) {
                        fixture.columnStates.getValue(column).scrollToItem(if (atBottom) 150 else 0)
                    }
                    fixture.host.coordinator.restoreProgress(.5f)
                    positioned.set(true)
                }
            }
            await(scenario) { positioned.get() }
            var before = 0f
            var dragDistance = 0f
            lateinit var columnView: View
            var columnX = .25f
            scenario.onFragment { fixture ->
                before = fixture.host.coordinator.offset
                dragDistance = 96f
                columnView = fixture.composeView
                columnX = fixture.columnBounds.getValue(DataviewScrollHarnessFragment.TALL).center.x / fixture.composeView.width
            }
            // At the top a downward release can only expand; at the bottom an upward release
            // can only collapse. The short drag leaves enough range to observe residual inertia.
            controlledNativeSwipe(columnView, columnX, if (atBottom) .8f else .2f, if (atBottom) -96f else 96f)
            try {
                await(scenario) { abs(it.host.coordinator.offset - before) > dragDistance + 1f }
            } catch (failure: AssertionError) {
                scenario.onFragment { fixture ->
                    throw AssertionError("atBottom=$atBottom before=$before after=${fixture.host.coordinator.offset} drag=$dragDistance trace=${fixture.flingTrace}", failure)
                }
            }
        }
    }

    @Test
    fun composeReleaseVelocityComparisonAtFixedAndMovingViewportOrigin() {
        val velocities = mutableListOf<Float>()
        // Suppress edge absorption only in this control comparison. The separate native
        // stretch tests retain real effects and prove their state before reversal.
        for (progress in listOf(0f, .5f)) withHarness(disableOverscroll = true) { scenario ->
            lateinit var fixture: DataviewScrollHarnessFragment
            scenario.onFragment {
                fixture = it
                it.host.coordinator.restoreProgress(progress)
                it.flingTrace.clear()
            }
            SystemClock.sleep(100)
            controlledNativeSwipe(fixture.composeView, .25f, .2f, 96f)
            await(scenario) { it.flingTrace.any { trace -> " pre y=" in trace } }
            scenario.onFragment {
                val velocity = it.flingTrace.first { trace -> " pre y=" in trace }
                    .substringAfter(" pre y=").substringBefore(" h=").toFloat()
                velocities.add(velocity)
                android.util.Log.i("DataviewGate", "originProgress=$progress nativeRelease=$velocity trace=${it.flingTrace}")
            }
        }
        assertTrue("Fixed-origin native release must establish real inertia: $velocities", velocities[0] > 300f)
        assertTrue("Moving viewport must retain physical pointer release velocity: $velocities", velocities[1] > velocities[0] * .7f)
    }

    @Test
    fun emptyComposeResidualUsesNativeTimebaseWithAmbientAnimationsDisabled() = withHarness { scenario ->
        val result = AtomicReference<Velocity?>()
        scenario.onFragment { fixture ->
            fixture.host.coordinator.restoreProgress(.25f)
            fixture.headerTrace.clear()
            val connection = fixture.connections.getValue(DataviewScrollHarnessFragment.EMPTY)
            connection.onPreScroll(Offset(0f, -1f), NestedScrollSource.UserInput)
            fixture.composeScope.launch(object : MotionDurationScale {
                override val scaleFactor: Float = 0f
            }) {
                result.set(connection.onPostFling(Velocity.Zero, Velocity(300f, -6_000f)))
            }
        }
        await(scenario) { result.get() != null }
        scenario.onFragment { fixture ->
            assertEquals(fixture.host.coordinator.range, fixture.host.coordinator.offset, 1f)
            assertTrue("Residual inertia must occupy multiple frames at animator scale zero", fixture.headerTrace.size > 2)
            val consumed = requireNotNull(result.get())
            assertEquals(0f, consumed.x, 0f)
            assertTrue(consumed.y < 0f && abs(consumed.y) < 6_000f)
        }
    }

    @Test
    fun replacementOwnerCancelsComposeContinuationBeforeItCanWriteAnotherFrame() = withHarness { scenario ->
        val oldJob = AtomicReference<Job>()
        scenario.onFragment { fixture ->
            fixture.host.coordinator.restoreProgress(.25f)
            val connection = fixture.connections.getValue(DataviewScrollHarnessFragment.EMPTY)
            connection.onPreScroll(Offset(0f, -1f), NestedScrollSource.UserInput)
            oldJob.set(fixture.composeScope.launch {
                connection.onPostFling(Velocity.Zero, Velocity(0f, -1_000f))
            })
        }
        var offsetAfterReplacement = 0f
        // onFragment executes on the main thread: replacement and the snapshot are atomic.
        scenario.onFragment { fixture ->
            fixture.host.coordinator.begin("replacement-column")
            offsetAfterReplacement = fixture.host.coordinator.offset
        }
        await(scenario) { oldJob.get().isCompleted }
        SystemClock.sleep(100)
        scenario.onFragment { fixture ->
            assertEquals(offsetAfterReplacement, fixture.host.coordinator.offset, 0f)
        }
    }

    @Test
    fun shortColumnLowerBackgroundCollapsesAndExpands() = withHarness(shortCount = 1) { scenario ->
        swipeColumn(scenario, DataviewScrollHarnessFragment.EMPTY, upward = true)
        await(scenario) { it.host.coordinator.offset == it.host.coordinator.range }
        swipeColumn(scenario, DataviewScrollHarnessFragment.EMPTY, upward = false)
        await(scenario) { it.host.coordinator.offset == 0f }
        scenario.onFragment { fixture ->
            assertEquals(0, fixture.columnStates.getValue(DataviewScrollHarnessFragment.EMPTY).firstVisibleItemIndex)
            assertEquals(0, fixture.columnStates.getValue(DataviewScrollHarnessFragment.TALL).firstVisibleItemIndex)
        }
    }

    @Test
    fun deepColumnsKeepIndependentAnchorsAndOnlyActiveTopExpands() = withHarness(shortCount = 150) { scenario ->
        val positioned = AtomicReference(false)
        scenario.onFragment { fixture ->
            fixture.host.coordinator.restoreProgress(1f)
            fixture.composeScope.launch {
                for ((column, index) in listOf(DataviewScrollHarnessFragment.TALL to 80, DataviewScrollHarnessFragment.EMPTY to 50)) {
                    fixture.connections.getValue(column).withOrigin(MotionOrigin.Restoration) {
                        fixture.columnStates.getValue(column).scrollToItem(index, 12)
                    }
                }
                positioned.set(true)
            }
        }
        await(scenario) { positioned.get() }
        swipeColumn(scenario, DataviewScrollHarnessFragment.EMPTY, upward = false, distance = .15f)
        scenario.onFragment { fixture ->
            assertEquals(fixture.host.coordinator.range, fixture.host.coordinator.offset, 1f)
            val inactive = fixture.columnStates.getValue(DataviewScrollHarnessFragment.TALL)
            assertEquals(80, inactive.firstVisibleItemIndex)
            assertEquals(12, inactive.firstVisibleItemScrollOffset)
            assertTrue(fixture.columnStates.getValue(DataviewScrollHarnessFragment.EMPTY).firstVisibleItemIndex > 0)
            positioned.set(false)
            fixture.composeScope.launch {
                fixture.connections.getValue(DataviewScrollHarnessFragment.EMPTY).withOrigin(MotionOrigin.Restoration) {
                    fixture.columnStates.getValue(DataviewScrollHarnessFragment.EMPTY).scrollToItem(0)
                }
                positioned.set(true)
            }
        }
        await(scenario) { positioned.get() }
        swipeColumn(scenario, DataviewScrollHarnessFragment.EMPTY, upward = false, distance = .3f)
        await(scenario) { it.host.coordinator.offset < it.host.coordinator.range }
        scenario.onFragment { fixture ->
            val inactive = fixture.columnStates.getValue(DataviewScrollHarnessFragment.TALL)
            assertEquals(80, inactive.firstVisibleItemIndex)
            assertEquals(12, inactive.firstVisibleItemScrollOffset)
        }
    }

    @Test
    fun anotherColumnHorizontalTouchCancelsResidualBeforeTheNextFrame() = withHarness { scenario ->
        lateinit var fixture: DataviewScrollHarnessFragment
        scenario.onFragment { fixture = it }
        performDataviewNativeAction(fixture.composeView, object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription() = "start residual inertia, then touch another column and move horizontally"
            override fun perform(uiController: UiController, view: View) {
                checkDataviewMain { fixture.host.coordinator.restoreProgress(.25f) }
                val connection = fixture.connections.getValue(DataviewScrollHarnessFragment.EMPTY)
                val oldJob = checkDataviewMain {
                    connection.onPreScroll(Offset(0f, -1f), NestedScrollSource.UserInput)
                    fixture.composeScope.launch(start = CoroutineStart.UNDISPATCHED) {
                        connection.onPostFling(Velocity.Zero, Velocity(0f, -1_000f))
                    }
                }
                // The new touch must also cancel inertia before its first actual frame.
                // Espresso's idle-loop duration is a lower bound, so waiting here can
                // accidentally finish the decay before a replacement pointer arrives.
                assertTrue("Residual must still be active before replacement touch", oldJob.isActive)
                val location = IntArray(2)
                view.getLocationOnScreen(location)
                val bounds = fixture.columnBounds.getValue(DataviewScrollHarnessFragment.TALL)
                val x = location[0] + bounds.center.x
                val y = location[1] + bounds.center.y
                val downTime = SystemClock.uptimeMillis()
                fun inject(action: Int, nextX: Float) {
                    val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, nextX, y, 0)
                    try { assertTrue(uiController.injectMotionEvent(event)) } finally { event.recycle() }
                }
                inject(MotionEvent.ACTION_DOWN, x)
                uiController.loopMainThreadForAtLeast(16)
                val frozen = fixture.host.coordinator.offset
                for (step in 1..8) {
                    uiController.loopMainThreadForAtLeast(16)
                    inject(MotionEvent.ACTION_MOVE, x + step * 5f)
                }
                inject(MotionEvent.ACTION_UP, x + 40f)
                uiController.loopMainThreadForAtLeast(150)
                assertTrue(oldJob.isCancelled)
                assertEquals(frozen, fixture.host.coordinator.offset, 0f)
                assertEquals(0, fixture.columnStates.getValue(DataviewScrollHarnessFragment.TALL).firstVisibleItemIndex)
            }
        })
    }

    @Test
    fun finiteViewportMakesLastNativeRowAndComposeCardFullyReachable() = withHarness { scenario ->
        val positioned = AtomicReference(false)
        scenario.onFragment { fixture ->
            fixture.host.coordinator.restoreProgress(1f)
            (fixture.nativeList.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(199, 0)
            fixture.composeScope.launch {
                fixture.connections.getValue(DataviewScrollHarnessFragment.TALL).withOrigin(MotionOrigin.Restoration) {
                    fixture.columnStates.getValue(DataviewScrollHarnessFragment.TALL).scrollToItem(150)
                }
                positioned.set(true)
            }
        }
        await(scenario) { positioned.get() && !it.nativeList.isLayoutRequested }
        scenario.onFragment { fixture ->
            val manager = fixture.nativeList.layoutManager as LinearLayoutManager
            val last = requireNotNull(manager.findViewByPosition(199))
            assertTrue(last.bottom <= fixture.nativeList.height)
            assertFalse(fixture.nativeList.canScrollVertically(1))
            val state = fixture.columnStates.getValue(DataviewScrollHarnessFragment.TALL)
            val finalCard = state.layoutInfo.visibleItemsInfo.single { it.key == "${DataviewScrollHarnessFragment.TALL}-149" }
            assertTrue(finalCard.offset + finalCard.size <= state.layoutInfo.viewportEndOffset)
            assertFalse(state.canScrollForward)
            val visible = android.graphics.Rect()
            assertTrue(fixture.composeView.getGlobalVisibleRect(visible))
            assertEquals("Actual lazy viewport must equal its visible height inside the stable Android owner",
                state.layoutInfo.viewportSize.height, visible.height())
        }
    }

    @Test
    fun appBarReferenceSharesNativeTopOnlyExpansionPolicy() = withHarness(appBar = true) { scenario ->
        swipeNative(scenario, upward = true)
        waitForNativeIdle(scenario)
        scenario.onFragment { fixture ->
            assertEquals(-fixture.appBar.totalScrollRange, fixture.appBarOffset)
            (fixture.nativeList.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(80, 0)
        }
        swipeNative(scenario, upward = false, distance = .15f)
        waitForNativeIdle(scenario)
        scenario.onFragment { fixture ->
            assertEquals(-fixture.appBar.totalScrollRange, fixture.appBarOffset)
        }
    }

    private fun withHarness(
        appBar: Boolean = false,
        shortCount: Int = 0,
        disableOverscroll: Boolean = false,
        block: (FragmentScenario<DataviewScrollHarnessFragment>) -> Unit
    ) {
        val scenario = launchFragmentInContainer<DataviewScrollHarnessFragment>(
            fragmentArgs = Bundle().apply {
                putBoolean(DataviewScrollHarnessFragment.ARG_APP_BAR, appBar)
                putInt(DataviewScrollHarnessFragment.ARG_SHORT_COUNT, shortCount)
                putBoolean(DataviewScrollHarnessFragment.ARG_DISABLE_OVERSCROLL, disableOverscroll)
            },
            themeResId = R.style.AppTheme
        )
        try {
            await(scenario) { it.columnStates.size == 2 && it.columnBounds.size == 2 && it.composeView.height > 0 }
            block(scenario)
        } finally {
            scenario.close()
        }
    }

    private fun swipeNative(scenario: FragmentScenario<DataviewScrollHarnessFragment>, upward: Boolean, distance: Float = .7f) {
        lateinit var view: View
        scenario.onFragment { view = it.nativeList.parent as View }
        performDataviewNativeAction(view, swipe(upward, distance))
    }

    private fun swipeColumn(
        scenario: FragmentScenario<DataviewScrollHarnessFragment>,
        column: String,
        upward: Boolean,
        distance: Float = .7f,
        speed: Swipe = Swipe.SLOW
    ) {
        var x = .5f
        lateinit var view: View
        scenario.onFragment { fixture ->
            x = fixture.columnBounds.getValue(column).center.x / fixture.composeView.width
            view = fixture.composeView
        }
        performDataviewNativeAction(view, swipe(upward, distance, x, speed))
    }

    private fun swipe(upward: Boolean, distance: Float, xFraction: Float = .3f, speed: Swipe = Swipe.SLOW): ViewAction = object : ViewAction {
        // The stable Android owner intentionally extends above its clipped viewport. Coordinates
        // come from the visible rectangle, so Espresso's unrelated 90%-of-owner constraint fails.
        override fun getConstraints(): Matcher<View> = isDisplayed()
        override fun getDescription() = "swipe inside the actual visible viewport"
        override fun perform(uiController: UiController, view: View) = GeneralSwipeAction(
        speed,
        coordinates(xFraction, if (upward) .85f else .15f),
        coordinates(xFraction, if (upward) .85f - distance else .15f + distance),
        Press.FINGER
        ).perform(uiController, view)
    }

    /** Native pipeline with an identical delivered timebase despite emulator scheduling delays. */
    private fun controlledNativeSwipe(view: View, xFraction: Float, yFraction: Float, distance: Float) {
        val rect = checkDataviewMain { android.graphics.Rect().also { check(view.getGlobalVisibleRect(it)) } }
        val x = rect.left + rect.width() * xFraction
        val y = rect.top + rect.height() * yFraction
        val downTime = SystemClock.uptimeMillis()
        var sample = 0
        fun inject(action: Int, dy: Float) {
            val eventTime = downTime + if (action == MotionEvent.ACTION_UP) 97 else sample * 16L
            val wait = eventTime - SystemClock.uptimeMillis()
            if (wait > 0) SystemClock.sleep(wait)
            val event = MotionEvent.obtain(downTime, eventTime, action, x, y + dy, 0)
            try { DataviewNativeInput.injectMotionEvent(event) } finally { event.recycle() }
            sample++
        }
        inject(MotionEvent.ACTION_DOWN, 0f)
        for (step in 1..6) inject(MotionEvent.ACTION_MOVE, distance * step / 6)
        inject(MotionEvent.ACTION_UP, distance)
    }

    private fun coordinates(xFraction: Float, yFraction: Float) = CoordinatesProvider { view: View ->
        val visible = checkDataviewMain { android.graphics.Rect().also { check(view.getGlobalVisibleRect(it)) } }
        floatArrayOf(visible.left + visible.width() * xFraction, visible.top + visible.height() * yFraction)
    }

    /** One uninterrupted native pointer stream: pull at the top, reverse through stretch, release. */
    private fun pullThenReverse(xFraction: Float = .3f, beforeReverse: () -> Unit = {}) = object : ViewAction {
        override fun getConstraints(): Matcher<View> = isDisplayed()
        override fun getDescription() = "pull at the top and reverse into an upward fling without an idle gap"
        override fun perform(uiController: UiController, view: View) {
            val visible = android.graphics.Rect()
            assertTrue(view.getGlobalVisibleRect(visible))
            val x = visible.left + visible.width() * xFraction
            val startY = visible.top + visible.height() * .4f
            val bottomY = visible.top + visible.height() * .8f
            val endY = visible.top + visible.height() * .2f
            val downTime = SystemClock.uptimeMillis()
            var eventTime = downTime
            fun inject(action: Int, y: Float) {
                // Keep the intended physical velocity even when emulator injection blocks.
                // Real frame waits below ensure every timestamp is already in the past.
                val event = MotionEvent.obtain(downTime, eventTime, action, x, y, 0)
                try { assertTrue(uiController.injectMotionEvent(event)) } finally { event.recycle() }
            }
            inject(MotionEvent.ACTION_DOWN, startY)
            for (step in 1..12) {
                uiController.loopMainThreadForAtLeast(20)
                eventTime += 20
                inject(MotionEvent.ACTION_MOVE, startY + (bottomY - startY) * step / 12)
            }
            beforeReverse()
            for (step in 1..10) {
                uiController.loopMainThreadForAtLeast(10)
                eventTime += 10
                inject(MotionEvent.ACTION_MOVE, bottomY + (endY - bottomY) * step / 10)
            }
            eventTime++
            inject(MotionEvent.ACTION_UP, endY)
        }
    }

    private fun waitForNativeIdle(scenario: FragmentScenario<DataviewScrollHarnessFragment>) {
        await(scenario) { it.nativeList.scrollState == RecyclerView.SCROLL_STATE_IDLE }
    }

    private fun await(
        scenario: FragmentScenario<DataviewScrollHarnessFragment>,
        predicate: (DataviewScrollHarnessFragment) -> Boolean
    ) {
        val deadline = SystemClock.uptimeMillis() + 5_000
        do {
            var satisfied = false
            scenario.onFragment { satisfied = predicate(it) }
            if (satisfied) return
            SystemClock.sleep(16)
        } while (SystemClock.uptimeMillis() < deadline)
        throw AssertionError("Dataview fixture did not reach the expected state within 5 seconds")
    }
}
