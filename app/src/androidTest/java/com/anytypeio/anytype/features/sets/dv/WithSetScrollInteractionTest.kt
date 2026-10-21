package com.anytypeio.anytype.features.sets.dv

import android.graphics.Rect
import android.os.SystemClock
import android.view.MotionEvent
import android.view.KeyEvent
import android.view.ViewConfiguration
import android.os.Bundle
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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.never
import kotlinx.coroutines.runBlocking
import com.anytypeio.anytype.core_models.Payload
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.stub

import androidx.fragment.app.testing.launchFragmentInContainer
/** Actual WithSetScreen hosting real native/Compose dataviews. */
@RunWith(AndroidJUnit4::class)
class WithSetScrollInteractionTest : TestObjectSetSetup() {
    override val title = Block(id = "runtime-native-title", content = StubTextContent(
        style = Block.Content.Text.Style.TITLE, text = "Hidden embedded title", marks = emptyList()),
        children = emptyList(), fields = Block.Fields.empty())
    @Before override fun setup() = super.setup()
    @Test fun gridCollapsesTypeHeaderBeforeRows() = typeSwipe(Block.Content.DataView.Viewer.Type.GRID)
    @Test fun listCollapsesTypeHeaderBeforeRows() = typeSwipe(Block.Content.DataView.Viewer.Type.LIST)
    @Test fun galleryCollapsesTypeHeaderBeforeRows() = typeSwipe(Block.Content.DataView.Viewer.Type.GALLERY)
    @Test fun boardCollapsesTypeHeaderBeforeCards() = typeSwipe(Block.Content.DataView.Viewer.Type.BOARD)

    @Test fun largeGalleryCollapsesTypeHeaderBeforeRows() = typeSwipe(Block.Content.DataView.Viewer.Type.GALLERY, large = true)
    @Test fun gridDescriptionEditingAndBackReleaseOuterHeader() = typeSwipe(Block.Content.DataView.Viewer.Type.GRID, edit = true)
    @Test fun boardDescriptionEditingAndBackReleaseOuterHeader() = typeSwipe(Block.Content.DataView.Viewer.Type.BOARD, edit = true)
    @Test fun gridRecreationRestoresOuterHeaderAndRows() = typeSwipe(Block.Content.DataView.Viewer.Type.GRID, recreate = true)
    @Test fun boardRecreationRestoresOuterHeaderAndCards() = typeSwipe(Block.Content.DataView.Viewer.Type.BOARD, recreate = true)

