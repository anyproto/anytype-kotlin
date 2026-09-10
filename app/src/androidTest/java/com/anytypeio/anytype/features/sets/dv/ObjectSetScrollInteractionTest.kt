package com.anytypeio.anytype.features.sets.dv

import android.graphics.Rect
import android.os.SystemClock
import android.view.MotionEvent
import android.view.KeyEvent
import android.view.ViewConfiguration
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.DataviewScrollHost
import com.anytypeio.anytype.core_ui.widgets.dv.board.BoardViewWidget
import androidx.test.platform.app.InstrumentationRegistry
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.stub

/** Native input through the real ObjectSetFragment, including focus and toolbar wiring. */
@RunWith(AndroidJUnit4::class)
class ObjectSetScrollInteractionTest : TestObjectSetSetup() {
    override val title = Block(
        id = "runtime-title",
        content = StubTextContent(style = Block.Content.Text.Style.TITLE,
            text = "Example object\nLong title\nScroll check", marks = emptyList()),
        children = emptyList(), fields = Block.Fields.empty()
    )

    @Before override fun setup() = super.setup()

    @Test fun gridNativeSwipeCollapsesActualObjectHeader() = actualSwipe(false)
    @Test fun gridNativeSwipeAfterKeyboardDismissalCollapsesActualObjectHeader() = actualSwipe(true)
    @Test fun boardNativeSwipeCollapsesActualObjectHeader() = actualSwipe(false, boardMode = true)
    @Test fun boardNativeSwipeAfterKeyboardDismissalCollapsesActualObjectHeader() = actualSwipe(true, boardMode = true)
    @Test fun gridNativeSwipeAfterTitleDoneCollapsesActualObjectHeader() = actualSwipe(true, done = true)
    @Test fun boardNativeSwipeAfterTitleDoneCollapsesActualObjectHeader() = actualSwipe(true, boardMode = true, done = true)

