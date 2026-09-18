package com.anytypeio.anytype.core_ui.widgets.dv.scroll

import android.content.Context
import android.app.Activity
import android.os.Looper
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.InputDevice
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.EditText
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ComposeView
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
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration
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

    @Test fun `header child entering editing on down keeps its remaining pointer stream`() {
        val actions = mutableListOf<Int>()
        val editor = object : View(context) {
            override fun onTouchEvent(event: MotionEvent): Boolean {
                actions.add(event.actionMasked)
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    host.coordinator.setBlocked(MotionOrigin.Editing, true)
                }
                return true
            }
        }
        host.removeViewAt(0)
        host.addView(editor, 0, ViewGroup.LayoutParams(-1, 244))
        layout()
        assertFalse(host.coordinator.isBlocked)
        listOf(MotionEvent.ACTION_DOWN to 200f, MotionEvent.ACTION_MOVE to 100f,
            MotionEvent.ACTION_UP to 100f).forEachIndexed { index, (action, y) ->
            val event = MotionEvent.obtain(0, index * 16L, action, 100f, y, 0)
            try { host.dispatchTouchEvent(event) } finally { event.recycle() }
        }
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP), actions)
        assertEquals(0f, host.coordinator.offset)
    }

    @Test fun `Compose header background collapses under a vertical drag`() {
        val container = UnmeasuredSlot(context).apply { addView(ComposeView(context)) }
        host.removeViewAt(0)
        host.addView(container, 0, ViewGroup.LayoutParams(-1, 244))
        layout()
        // An uncomposed owner hits no pointer input node, so it declines the down like a
        // plain background. The drag must then belong to the host, not to the activity.
        pointer(MotionEvent.ACTION_DOWN, 200f, 0)
        pointer(MotionEvent.ACTION_MOVE, 120f, 16)
        pointer(MotionEvent.ACTION_MOVE, 60f, 32)
        assertEquals(140f - slop, host.coordinator.offset)
        pointer(MotionEvent.ACTION_UP, 60f, 48)
    }

    @Test fun `Compose pointer target that never claims the gesture yields a vertical drag`() {
        val owner = composeOwner(hitsPointerInput = true)
        pointer(MotionEvent.ACTION_DOWN, 200f, 0)
        assertEquals(listOf(MotionEvent.ACTION_DOWN), owner.actions)
        pointer(MotionEvent.ACTION_MOVE, 120f, 16)
        // The owner saw the move first and did not ask to keep the gesture: the host
        // claims it in the same event, cancels the owner, and travels past slop at once.
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL), owner.actions)
        assertEquals(80f - slop, host.coordinator.offset)
        pointer(MotionEvent.ACTION_MOVE, 60f, 32)
        assertEquals(140f - slop, host.coordinator.offset)
        pointer(MotionEvent.ACTION_UP, 60f, 48)
        assertEquals(3, owner.actions.size)
    }

    @Test fun `Compose owner that consumes movement keeps its gesture`() {
        val owner = composeOwner(hitsPointerInput = true).apply { consumesMovement = true }
        pointer(MotionEvent.ACTION_DOWN, 200f, 0)
        pointer(MotionEvent.ACTION_MOVE, 120f, 16)
        pointer(MotionEvent.ACTION_MOVE, 60f, 32)
        pointer(MotionEvent.ACTION_UP, 60f, 48)
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP), owner.actions)
        assertEquals(0f, host.coordinator.offset)
    }

    @Test fun `Compose pointer target keeps a horizontal drag`() {
        val owner = composeOwner(hitsPointerInput = true)
        pointer(MotionEvent.ACTION_DOWN, 200f, 0, x = 100f)
        pointer(MotionEvent.ACTION_MOVE, 205f, 16, x = 200f)
        pointer(MotionEvent.ACTION_MOVE, 150f, 32, x = 300f)
        pointer(MotionEvent.ACTION_UP, 150f, 48, x = 300f)
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP), owner.actions)
        assertEquals(0f, host.coordinator.offset)
    }

    @Test fun `Compose owner entering editing on down keeps its remaining pointer stream`() {
        val owner = composeOwner(hitsPointerInput = true).apply {
            onDown = { host.coordinator.setBlocked(MotionOrigin.Editing, true) }
        }
        pointer(MotionEvent.ACTION_DOWN, 200f, 0)
        pointer(MotionEvent.ACTION_MOVE, 120f, 16)
        pointer(MotionEvent.ACTION_UP, 120f, 32)
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP), owner.actions)
        assertEquals(0f, host.coordinator.offset)
    }

    @Test fun `frames drawn during a header drag keep the drag alive`() {
        val owner = composeOwner(hitsPointerInput = true)
        pointer(MotionEvent.ACTION_DOWN, 200f, 0)
        pointer(MotionEvent.ACTION_MOVE, 130f, 16)
        assertEquals(70f - slop, host.coordinator.offset)
        // Every frame calls computeScroll while the finger is still down and no fling runs.
        host.computeScroll()
        pointer(MotionEvent.ACTION_MOVE, 60f, 32)
        host.computeScroll()
        pointer(MotionEvent.ACTION_MOVE, -10f, 48)
        assertEquals(210f - slop, host.coordinator.offset)
        assertFalse(host.coordinator.isIdle)
        pointer(MotionEvent.ACTION_UP, -10f, 64)
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL), owner.actions)
    }

    @Test fun `header release flings with the velocity of the whole pointer stream`() {
        val owner = composeOwner(hitsPointerInput = true)
        pointer(MotionEvent.ACTION_DOWN, 200f, 0)
        pointer(MotionEvent.ACTION_MOVE, 180f, 8)
        pointer(MotionEvent.ACTION_MOVE, 160f, 16)
        val held = host.coordinator.offset
        pointer(MotionEvent.ACTION_UP, 160f, 24)
        assertEquals(40f - slop, held)
        // The owner held the first move, so only the down and two moves carried velocity.
        // The release must still fling with the travel of the whole stream.
        repeat(20) {
            ShadowSystemClock.advanceBy(Duration.ofMillis(16))
            host.computeScroll()
            layout()
        }
        assertTrue(host.coordinator.offset > held, "offset ${host.coordinator.offset} <= $held")
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL), owner.actions)
    }

    @Test fun `controls row background collapses under a vertical drag`() {
        layout()
        pointer(MotionEvent.ACTION_DOWN, 260f, 0)
        pointer(MotionEvent.ACTION_MOVE, 190f, 16)
        pointer(MotionEvent.ACTION_MOVE, 120f, 32)
        assertEquals(140f - slop, host.coordinator.offset)
        pointer(MotionEvent.ACTION_UP, 120f, 48)
    }

    @Test fun `button in the controls row keeps a tap and yields a vertical drag`() {
        // A click is posted, so the host must be attached for it to run.
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.setContentView(host)
        var clicks = 0
        val button = Recorder(context).apply {
            isClickable = true
            setOnClickListener { clicks++ }
        }
        host.removeViewAt(1)
        host.addView(LinearLayout(context).apply { addView(button, LinearLayout.LayoutParams(200, 40)) }, 1, ViewGroup.LayoutParams(-1, 40))
        layout()
        pointer(MotionEvent.ACTION_DOWN, 260f, 0)
        pointer(MotionEvent.ACTION_UP, 260f, 16)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(1, clicks)
        assertEquals(0f, host.coordinator.offset)

        layout()
        pointer(MotionEvent.ACTION_DOWN, 260f, 100)
        pointer(MotionEvent.ACTION_MOVE, 190f, 116)
        pointer(MotionEvent.ACTION_MOVE, 120f, 132)
        pointer(MotionEvent.ACTION_UP, 120f, 148)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(1, clicks)
        assertEquals(140f - slop, host.coordinator.offset)
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP, MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL), button.actions)
    }

    @Test fun `viewer chrome that scrolls only horizontally yields a vertical drag while rows keep theirs`() {
        val columns = recordingRecycler(LinearLayoutManager(context, RecyclerView.HORIZONTAL, false), itemWidth = 100, itemHeight = 60)
        val rows = recordingRecycler(LinearLayoutManager(context), itemWidth = -1, itemHeight = 48)
        val table = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(columns, LinearLayout.LayoutParams(800, 60))
            addView(rows, LinearLayout.LayoutParams(800, -1))
        }
        (host.getChildAt(2) as FrameLayout).addView(HorizontalScrollView(context).apply { addView(table) }, ViewGroup.LayoutParams(-1, -1))
        layout()
        // The column header row sits at the top of the viewport and cannot scroll vertically.
        pointer(MotionEvent.ACTION_DOWN, 314f, 0)
        pointer(MotionEvent.ACTION_MOVE, 244f, 16)
        pointer(MotionEvent.ACTION_MOVE, 174f, 32)
        pointer(MotionEvent.ACTION_UP, 174f, 48)
        assertEquals(140f - slop, host.coordinator.offset)
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL), columns.actions)

        // The rows below are a nested scroll target: they keep the stream and feed the host.
        host.coordinator.restoreProgress(0f)
        layout()
        pointer(MotionEvent.ACTION_DOWN, 500f, 100)
        pointer(MotionEvent.ACTION_MOVE, 430f, 116)
        pointer(MotionEvent.ACTION_MOVE, 360f, 132)
        assertTrue(host.coordinator.offset > 0f)
        assertFalse(rows.actions.contains(MotionEvent.ACTION_CANCEL))
        pointer(MotionEvent.ACTION_UP, 360f, 148)
    }

    @Test fun `board surface keeps a vertical drag at every point`() {
        val board = Recorder(context, consumes = true)
        (host.getChildAt(2) as FrameLayout).addView(board, ViewGroup.LayoutParams(-1, -1))
        host.excludeNestedScrollTarget = board
        host.setStableViewportChild(board)
        layout()
        pointer(MotionEvent.ACTION_DOWN, 400f, 0)
        pointer(MotionEvent.ACTION_MOVE, 330f, 16)
        pointer(MotionEvent.ACTION_MOVE, 260f, 32)
        pointer(MotionEvent.ACTION_UP, 260f, 48)
        assertEquals(0f, host.coordinator.offset)
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP), board.actions)
    }

    @Test fun `native vertical scroller in the viewer keeps its drag`() {
        val scroller = ScrollView(context).apply { addView(View(context).apply { minimumHeight = 2000 }) }
        (host.getChildAt(2) as FrameLayout).addView(scroller, ViewGroup.LayoutParams(-1, -1))
        layout()
        pointer(MotionEvent.ACTION_DOWN, 400f, 0)
        pointer(MotionEvent.ACTION_MOVE, 330f, 16)
        pointer(MotionEvent.ACTION_MOVE, 260f, 32)
        pointer(MotionEvent.ACTION_UP, 260f, 48)
        assertEquals(0f, host.coordinator.offset)
        assertTrue(scroller.scrollY > 0)
    }

    private fun recordingRecycler(manager: LinearLayoutManager, itemWidth: Int, itemHeight: Int): RecordingRecycler =
        RecordingRecycler(context).apply {
            layoutManager = manager
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun getItemCount() = 100
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                    object : RecyclerView.ViewHolder(View(context).apply {
                        layoutParams = RecyclerView.LayoutParams(itemWidth, itemHeight)
                    }) {}
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = Unit
            }
        }

    private class RecordingRecycler(context: Context) : RecyclerView(context) {
        val actions = mutableListOf<Int>()
        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            actions.add(event.actionMasked)
            return super.dispatchTouchEvent(event)
        }
    }

    /** A native child that records its stream; [consumes] models a surface that owns every event. */
    private class Recorder(context: Context, private val consumes: Boolean = false) : View(context) {
        val actions = mutableListOf<Int>()
        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            actions.add(event.actionMasked)
            return super.dispatchTouchEvent(event) || consumes
        }
    }

    private val slop: Float get() = ViewConfiguration.get(context).scaledTouchSlop.toFloat()

    private fun pointer(action: Int, y: Float, time: Long, x: Float = 100f) {
        val event = MotionEvent.obtain(0, time, action, x, y, 0).apply { source = InputDevice.SOURCE_TOUCHSCREEN }
        try { host.dispatchTouchEvent(event) } finally { event.recycle() }
        layout()
    }

    private fun composeOwner(hitsPointerInput: Boolean): ComposeOwnerStub {
        val owner = ComposeOwnerStub(context, hitsPointerInput)
        val container = UnmeasuredSlot(context).apply { addView(owner) }
        host.removeViewAt(0)
        host.addView(container, 0, ViewGroup.LayoutParams(-1, 244))
        layout()
        return owner
    }

    /** Lays its child out at its own size: measuring an unattached Compose owner would compose. */
    private class UnmeasuredSlot(context: Context) : ViewGroup(context) {
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) =
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            for (index in 0 until childCount) getChildAt(index).layout(0, 0, r - l, b - t)
        }
    }

    /**
     * Models AndroidComposeView's contract without a composition: the return value says
     * whether a pointer input node was hit, and the parent is asked to stop intercepting
     * only once a handler consumes movement, never on the down.
     */
    private class ComposeOwnerStub(context: Context, private val hitsPointerInput: Boolean) : AbstractComposeView(context) {
        val actions = mutableListOf<Int>()
        var consumesMovement = false
        var onDown: () -> Unit = {}
        @Composable override fun Content() = Unit
        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            actions.add(event.actionMasked)
            if (event.actionMasked == MotionEvent.ACTION_DOWN) onDown()
            if (consumesMovement && event.actionMasked == MotionEvent.ACTION_MOVE) {
                parent.requestDisallowInterceptTouchEvent(true)
            }
            return hitsPointerInput
        }
    }

    @Test fun `external wrapped header applies saved progress after deferred content measurement`() {
        val external = View(context).apply { visibility = View.GONE }
        val container = FrameLayout(context).apply {
            addView(View(context).apply { visibility = View.GONE }, ViewGroup.LayoutParams(-1, 244))
            addView(external, ViewGroup.LayoutParams(-1, 300))
        }
        host.removeViewAt(0)
        host.addView(container, 0, ViewGroup.LayoutParams(-1, -2))
        host.pinHeight = 0
        host.coordinator.restoreProgress(.5f)
        layout()
        assertEquals(0f, host.coordinator.range)
        assertEquals(.5f, host.coordinator.savedProgress)

        external.visibility = View.VISIBLE
        layout()

        assertEquals(300f, host.coordinator.range)
        assertEquals(150f, host.coordinator.offset)
        assertEquals(190, host.getChildAt(2).top)
        assertEquals(610, host.getChildAt(2).height)
        assertEquals(800, host.getChildAt(2).bottom)
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