    private fun typeSwipe(type: Block.Content.DataView.Viewer.Type, large: Boolean = false, edit: Boolean = false, recreate: Boolean = false) {
        repo.stub { onBlocking { setObjectDetail(any(), any(), anyOrNull()) } doReturn Payload(ctx, emptyList()) }
        val boardMode = type == Block.Content.DataView.Viewer.Type.BOARD
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
            type = type, cardSize = if (large) Block.Content.DataView.Viewer.Size.LARGE else Block.Content.DataView.Viewer.Size.SMALL,
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

        val scenario = launchFragmentInContainer<WithSetScrollTestFragment>(
            fragmentArgs = bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to ctx, ObjectSetFragment.SPACE_ID_KEY to defaultSpace),
            themeResId = R.style.AppTheme)
        try {
            lateinit var fragment: ObjectSetFragment
            val deadline = SystemClock.uptimeMillis() + 10_000
            var ready = false
            while (!ready && SystemClock.uptimeMillis() < deadline) {
                scenario.onFragment { outer -> outer.childFragmentManager.fragments.filterIsInstance<ObjectSetFragment>().firstOrNull()?.let { fragment = it; ready = true } }
                SystemClock.sleep(32)
            }
            lateinit var host: DataviewScrollHost
            lateinit var target: View
            lateinit var screen: View
            fun bind() = scenario.onFragment { outer ->
                fragment = outer.dataview()
                screen = fragment.requireView()
                host = screen.findViewById(R.id.scrollHost)
                target = screen.findViewById(when (type) {
                    Block.Content.DataView.Viewer.Type.LIST -> R.id.listView
                    Block.Content.DataView.Viewer.Type.GALLERY -> R.id.galleryView
                    Block.Content.DataView.Viewer.Type.BOARD -> R.id.boardView
                    else -> R.id.rvRows
                })
            }
            bind()
            fun contentPosition(): String = checkDataviewMain {
                if (target is RecyclerView) (target as RecyclerView).computeVerticalScrollOffset().toString()
                else boardPosition((target as BoardViewWidget).saveScrollState())
            }
            fun offset() = checkDataviewMain { host.coordinator.offset }
            fun range() = checkDataviewMain { host.coordinator.range }
            fun bounds() = checkDataviewMain { Rect().also { assertTrue(target.getGlobalVisibleRect(it)) } }
            fun gesture(dy: Float) {
                val visible = bounds()
                val travel = dy.coerceIn(-visible.height() * .6f, visible.height() * .6f)
                swipe(visible.left + if (boardMode) 100 * target.resources.displayMetrics.density else visible.width() * .5f,
                    if (travel < 0) visible.bottom - visible.height() * .15f else visible.top + visible.height() * .15f, travel)
                SystemClock.sleep(80)
            }
            while ((if (boardMode) textBounds("Runtime row 0").isEmpty() else checkDataviewMain { ((target as RecyclerView).adapter?.itemCount ?: 0) < 20 }) && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(32)
            SystemClock.sleep(750)
            assertTrue("Actual type dataview records must load", if (boardMode) textBounds("Runtime row 0").isNotEmpty() else checkDataviewMain { (target as RecyclerView).adapter!!.itemCount >= 20 })
            await("Actual type header semantics must be ready") { textBounds("Runtime Leads").size >= 2 && textBounds("Properties").isNotEmpty() }
            if (type == Block.Content.DataView.Viewer.Type.GRID && !edit && !recreate) saveScreenshot("type-grid-expanded")
            val navBefore = textBounds("Back button").firstOrNull() ?: error("Missing real type navigation title")
            val titleBefore = textBounds("Runtime Leads").maxByOrNull { it.top } ?: error("Missing real type header title")
            val propertiesBefore = textBounds("Properties").firstOrNull() ?: error("Missing real type properties button")
            checkDataviewMain {
                assertFalse("Duplicate native object title must be hidden", screen.findViewById<View>(R.id.tvSetTitle).isShown)
                assertFalse("Duplicate native object toolbar must be hidden", screen.findViewById<View>(R.id.topToolbar).isShown)
            }
            if (edit) {
                val description = textBounds("Type description for native scroll validation").first()
                tap(description.centerX().toFloat(), description.centerY().toFloat())
                await("Visible IME and blocked external header") { checkDataviewMain { ViewCompat.getRootWindowInsets(screen)?.isVisible(WindowInsetsCompat.Type.ime()) == true && host.coordinator.isBlocked } }
                awaitDataviewImeWindow(visible = true)
                if (boardMode) {
                    val visible = bounds().also { rect -> checkDataviewMain {
                        val metrics = android.util.DisplayMetrics()
                        (screen.context.getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay.getRealMetrics(metrics)
                        rect.bottom = minOf(rect.bottom, metrics.heightPixels - (ViewCompat.getRootWindowInsets(screen)?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0))
                    } }
                    val movement = visible.height() * .45f
                    assertTrue("Editing content gesture exceeds slop", movement > ViewConfiguration.get(target.context).scaledTouchSlop * 2)
                    val contentBefore = contentPosition()
                    swipe(visible.left + if (boardMode) 100 * target.resources.displayMetrics.density else visible.width() * .5f,
                        visible.bottom - visible.height() * .15f, -movement)
                    SystemClock.sleep(100)
                    assertNotEquals("Native records must scroll while outer description edits", contentBefore, contentPosition())
                    assertEquals("Outer header frozen while IME visible", 0f, offset(), 0f)
                    assertTrue(checkDataviewMain { ViewCompat.getRootWindowInsets(screen)?.isVisible(WindowInsetsCompat.Type.ime()) == true })
                }
                DataviewNativeInput.injectKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
                DataviewNativeInput.injectKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK))
                await("Native BACK must release external editing block") { checkDataviewMain { ViewCompat.getRootWindowInsets(screen)?.isVisible(WindowInsetsCompat.Type.ime()) == false && !host.coordinator.isBlocked } }
                awaitDataviewImeWindow(visible = false)
                gesture(-180f)
                assertTrue("Type header collapses after description BACK", offset() > 0)
                return
            }
            val initialContent = contentPosition()
            val initialOffset = offset()
            gesture(-minOf(220f, range().takeIf { it > 0 }?.times(.45f) ?: 220f))
            assertTrue("Actual type header must collapse; range=${range()} before=$initialOffset after=${offset()} content=$initialContent→${contentPosition()}", offset() > initialOffset)
            assertTrue("First gesture must stop before full collapse", offset() < range())
            assertEquals("Header consumes before native content", initialContent, contentPosition())
            val titleAfter = textBounds("Runtime Leads").maxByOrNull { it.top }
            assertTrue("Visible outer type title must move upward", titleAfter == null || titleAfter.top < titleBefore.top)
            val propertiesAfter = textBounds("Properties").firstOrNull()
            assertTrue("Visible outer buttons must move upward", propertiesAfter == null || propertiesAfter.top < propertiesBefore.top)
            assertEquals("Outer navigation stays pinned", navBefore, textBounds("Back button").firstOrNull())
            repeat(4) { if (offset() < range()) gesture(-bounds().height() * .55f) }
            assertEquals("Type header completely collapses", range(), offset(), 1f)
            await("Collapsed type properties must leave accessibility tree") { textBounds("Properties").isEmpty() }
            assertTrue("Collapsed type properties are hidden: bounds=${textBounds("Properties")} range=${range()} host=" + checkDataviewMain { Rect().also(host::getGlobalVisibleRect).toString() }, textBounds("Properties").isEmpty())
            await("Outer navigation remains visible at full collapse") { textBounds("Back button").firstOrNull() == navBefore }
            val pinnedControls = checkDataviewMain { Rect().also { screen.findViewById<View>(R.id.dataViewHeader).getGlobalVisibleRect(it) } }
            assertEquals("Collapsed controls pin directly below outer navigation", checkDataviewMain { Rect().also(screen::getGlobalVisibleRect).top }, pinnedControls.top)
            if (!recreate) saveScreenshot("type-${type.name.lowercase()}-collapsed")
            val contentAtCollapse = contentPosition()
            gesture(-300f)
            assertNotEquals("After collapse records advance", contentAtCollapse, contentPosition())
            assertEquals("Dataview controls stay pinned", pinnedControls, checkDataviewMain { Rect().also { screen.findViewById<View>(R.id.dataViewHeader).getGlobalVisibleRect(it) } })
            if (recreate) {
                val savedContent = contentPosition()
                scenario.recreate()
                SystemClock.sleep(1000)
                bind()
                await("Recreated type header restores collapse") { kotlin.math.abs(offset() - range()) < 1f && range() > 0f }
                await("Recreated native content restores its anchor") { contentPosition() == savedContent }
                runBlocking { verify(repo, never()).setObjectDetail(any(), any(), anyOrNull()) }
                await("Recreated outer navigation stays visible and pinned") { textBounds("Back button").firstOrNull() == navBefore }
                assertFalse("Recreation must retain hidden duplicate title", checkDataviewMain { screen.findViewById<View>(R.id.tvSetTitle).isShown })
            }
            val deepContent = contentPosition()
            gesture(120f)
            assertNotEquals("Reverse first consumes scrolled content", deepContent, contentPosition())
            assertEquals("Header remains collapsed while content returns", range(), offset(), 1f)
            repeat(12) { if (offset() > 0f) gesture(bounds().height() * .55f) }
            assertEquals("Top boundary expands outer header", 0f, offset(), 1f)
            assertEquals("Outer title returns to expanded geometry", titleBefore, textBounds("Runtime Leads").maxByOrNull { it.top })
            assertEquals("Outer buttons return to expanded geometry", propertiesBefore, textBounds("Properties").firstOrNull())
        } finally { scenario.close() }
    }

    private fun swipe(x: Float, y: Float, deltaY: Float) {
        val down = SystemClock.uptimeMillis()
        for (step in 0..10) {
            val event = MotionEvent.obtain(down, SystemClock.uptimeMillis(), if (step == 0) MotionEvent.ACTION_DOWN else MotionEvent.ACTION_MOVE, x, y + deltaY * step / 10, 0)
            try { injectFinger(event) } finally { event.recycle() }
            SystemClock.sleep(32)
        }
        val cancel = MotionEvent.obtain(down, SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, x, y + deltaY, 0)
        try { injectFinger(cancel) } finally { cancel.recycle() }
    }
    private fun await(message: String, predicate: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 5_000
        while (!predicate() && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(32)
        assertTrue(message, predicate())
    }

    private fun saveScreenshot(name: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()?.let { bitmap ->
            java.io.File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "dataview-$name.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    private fun textBounds(text: String): List<Rect> {
        val result = mutableListOf<Rect>()
        fun visit(node: android.view.accessibility.AccessibilityNodeInfo?) {
            if (node == null) return
            if (node.isVisibleToUser && (node.text?.toString()?.contains(text) == true || node.contentDescription?.toString()?.contains(text) == true)) {
                Rect().also { node.getBoundsInScreen(it); if (!it.isEmpty) result += it }
            }
            repeat(node.childCount) { visit(node.getChild(it)) }
        }
        visit(InstrumentationRegistry.getInstrumentation().uiAutomation.rootInActiveWindow)
        return result.distinct()
    }

    private fun tap(x: Float, y: Float) {
        val down = SystemClock.uptimeMillis()
        for (action in listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP)) {
            val event = MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, x, y, 0)
            try { injectFinger(event) } finally { event.recycle() }
            SystemClock.sleep(32)
        }
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
