package com.anytypeio.anytype.features.sets.dv

import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.anytypeio.anytype.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Tests the integrated production Board, retaining native pointer and animation handling. */
@RunWith(AndroidJUnit4::class)
class DataviewBoardHarnessTest {
    @Test fun repeatedHorizontalSwipeAcceleratesBetweenColumns() = withBoard(0) { scenario ->
        lateinit var fixture: DataviewBoardHarnessFragment
        scenario.onFragment {
            fixture = it
            it.model = it.model.copy(columns = List(12) { index ->
                it.column(0).copy(id = "column-$index", label = "Runtime column $index")
            })
            it.board.setBoard(it.model)
            it.host.coordinator.setExpanded(false)
        }
        textBounds("Runtime column 0")
        perform("swipe again during horizontal board inertia", fixture) { ui, view ->
            val bounds = checkDataviewMain { Rect().also { assertTrue(view.getGlobalVisibleRect(it)) } }
            val x = bounds.left + bounds.width() * .75f
            val y = bounds.top + bounds.height() * .6f
            fun position(): Int = checkDataviewMain {
                requireNotNull(fixture.board.saveScrollState().getBundle(fixture.model.id)?.getBundle("row")).let {
                    val index = it.getInt("index")
                    index * fixture.px(292) + it.getInt("offset") + if (index > 0) fixture.px(16) else 0
                }
            }
            fun flingTravel(): Int {
                val downTime = SystemClock.uptimeMillis()
                fun inject(action: Int, nextX: Float) {
                    val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, nextX, y, 0)
                    try { assertTrue(ui.injectMotionEvent(event)) } finally { event.recycle() }
                }
                inject(MotionEvent.ACTION_DOWN, x)
                repeat(6) { step ->
                    ui.loopMainThreadForAtLeast(16)
                    inject(MotionEvent.ACTION_MOVE, x - fixture.px(100) * (step + 1) / 6f)
                }
                inject(MotionEvent.ACTION_UP, x - fixture.px(100))
                val start = position()
                ui.loopMainThreadForAtLeast(80)
                return position() - start
            }
            val first = flingTravel()
            val second = flingTravel()
            assertTrue("First horizontal swipe must fling: $first", first > fixture.px(10))
            assertTrue("Repeated horizontal swipe must accelerate, first=$first second=$second", second > first * 1.15f)
            assertEquals(fixture.host.coordinator.range, fixture.host.coordinator.offset, 0f)
            assertEquals(0, fixture.persistedMoves)
        }
    }

    @Test fun repeatedSameDirectionSwipeAcceleratesColumn() = withBoard(100) { scenario ->
        lateinit var fixture: DataviewBoardHarnessFragment
        scenario.onFragment { fixture = it; it.host.coordinator.setExpanded(false) }
        val pitch = textBounds("Runtime card A-1").top - textBounds("Runtime card A-0").top
        assertTrue(pitch > 0)
        perform("swipe again during column inertia", fixture) { ui, view ->
            val bounds = checkDataviewMain { Rect().also { assertTrue(view.getGlobalVisibleRect(it)) } }
            val x = bounds.left + fixture.px(130).toFloat()
            val y = bounds.top + bounds.height() * .75f
            fun position(): Int = checkDataviewMain {
                requireNotNull(fixture.anchor()).let { it.getInt("index") * pitch + it.getInt("offset") }
            }
            fun flingTravel(): Int {
                val downTime = SystemClock.uptimeMillis()
                fun inject(action: Int, nextY: Float) {
                    val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, nextY, 0)
                    try { assertTrue(ui.injectMotionEvent(event)) } finally { event.recycle() }
                }
                inject(MotionEvent.ACTION_DOWN, y)
                repeat(6) { step ->
                    ui.loopMainThreadForAtLeast(16)
                    inject(MotionEvent.ACTION_MOVE, y - fixture.px(100) * (step + 1) / 6f)
                }
                inject(MotionEvent.ACTION_UP, y - fixture.px(100))
                val start = position()
                ui.loopMainThreadForAtLeast(80)
                return position() - start
            }
            val first = flingTravel()
            val second = flingTravel()
            assertTrue("First swipe must fling: $first", first > fixture.px(10))
            assertTrue("Repeated swipe must accelerate, first=$first second=$second", second > first * 1.15f)
            assertEquals(0, fixture.persistedMoves)
        }
    }

    @Test fun labelLongHoldThenSwipeStillScrolls() = nonCardHoldScroll("label", count = 100)
    @Test fun gutterLongHoldThenSwipeStillScrolls() = nonCardHoldScroll("gutter", count = 100)
    @Test fun emptyReadOnlyLongHoldThenSwipeStillScrolls() = nonCardHoldScroll("empty", count = 0)
    @Test fun columnBorderLongHoldThenSwipeStillScrolls() = nonCardHoldScroll("border", count = 0)

    private fun nonCardHoldScroll(surface: String, count: Int) = withBoard(count) { scenario ->
        lateinit var fixture: DataviewBoardHarnessFragment
        scenario.onFragment { fixture = it }
        val labelBounds = if (surface == "label") textBounds("Runtime column A") else null
        if (surface == "label") {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                java.io.File(instrumentation.targetContext.cacheDir, "dataview-board-runtime.png").outputStream().use {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                }
                bitmap.recycle()
            }
        }
        perform("hold $surface then swipe", fixture) { ui, view ->
            val bounds = Rect().also { assertTrue(view.getGlobalVisibleRect(it)) }
            val x = labelBounds?.centerX()?.toFloat() ?: if (surface == "gutter") {
                bounds.left + fixture.px(8).toFloat()
            } else if (surface == "border") {
                bounds.left + fixture.px(18).toFloat()
            } else bounds.left + fixture.px(100).toFloat()
            val y = labelBounds?.centerY()?.toFloat() ?: (bounds.top + bounds.height() * .7f)
            pointer(ui, x, y, hold = true, dy = -fixture.px(100).toFloat(), afterHold = {
                assertFalse("A non-card hold must not enter drag mode", fixture.host.coordinator.isBlocked)
            })
            assertTrue("Native swipe after holding $surface must collapse the header", fixture.host.coordinator.offset > 0f)
            assertEquals(0, fixture.persistedMoves)
        }
    }

    @Test fun cardLiftFreezesHeaderAndGeometryChangeCancelsWithoutPersisting() = withBoard(100) { scenario ->
        lateinit var fixture: DataviewBoardHarnessFragment
        scenario.onFragment { fixture = it }
        val card = textBounds("Runtime card A-0")
        perform("lift card, move it, then change host geometry", fixture) { ui, _ ->
            val initial = fixture.host.coordinator.offset
            pointer(ui, card.centerX().toFloat(), card.centerY().toFloat(), hold = true, dy = 90f,
                afterHold = {
                    assertTrue("Stationary card hold must lift", fixture.host.coordinator.isBlocked)
                }, beforeRelease = {
                    assertEquals(initial, fixture.host.coordinator.offset, 0f)
                    checkDataviewMain {
                        fixture.header.layoutParams = fixture.header.layoutParams.apply { height += fixture.px(40) }
                        fixture.host.requestLayout()
                    }
                    ui.loopMainThreadForAtLeast(64)
                    assertFalse("Geometry must cancel the lifted card", fixture.host.coordinator.isBlocked)
                })
            assertEquals(0, fixture.persistedMoves)
        }
    }

    @Test fun paginationRearmsWhenConsecutivePagesStayVisible() = withBoard(1, paginate = true) { scenario ->
        await(scenario) { it.model.columns.single().cards.size == 3 }
        scenario.onFragment { assertEquals(listOf(1, 2), it.pageRequests) }
    }

    @Test fun cardEdgeAutoscrollAndDataEmissionKeepHeaderFrozen() = withBoard(100) { scenario ->
        lateinit var fixture: DataviewBoardHarnessFragment
        scenario.onFragment { fixture = it; it.host.coordinator.restoreProgress(.5f) }
        val card = textBounds("Runtime card A-0")
        perform("drag card to vertical autoscroll edge and cancel", fixture) { ui, view ->
            val bounds = checkDataviewMain { Rect().also { assertTrue(view.getGlobalVisibleRect(it)) } }
            val header = fixture.host.coordinator.offset
            val targetY = bounds.bottom - fixture.px(80).toFloat()
            pointer(ui, card.centerX().toFloat(), card.centerY().toFloat(), hold = true,
                dy = targetY - card.centerY(), cancelRelease = true,
                afterHold = { assertTrue(fixture.host.coordinator.isBlocked) }, beforeRelease = {
                    ui.loopMainThreadForAtLeast(400)
                    checkDataviewMain {
                        assertTrue("Controlled card autoscroll must move the active list", requireNotNull(fixture.anchor()).getInt("index") > 0)
                        fixture.model = fixture.model.copy(title = "Updated runtime board")
                        fixture.board.setBoard(fixture.model)
                    }
                    ui.loopMainThreadForAtLeast(50)
                    assertTrue("Ordinary data emission must preserve the lifted card", fixture.host.coordinator.isBlocked)
                    assertEquals(header, fixture.host.coordinator.offset, 0f)
                })
            assertFalse(fixture.host.coordinator.isBlocked)
            assertEquals(0, fixture.persistedMoves)
        }
    }

    @Test fun clearAndRecreateRestoresStableCardAfterInsertion() = restoreAfterInsertion(false)
    @Test fun pendingLoadingRetainsDeepAnchorUntilMatchingRecordsArrive() = restoreAfterInsertion(true)

    private fun restoreAfterInsertion(withPendingLoading: Boolean) = withBoard(100) { scenario ->
        lateinit var fixture: DataviewBoardHarnessFragment
        scenario.onFragment { fixture = it }
        perform("scroll production board to a nonzero anchor", fixture) { ui, view ->
            val bounds = Rect().also { assertTrue(view.getGlobalVisibleRect(it)) }
            repeat(3) {
                pointer(ui, bounds.left + fixture.px(130).toFloat(), bounds.top + bounds.height() * .8f,
                    hold = false, dy = -bounds.height() * .55f)
                ui.loopMainThreadForAtLeast(300)
            }
        }
        var savedId: String? = null
        var savedOffset = 0
        var header = 0f
        lateinit var originalModel: com.anytypeio.anytype.presentation.sets.model.Viewer.Board
        scenario.onFragment {
            val anchor = requireNotNull(it.anchor())
            savedId = anchor.getString("id")
            assertNotNull("Fixture must reach a stable card anchor", savedId)
            savedOffset = anchor.getInt("offset")
            header = it.host.coordinator.offset
            originalModel = it.model
        }
        // LazyList's first anchor may be occluded by the sticky label. Compare a following
        // fully visible card's actual screen geometry as well as the exact stored anchor.
        val cards = originalModel.columns.single().cards
        val visibleId = cards[cards.indexOfFirst { it.objectId == savedId } + 2].objectId
        val beforeBounds = textBounds("Runtime card $visibleId")
        scenario.onFragment { it.board.clear() }
        SystemClock.sleep(100)
        if (withPendingLoading) {
            scenario.onFragment {
                it.model = originalModel.copy(columns = listOf(originalModel.columns.single().copy(
                    cards = emptyList(), count = 0, hasLoadedRecords = false)))
                it.board.setBoard(it.model)
            }
            SystemClock.sleep(150)
            scenario.onFragment { assertEquals(savedId, it.anchor()?.getString("id")) }
            scenario.onFragment {
                it.model = originalModel.copy(columns = listOf(originalModel.columns.single().copy(
                    cards = originalModel.columns.single().cards.take(2), count = 100, hasLoadedRecords = true)))
                it.board.setBoard(it.model)
            }
            SystemClock.sleep(150)
            scenario.onFragment { assertEquals(savedId, it.anchor()?.getString("id")) }
        }
        scenario.onFragment {
            val old = originalModel.columns.single()
            it.model = originalModel.copy(columns = listOf(old.copy(cards = listOf(it.card("inserted")) + old.cards, count = old.count + 1)))
            it.board.setBoard(it.model)
        }
        await(scenario) { it.anchor()?.getString("id") == savedId }
        val restoredBounds = textBounds("Runtime card $visibleId")
        assertEquals("Stable visible card must retain its physical position", beforeBounds.top, restoredBounds.top)
        scenario.onFragment {
            assertEquals(savedOffset, requireNotNull(it.anchor()).getInt("offset"))
            assertEquals(header, it.host.coordinator.offset, 0f)
        }
    }

    private fun textBounds(text: String): Rect {
        val deadline = SystemClock.uptimeMillis() + 5_000
        do {
            // Compose exposes virtual descendants, but the platform text-search shortcut can
            // return no results for them. Traverse the actual accessibility tree instead.
            fun find(node: android.view.accessibility.AccessibilityNodeInfo?): android.view.accessibility.AccessibilityNodeInfo? {
                if (node == null) return null
                if (node.isVisibleToUser && text in node.text?.toString().orEmpty().lines()) return node
                for (index in 0 until node.childCount) find(node.getChild(index))?.let { return it }
                return null
            }
            val match = find(InstrumentationRegistry.getInstrumentation().uiAutomation.rootInActiveWindow)
            if (match != null) return Rect().also { match.getBoundsInScreen(it) }
            SystemClock.sleep(50)
        } while (SystemClock.uptimeMillis() < deadline)
        fun describe(node: android.view.accessibility.AccessibilityNodeInfo?, depth: Int = 0): String {
            if (node == null || depth > 12) return ""
            return "${node.className} text=${node.text} desc=${node.contentDescription} visible=${node.isVisibleToUser}\n" +
                (0 until node.childCount).joinToString("") { describe(node.getChild(it), depth + 1) }
        }
        throw AssertionError("No visible accessibility node for $text\n${describe(InstrumentationRegistry.getInstrumentation().uiAutomation.rootInActiveWindow)}")
    }

    private fun pointer(ui: UiController, x: Float, y: Float, hold: Boolean, dy: Float, cancelRelease: Boolean = false,
        afterHold: () -> Unit = {}, beforeRelease: () -> Unit = {}) {
        val downTime = SystemClock.uptimeMillis()
        fun inject(action: Int, nextY: Float) {
            val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, nextY, 0)
            try { assertTrue(ui.injectMotionEvent(event)) } finally { event.recycle() }
        }
        inject(MotionEvent.ACTION_DOWN, y)
        ui.loopMainThreadForAtLeast(if (hold) ViewConfiguration.getLongPressTimeout().toLong() + 100 else 20)
        afterHold()
        for (step in 1..12) {
            ui.loopMainThreadForAtLeast(30)
            inject(MotionEvent.ACTION_MOVE, y + dy * step / 12)
        }
        beforeRelease()
        inject(if (cancelRelease) MotionEvent.ACTION_CANCEL else MotionEvent.ACTION_UP, y + dy)
        ui.loopMainThreadForAtLeast(100)
    }

    private fun perform(description: String, fixture: DataviewBoardHarnessFragment, block: (UiController, View) -> Unit) {
        performDataviewNativeAction(fixture.board, object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription() = description
            override fun perform(uiController: UiController, view: View) = block(uiController, view)
        })
    }

    private fun withBoard(count: Int, paginate: Boolean = false, block: (FragmentScenario<DataviewBoardHarnessFragment>) -> Unit) {
        val scenario = launchFragmentInContainer<DataviewBoardHarnessFragment>(
            fragmentArgs = Bundle().apply {
                putInt(DataviewBoardHarnessFragment.ARG_COUNT, count)
                putBoolean(DataviewBoardHarnessFragment.ARG_PAGINATE, paginate)
            }, themeResId = R.style.AppTheme)
        try {
            await(scenario) { it.board.height > 0 && it.anchor() != null }
            // Keep system animations enabled, but let the activity's opening transition finish
            // before sampling physical screen coordinates or taking the fixture screenshot.
            SystemClock.sleep(500)
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            block(scenario)
        } finally { scenario.close() }
    }

    private fun await(scenario: FragmentScenario<DataviewBoardHarnessFragment>, predicate: (DataviewBoardHarnessFragment) -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 5_000
        do {
            var ready = false
            scenario.onFragment { ready = predicate(it) }
            if (ready) return
            SystemClock.sleep(16)
        } while (SystemClock.uptimeMillis() < deadline)
        throw AssertionError("Production Board fixture did not reach expected state")
    }
}
