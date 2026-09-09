package com.anytypeio.anytype.core_ui.widgets.dv.scroll

import android.content.Context
import android.app.Activity
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.InputDevice
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DataviewScrollHostTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val host = DataviewScrollHost(context).apply {
        pinHeight = 44
        addView(View(context), ViewGroup.LayoutParams(-1, 244))
        addView(View(context), ViewGroup.LayoutParams(-1, 40))
        addView(FrameLayout(context), ViewGroup.LayoutParams(-1, -1))
    }
    private fun layout() {
        host.measure(View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY))
        host.layout(0, 0, 400, 800)
    }
    private fun recycler() = RecyclerView(context).apply { layoutManager = LinearLayoutManager(context) }

    @Test fun `viewer is measured at its actual visible height at partial collapse`() {
        layout()
        assertEquals(200f, host.coordinator.range)
        assertEquals(516, host.getChildAt(2).measuredHeight)
        host.coordinator.restoreProgress(.5f)
        layout()
        assertEquals(184, host.getChildAt(2).top)
        assertEquals(616, host.getChildAt(2).measuredHeight)
        assertEquals(800, host.getChildAt(2).bottom)
    }

    @Test fun `Parent3 accumulates actual residual and never X for both input types`() {
        layout()
        val rows = recycler()
        (host.getChildAt(2) as FrameLayout).addView(rows)
        for (type in listOf(ViewCompat.TYPE_TOUCH, ViewCompat.TYPE_NON_TOUCH)) {
            host.coordinator.restoreProgress(.2f)
            host.onNestedScrollAccepted(rows, rows, ViewCompat.SCROLL_AXIS_VERTICAL, type)
            val result = intArrayOf(11, 7)
            host.onNestedPreScroll(rows, 100, -70, result, type)
            assertEquals(listOf(11, 7), result.toList())
            host.onNestedScroll(rows, 0, 0, 100, -70, type, result)
            assertEquals(listOf(11, -33), result.toList())
            assertEquals(0f, host.coordinator.offset)
        }
    }

    @Test fun `table rows dispatch vertically through horizontal wrapper`() {
        layout()
        val wrapper = HorizontalScrollView(context)
        val rows = recycler()
        wrapper.addView(rows)
        (host.getChildAt(2) as FrameLayout).addView(wrapper)
        assertTrue(rows.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH))
        val result = IntArray(2)
        assertTrue(rows.dispatchNestedPreScroll(20, 80, result, null, ViewCompat.TYPE_TOUCH))
        assertEquals(listOf(0, 80), result.toList())
        // Move inside dispatch so RecyclerView can account for offsetInWindow immediately.
        assertEquals(204, host.getChildAt(2).top)
        assertFalse(host.onStartNestedScroll(wrapper, rows, ViewCompat.SCROLL_AXIS_HORIZONTAL, ViewCompat.TYPE_TOUCH))
        assertFalse(host.onNestedPreFling(rows, 0f, 2000f))
    }

    @Test fun `native pointer stream collapses before table rows scroll`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.setContentView(host)
        val rows = recycler().apply {
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun getItemCount() = 100
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                    object : RecyclerView.ViewHolder(View(context).apply {
                        layoutParams = RecyclerView.LayoutParams(-1, 48)
                    }) {}
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = Unit
            }
        }
        val wrapper = HorizontalScrollView(context).apply {
            isFillViewport = true
            addView(rows, ViewGroup.LayoutParams(400, -1))
        }
        (host.getChildAt(2) as FrameLayout).addView(wrapper, ViewGroup.LayoutParams(-1, -1))
        layout()
        host.coordinator.restoreProgress(.4f)
        layout()
        var rowTravel = 0
        rows.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) { rowTravel += dy }
        })
        fun pointer(action: Int, y: Float, time: Long) {
            val event = MotionEvent.obtain(0, time, action, 200f, y, 0).apply {
                source = InputDevice.SOURCE_TOUCHSCREEN
            }
            try { host.dispatchTouchEvent(event) } finally { event.recycle() }
            layout()
        }
        pointer(MotionEvent.ACTION_DOWN, 700f, 0)
        pointer(MotionEvent.ACTION_MOVE, 620f, 16)
        pointer(MotionEvent.ACTION_MOVE, 600f, 32)
        val headerBefore = host.coordinator.offset
        val rowsBefore = rowTravel
        pointer(MotionEvent.ACTION_MOVE, 480f, 48)

        assertEquals(200f, host.coordinator.offset)
        assertTrue(rowTravel > rowsBefore)
        assertEquals(120f, host.coordinator.offset - headerBefore + rowTravel - rowsBefore, 1f)
        pointer(MotionEvent.ACTION_CANCEL, 480f, 64)
    }

    @Test fun `posted stop cannot end a restarted native touch stream`() {
        layout()
        val rows = recycler()
        (host.getChildAt(2) as FrameLayout).addView(rows)
        host.onNestedScrollAccepted(rows, rows, ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
        host.onStopNestedScroll(rows, ViewCompat.TYPE_TOUCH)
        host.onNestedScrollAccepted(rows, rows, ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
        shadowOf(Looper.getMainLooper()).idle()

        val consumed = IntArray(2)
        host.onNestedPreScroll(rows, 0, 70, consumed, ViewCompat.TYPE_TOUCH)

        assertEquals(70, consumed[1])
        assertEquals(70f, host.coordinator.offset)
    }

    @Test fun `geometry cancels held native pointer before moving bounds and rejects its remaining events`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.setContentView(host)
        var cancelCount = 0
        var topWhenCancelled = -1
        val rows = object : RecyclerView(context) {
            override fun onTouchEvent(event: MotionEvent): Boolean {
                if (event.actionMasked == MotionEvent.ACTION_CANCEL) {
                    cancelCount++
                    topWhenCancelled = host.getChildAt(2).top
                }
                return super.onTouchEvent(event)
            }
        }.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun getItemCount() = 100
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                    object : RecyclerView.ViewHolder(View(context).apply {
                        layoutParams = RecyclerView.LayoutParams(-1, 48)
                    }) {}
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = Unit
            }
        }
        (host.getChildAt(2) as FrameLayout).addView(HorizontalScrollView(context).apply {
            isFillViewport = true
            addView(rows, ViewGroup.LayoutParams(400, -1))
        }, ViewGroup.LayoutParams(-1, -1))
        layout()
        var rowTravel = 0
        rows.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) { rowTravel += dy }
        })
        fun pointer(action: Int, y: Float, time: Long, downTime: Long = 0) {
            val event = MotionEvent.obtain(downTime, time, action, 200f, y, 0).apply {
                source = InputDevice.SOURCE_TOUCHSCREEN
            }
            try { host.dispatchTouchEvent(event) } finally { event.recycle() }
            layout()
        }
        pointer(MotionEvent.ACTION_DOWN, 700f, 0)
        pointer(MotionEvent.ACTION_MOVE, 620f, 16)
        pointer(MotionEvent.ACTION_MOVE, 600f, 32)
        assertTrue(host.coordinator.offset > 0f && host.coordinator.offset < host.coordinator.range)
        assertEquals(0, rowTravel)
        val oldTop = host.getChildAt(2).top
        val oldOffset = host.coordinator.offset

        host.getChildAt(0).layoutParams.height += 100
        host.invalidateGeometry()
        layout()

        assertEquals(1, cancelCount)
        assertEquals(oldTop, topWhenCancelled)
        assertFalse(rows.hasNestedScrollingParent(ViewCompat.TYPE_TOUCH))
        assertEquals(oldOffset, host.coordinator.offset)
        pointer(MotionEvent.ACTION_MOVE, 500f, 48)
        pointer(MotionEvent.ACTION_MOVE, 400f, 64)
        pointer(MotionEvent.ACTION_UP, 400f, 80)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(oldOffset, host.coordinator.offset)
        assertEquals(0, rowTravel)
        assertEquals(RecyclerView.SCROLL_STATE_IDLE, rows.scrollState)
        // Activity traversal during idle uses Robolectric's default screen size. Restore
        // this fixture's explicit bounds before sending the next physical DOWN.
        layout()

        pointer(MotionEvent.ACTION_DOWN, 700f, 200, downTime = 200)
        pointer(MotionEvent.ACTION_MOVE, 620f, 216, downTime = 200)
        pointer(MotionEvent.ACTION_MOVE, 600f, 232, downTime = 200)
        assertTrue(host.coordinator.offset > oldOffset)
        assertEquals(0, rowTravel)
        pointer(MotionEvent.ACTION_CANCEL, 600f, 248, downTime = 200)
    }

    @Test fun `geometry preserves a held header editing gesture`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.setContentView(host)
        val actions = mutableListOf<Int>()
        val editor = object : EditText(context) {
            override fun onTouchEvent(event: MotionEvent): Boolean {
                actions.add(event.actionMasked)
                return super.onTouchEvent(event)
            }
        }.apply { setText("Editable object description") }
        host.removeViewAt(0)
        host.addView(editor, 0, ViewGroup.LayoutParams(-1, 244))
        host.coordinator.setBlocked(MotionOrigin.Editing, true)
        layout()
        fun pointer(action: Int, time: Long) {
            val event = MotionEvent.obtain(0, time, action, 100f, 140f, 0)
            try { host.dispatchTouchEvent(event) } finally { event.recycle() }
        }
        pointer(MotionEvent.ACTION_DOWN, 0)
        editor.layoutParams.height += 100
        host.invalidateGeometry()
        layout()
        pointer(MotionEvent.ACTION_MOVE, 16)
        pointer(MotionEvent.ACTION_UP, 32)

        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP), actions)
        assertTrue(editor.hasFocus())
    }

    @Test fun `Compose Android origin stays fixed while inner viewport remains finite`() {
        val child = View(context)
        (host.getChildAt(2) as FrameLayout).addView(child, ViewGroup.LayoutParams(-1, -1))
        var topInset = -1
        host.setStableViewportChild(child) { topInset = it }
        layout()
        val origin = host.getChildAt(2).top + child.top + child.translationY
        assertEquals(84f, origin)
        assertEquals(200, topInset)
        assertEquals(host.getChildAt(2).height, child.height - topInset)

        host.coordinator.restoreProgress(.5f)
        // The Android owner must already be stationary before the deferred layout pass.
        assertEquals(origin, host.getChildAt(2).top + child.top + child.translationY)
        layout()
        assertEquals(100, topInset)
        assertEquals(host.getChildAt(2).height, child.height - topInset)
        assertEquals(800f, origin + child.height)

        host.setStableViewportChild(null)
        layout()
        assertEquals(0, topInset)
        assertEquals(0f, child.translationY)
        assertEquals(host.getChildAt(2).height, child.height)
    }

    @Test fun `direct Compose subtree and embedded mode reject local view ownership`() {
        layout()
        val rows = recycler()
        val viewport = host.getChildAt(2) as FrameLayout
        viewport.addView(rows)
        host.excludeNestedScrollTarget = viewport
        assertFalse(host.onStartNestedScroll(viewport, rows, ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH))
        host.excludeNestedScrollTarget = null
        host.embeddedMode = true
        layout()
        assertEquals(0f, host.coordinator.range)
        assertEquals(40, viewport.top)
        assertFalse(host.onStartNestedScroll(viewport, rows, ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH))
    }

    @Test fun `geometry callback runs before viewer bounds change`() {
        layout()
        var previousBottom = -1
        host.onGeometryChanging = { previousBottom = host.getChildAt(2).bottom }
        host.getChildAt(0).layoutParams.height = 344
        host.invalidateGeometry()
        layout()
        assertEquals(800, previousBottom)
        assertEquals(384, host.getChildAt(2).top)
    }

    @Test fun `embedding cancels drag before resetting the visible header origin`() {
        layout()
        host.coordinator.restoreProgress(.5f)
        val previousTop = host.getChildAt(2).top
        var topWhenCancelled = -1
        host.onGeometryChanging = { topWhenCancelled = host.getChildAt(2).top }

        host.embeddedMode = true

        assertEquals(previousTop, topWhenCancelled)
        assertEquals(0f, host.coordinator.offset)
        assertTrue(host.getChildAt(2).top > previousTop)
    }

    @Test fun `horizontal inset change cancels drag before column coordinates change`() {
        layout()
        var leftWhenCancelled = -1
        host.onGeometryChanging = { leftWhenCancelled = host.getChildAt(2).left }

        host.setPadding(24, 0, 24, 0)
        layout()

        assertEquals(0, leftWhenCancelled)
        assertEquals(24, host.getChildAt(2).left)
        assertEquals(352, host.getChildAt(2).width)
    }

    @Test fun `horizontal wheel cancels old vertical owner without changing header`() {
        layout()
        var stopped = false
        val old = host.coordinator.begin("column", cancel = { stopped = true })
        host.coordinator.consumePreScroll(80f, old)
        val event = MotionEvent.obtain(
            0, 1, MotionEvent.ACTION_SCROLL, 1,
            arrayOf(MotionEvent.PointerProperties().apply { id = 0; toolType = MotionEvent.TOOL_TYPE_MOUSE }),
            arrayOf(MotionEvent.PointerCoords().apply { x = 200f; y = 400f; setAxisValue(MotionEvent.AXIS_HSCROLL, 1f) }),
            0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_MOUSE, 0
        )
        try { host.dispatchGenericMotionEvent(event) } finally { event.recycle() }
        assertTrue(stopped)
        assertEquals(0f, host.coordinator.consumePreScroll(80f, old))
        assertEquals(80f, host.coordinator.offset)
    }

    @Test fun `focused rectangle in tall header remains reachable while editing freezes gestures`() {
        host.getChildAt(0).layoutParams.height = 1200
        host.invalidateGeometry()
        layout()
        host.coordinator.setBlocked(MotionOrigin.Editing, true)
        assertEquals(0, host.getChildAt(2).height)
        assertFalse(host.requestChildRectangleOnScreen(host.getChildAt(0), android.graphics.Rect(0, 0, 200, 1200), true))
        assertTrue(host.requestChildRectangleOnScreen(host.getChildAt(0), android.graphics.Rect(0, 920, 200, 950), true))
        assertEquals(150f, host.coordinator.offset)
        assertTrue(host.coordinator.isBlocked)
        assertEquals(0f, host.coordinator.consumePreScroll(30f, host.coordinator.begin("touch")))
    }
}