    private fun actualSwipe(editThenDismiss: Boolean, boardMode: Boolean = false, done: Boolean = false) {
        fieldParser.stub {
            on { getObjectName(any<ObjectWrapper.Basic>(), any()) } doAnswer {
                it.getArgument<ObjectWrapper.Basic>(0).name.orEmpty()
            }
        }
        val relation = Relation(key = Relations.NAME, name = "Name",
            format = Relation.Format.SHORT_TEXT, source = Relation.Source.values().first())
        val groupRelation = Relation(key = "runtime-done", name = "Done",
            format = Relation.Format.CHECKBOX, source = Relation.Source.values().first())
        val relations = if (boardMode) listOf(relation, groupRelation) else listOf(relation)
        stubRelations(relations)
        val viewer = Block.Content.DataView.Viewer(id = "runtime-grid", name = "Runtime Grid",
            filters = emptyList(), sorts = emptyList(),
            viewerRelations = listOf(DVViewerRelation(Relations.NAME, isVisible = true)),
            type = if (boardMode) Block.Content.DataView.Viewer.Type.BOARD else Block.Content.DataView.Viewer.Type.GRID,
            groupRelationKey = if (boardMode) groupRelation.key else null)
        val dataview = Block(id = "runtime-dataview", children = emptyList(), fields = Block.Fields.empty(),
            content = Block.Content.DataView(viewers = listOf(viewer),
                relationLinks = relations.map { RelationLink(it.key, it.format) }))
        val root = Block(id = ctx, fields = Block.Fields.empty(), content = Block.Content.Smart,
            children = listOf(header.id, dataview.id))
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSubscriptionEventChannel()
        if (boardMode) stubSingleBoardCheckboxGroup()
        stubSearchWithSubscription((0 until 50).map { index -> ObjectWrapper.Basic(mapOf(
            Relations.ID to "runtime-record-$index", Relations.NAME to "Runtime row $index",
            Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble())) })
        stubOpenObjectSetWithRecord(listOf(root, header, title, dataview), defaultDetails)
        val scenario = launchFragment(bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to ctx))
        try {
            lateinit var host: DataviewScrollHost
            lateinit var rows: RecyclerView
            lateinit var screen: View
            lateinit var target: View
            scenario.onFragment {
                screen = it.requireView()
                host = screen.findViewById(R.id.scrollHost)
                rows = screen.findViewById(R.id.rvRows)
                target = if (boardMode) screen.findViewById<BoardViewWidget>(R.id.boardView) else rows
            }
            val deadline = SystemClock.uptimeMillis() + 5_000
            fun boardTree(): String {
                fun describe(node: android.view.accessibility.AccessibilityNodeInfo?): String {
                    if (node == null) return ""
                    return "${node.text}|${node.contentDescription}\n" + (0 until node.childCount).joinToString("") { describe(node.getChild(it)) }
                }
                return describe(InstrumentationRegistry.getInstrumentation().uiAutomation.rootInActiveWindow)
            }
            fun boardHasCard(): Boolean {
                fun find(node: android.view.accessibility.AccessibilityNodeInfo?): Boolean {
                    if (node == null) return false
                    if (node.text?.toString() == "Runtime row 0") return true
                    return (0 until node.childCount).any { find(node.getChild(it)) }
                }
                return find(InstrumentationRegistry.getInstrumentation().uiAutomation.rootInActiveWindow)
            }
            while ((if (boardMode) !boardHasCard() else checkDataviewMain { (rows.adapter?.itemCount ?: 0) < 20 }) && SystemClock.uptimeMillis() < deadline) {
                SystemClock.sleep(32)
            }
            SystemClock.sleep(500) // Keep animations enabled and allow activity entry to settle.
            if (boardMode) assertTrue("Actual Board must contain its subscribed records. Tree: ${boardTree()}", boardHasCard())
            if (editThenDismiss) {
                val titleBounds = checkDataviewMain { Rect().also { assertTrue(screen.findViewById<View>(R.id.tvSetTitle).getGlobalVisibleRect(it)) } }
                val tapTime = SystemClock.uptimeMillis()
                for (action in listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP)) {
                    val event = MotionEvent.obtain(tapTime, SystemClock.uptimeMillis(), action,
                        titleBounds.centerX().toFloat(), titleBounds.centerY().toFloat(), 0)
                    try { injectFinger(event) } finally { event.recycle() }
                    SystemClock.sleep(32)
                }
                val imeDeadline = SystemClock.uptimeMillis() + 3_000
                while (!checkDataviewMain { ViewCompat.getRootWindowInsets(screen)?.isVisible(WindowInsetsCompat.Type.ime()) == true } && SystemClock.uptimeMillis() < imeDeadline) SystemClock.sleep(32)
                SystemClock.sleep(500) // Visibility becomes true at animation start; sample only settled keyboard geometry.
                checkDataviewMain { assertTrue("Native title tap must enter editing", screen.findViewById<View>(R.id.tvSetTitle).hasFocus()) }
                checkDataviewMain {
                    assertTrue("Editing guard must be verified with an actually visible IME", ViewCompat.getRootWindowInsets(screen)?.isVisible(WindowInsetsCompat.Type.ime()) == true)
                    assertTrue("Visible-IME editing must block header scrolling", host.coordinator.isBlocked)
                }
                awaitDataviewImeWindow(visible = true)
                if (done) {
                    val activeBounds = checkDataviewMain { Rect().also {
                        assertTrue(target.getGlobalVisibleRect(it))
                        val windowVisible = Rect().also(screen::getWindowVisibleDisplayFrame)
                        assertTrue("Native editing gesture must stay above keyboard", it.intersect(windowVisible))
                        val metrics = android.util.DisplayMetrics()
                        (screen.context.getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay.getRealMetrics(metrics)
                        val keyboardTop = metrics.heightPixels - (ViewCompat.getRootWindowInsets(screen)?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0)
                        it.bottom = minOf(it.bottom, keyboardTop)
                        val toolbarBounds = Rect()
                        val toolbar = screen.findViewById<View>(R.id.titleWidget)
                        if (toolbar.getGlobalVisibleRect(toolbarBounds) && Rect.intersects(it, toolbarBounds)) {
                            it.bottom = minOf(it.bottom, toolbarBounds.top)
                        }
                        assertTrue("Keyboard must leave a visible native viewport: $it", it.height() > 50)
                    } }
                    val activeX = activeBounds.left + if (boardMode) 100 * target.resources.displayMetrics.density else activeBounds.width() * .5f
                    val activeY = activeBounds.top + activeBounds.height() * .8f
                    assertTrue("Active editing gesture must exceed native touch slop",
                        activeBounds.height() * .35f > ViewConfiguration.get(target.context).scaledTouchSlop * 2)
                    fun contentPosition(): String = checkDataviewMain {
                        if (boardMode) boardPosition((target as BoardViewWidget).saveScrollState())
                        else rows.computeVerticalScrollOffset().toString()
                    }
                    val contentBefore = contentPosition()
                    assertTrue("Active content must have a measured scroll anchor", contentBefore.isNotEmpty())
                    val activeDown = SystemClock.uptimeMillis()
                    for (step in 0..6) {
                        val event = MotionEvent.obtain(activeDown, SystemClock.uptimeMillis(),
                            if (step == 0) MotionEvent.ACTION_DOWN else MotionEvent.ACTION_MOVE,
                            activeX, activeY - activeBounds.height() * .35f * step / 6, 0)
                        try { injectFinger(event) } finally { event.recycle() }
                        SystemClock.sleep(32)
                    }
                    val cancel = MotionEvent.obtain(activeDown, SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL,
                        activeX, activeY - activeBounds.height() * .35f, 0)
                    try { injectFinger(cancel) } finally { cancel.recycle() }
                    SystemClock.sleep(64)
                    checkDataviewMain {
                        assertTrue("Native content gesture must keep IME visible", ViewCompat.getRootWindowInsets(screen)?.isVisible(WindowInsetsCompat.Type.ime()) == true)
                        assertEquals("Header must remain expanded during active native editing gesture", 0f, host.coordinator.offset, 0f)
                    }
                    assertNotEquals("Native gesture must actually scroll active content while header is frozen; start=$activeBounds end=" + checkDataviewMain {
                        "${Rect().also(target::getGlobalVisibleRect)} toolbar=${Rect().also(screen.findViewById<View>(R.id.titleWidget)::getGlobalVisibleRect)}"
                    }, contentBefore, contentPosition())
                }
                if (done) {
                    // Dispatch the Android IME action through the real editor InputConnection.
                    checkDataviewMain {
                        val input = screen.findViewById<View>(R.id.tvSetTitle).onCreateInputConnection(EditorInfo())
                        assertTrue(requireNotNull(input).performEditorAction(EditorInfo.IME_ACTION_DONE))
                    }
                } else {
                    DataviewNativeInput.injectKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
                    DataviewNativeInput.injectKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK))
                }
                val dismissedDeadline = SystemClock.uptimeMillis() + 5_000
                fun editEnded() = checkDataviewMain {
                    ViewCompat.getRootWindowInsets(screen)?.isVisible(WindowInsetsCompat.Type.ime()) == false &&
                        !screen.findViewById<View>(R.id.tvSetTitle).hasFocus() && !host.coordinator.isBlocked
                }
                while (!editEnded() && SystemClock.uptimeMillis() < dismissedDeadline) SystemClock.sleep(32)
                assertTrue("Dismissal must hide IME, clear title focus and unblock header: " + checkDataviewMain {
                    "ime=${ViewCompat.getRootWindowInsets(screen)?.isVisible(WindowInsetsCompat.Type.ime())} titleFocus=${screen.findViewById<View>(R.id.tvSetTitle).hasFocus()} blocked=${host.coordinator.isBlocked}"
                }, editEnded())
                awaitDataviewImeWindow(visible = false)
            }
            fun state(): String = checkDataviewMain {
                val focus = screen.findFocus()
                "range=${host.coordinator.range} offset=${host.coordinator.offset} blocked=${host.coordinator.isBlocked} " +
                    "embedded=${host.embeddedMode} pin=${host.pinHeight} host=${host.width}x${host.height} " +
                    "header=${host.getChildAt(0).height} rows=${rows.adapter?.itemCount} rowOffset=${rows.computeVerticalScrollOffset()} " +
                    "focus=${focus?.javaClass?.simpleName}:${focus?.id} titleFocus=${screen.findViewById<View>(R.id.tvSetTitle).hasFocus()} " +
                    "titleEnabled=${screen.findViewById<View>(R.id.tvSetTitle).isEnabled} descriptionFocus=${screen.findViewById<View>(R.id.tvSetDescription).hasFocus()} " +
                    "ime=${ViewCompat.getRootWindowInsets(screen)?.isVisible(WindowInsetsCompat.Type.ime())} " +
                    "nestedEnabled=${rows.isNestedScrollingEnabled} eligible=${host.eligibleNestedScrollTarget?.invoke(rows)}"
            }
            val before = state()
            android.util.Log.i("DataviewActualScreen", "BEFORE $before")
            checkDataviewMain { assertTrue(before, host.coordinator.range > 0f); if (!boardMode) assertTrue(before, rows.adapter!!.itemCount >= 20) }
            checkDataviewMain { assertEquals("Actual object header must start expanded", 0f, host.coordinator.offset, 0f) }
            val visible = checkDataviewMain { Rect().also { assertTrue(target.getGlobalVisibleRect(it)) } }
            val x = if (boardMode) visible.left + 100 * target.resources.displayMetrics.density else visible.centerX().toFloat()
            val y = visible.top + visible.height() * .8f
            val down = SystemClock.uptimeMillis()
            fun inject(action: Int, delta: Float) {
                val event = MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, x, y - delta, 0)
                try { injectFinger(event) } finally { event.recycle() }
                SystemClock.sleep(32)
            }
            inject(MotionEvent.ACTION_DOWN, 0f)
            for (step in 1..10) inject(MotionEvent.ACTION_MOVE, visible.height() * .5f * step / 10)
            android.util.Log.i("DataviewActualScreen", "HELD parent=${checkDataviewMain { rows.hasNestedScrollingParent(ViewCompat.TYPE_TOUCH) }} ${state()}")
            inject(MotionEvent.ACTION_CANCEL, visible.height() * .5f)
            val after = state()
            android.util.Log.i("DataviewActualScreen", "AFTER $after")
            checkDataviewMain { assertTrue("Actual dataview swipe must collapse header. Before: $before After: $after", host.coordinator.offset > 0f) }
            if (editThenDismiss && !done) {
                val instrumentation = InstrumentationRegistry.getInstrumentation()
                SystemClock.sleep(100)
                instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                    val kind = if (boardMode) "board" else "grid"
                    java.io.File(instrumentation.targetContext.cacheDir, "dataview-actual-$kind-collapsed.png").outputStream().use {
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                    }
                    bitmap.recycle()
                }
            }
        } finally { scenario.close() }
    }

    private fun injectFinger(event: MotionEvent) {
        val properties = Array(event.pointerCount) { index ->
            MotionEvent.PointerProperties().also { event.getPointerProperties(index, it); it.toolType = MotionEvent.TOOL_TYPE_FINGER }
        }
        val coordinates = Array(event.pointerCount) { index -> MotionEvent.PointerCoords().also { event.getPointerCoords(index, it) } }
        val finger = MotionEvent.obtain(event.downTime, event.eventTime, event.action, event.pointerCount,
            properties, coordinates, event.metaState, event.buttonState, event.xPrecision, event.yPrecision,
            event.deviceId, event.edgeFlags, android.view.InputDevice.SOURCE_TOUCHSCREEN, event.flags)
        try { DataviewNativeInput.injectMotionEvent(finger) } finally { finger.recycle() }
    }

    private fun boardPosition(bundle: Bundle): String = bundle.keySet().sorted().joinToString("") { key ->
        when (val value = bundle.get(key)) {
            is Bundle -> "$key[${boardPosition(value)}]"
            else -> if (key in setOf("id", "index", "offset")) "$key=$value;" else ""
        }
    }

}
