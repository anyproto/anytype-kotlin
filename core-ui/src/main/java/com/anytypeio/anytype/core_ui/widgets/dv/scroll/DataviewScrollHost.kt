package com.anytypeio.anytype.core_ui.widgets.dv.scroll

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.EditText
import android.widget.OverScroller
import android.widget.TextView
import androidx.compose.ui.platform.AbstractComposeView
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Three children: measured object header, pinned dataview controls, finite viewer viewport.
 * The fixed toolbar and bottom controls belong to the enclosing screen. Header measurements
 * are reused during scrolling; only the real viewer viewport changes size.
 */
open class DataviewScrollHost @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs), NestedScrollingParent3 {
    val coordinator = DataviewScrollCoordinator()
    var pinHeight: Int = 0
        set(value) { if (field != value) { field = value.coerceAtLeast(0); invalidateGeometry() } }
    var embeddedMode: Boolean = false
        set(value) {
            if (field != value) {
                cancelHeldNativeTouchForGeometry()
                onGeometryChanging?.invoke()
                field = value
                cancelMotion()
                coordinator.restoreProgress(0f)
                invalidateGeometry()
            }
        }
    var eligibleNestedScrollTarget: (View) -> Boolean = {
        it is RecyclerView && it.layoutManager?.canScrollVertically() == true
    }
    var excludeNestedScrollTarget: View? = null
    var onGeometryChanging: (() -> Unit)? = null
    var onHeaderChanged: (() -> Unit)? = null

    private val helper = NestedScrollingParentHelper(this)
    private val configuration = ViewConfiguration.get(context)
    private val scroller = OverScroller(context)
    private val nativeSessions = mutableMapOf<View, DataviewScrollCoordinator.Session>()
    private val nativeTouchAcceptances = mutableMapOf<View, Long>()
    private var nextTouchAcceptance = 0L
    private var touchInProgress = false
    private var nativeTouchInProgress = false
    private var ignoreTouchUntilDown = false
    private var touchDownTime = 0L
    private var touchEventTime = 0L
    private var touchX = 0f
    private var touchY = 0f
    private var touchSource = 0
    private var stableViewportChild: View? = null
    private var stableViewportOriginalHeight = LayoutParams.MATCH_PARENT
    private var updateStableViewportInset: (Int) -> Unit = {}
    private var geometry: List<Int>? = null
    private var measureHeader = true
    private var expandedTop = 0
    private var controlsTop = 0
    private var viewportTop = 0
    private var downX = 0f
    private var downY = 0f
    private var lastY = 0f
    private var directEligible = false
    private var dragging = false
    private var directSession: DataviewScrollCoordinator.Session? = null
    private var velocityTracker: VelocityTracker? = null
    private var lastFlingY = 0
    private var updatingGeometry = false
    private val stateListener: () -> Unit = {
        // Native children measure offsetInWindow inside nested dispatch. Move the origin
        // now so their next pointer sample is compensated; defer viewport measurement to
        // the next layout pass, before drawing, rather than moving it between input events.
        if (!updatingGeometry && isLaidOut && childCount == 3) placeScrollOrigin()
        requestLayout()
        invalidate()
        onHeaderChanged?.invoke()
    }

    init {
        clipChildren = true
        clipToPadding = true
        coordinator.addListener(stateListener)
    }

    fun invalidateGeometry() {
        measureHeader = true
        requestLayout()
    }

    /**
     * Compose's native velocity tracker uses Android-local event coordinates. Keep its
     * Android owner fixed while an inner top inset supplies the actual moving viewport.
     * Native RecyclerViews still move inside nested dispatch for offsetInWindow accounting.
     */
    fun setStableViewportChild(view: View?, onTopInsetChanged: (Int) -> Unit = {}) {
        if (stableViewportChild !== view) {
            cancelHeldNativeTouchForGeometry()
            onGeometryChanging?.invoke()
            cancelMotion()
            stableViewportChild?.let { previous ->
                updateStableViewportInset(0)
                previous.translationY = 0f
                previous.layoutParams = previous.layoutParams.apply { height = stableViewportOriginalHeight }
            }
            stableViewportChild = view
            stableViewportOriginalHeight = view?.layoutParams?.height ?: LayoutParams.MATCH_PARENT
        }
        updateStableViewportInset = onTopInsetChanged
        invalidateGeometry()
    }

    fun cancelMotion() {
        coordinator.cancel()
        nativeSessions.clear()
        nativeTouchAcceptances.clear()
        stopDirectMotion()
    }

    override fun onDetachedFromWindow() {
        touchInProgress = false
        nativeTouchInProgress = false
        cancelMotion()
        coordinator.endTouch()
        super.onDetachedFromWindow()
    }

    override fun generateDefaultLayoutParams() = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    override fun generateLayoutParams(attrs: AttributeSet?) = LayoutParams(context, attrs)
    override fun generateLayoutParams(p: LayoutParams?) = LayoutParams(p)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
        if (childCount != 3) return
        val body = getChildAt(0)
        val controls = getChildAt(1)
        val viewport = getChildAt(2)
        val contentWidth = (width - paddingLeft - paddingRight).coerceAtLeast(0)
        val w = MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY)
        fun measureFixed(view: View) {
            if (view.visibility == GONE) return
            if (measureHeader || view.isLayoutRequested || view.measuredWidth != contentWidth) {
                val lp = view.layoutParams
                val h = if (lp.height >= 0) MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY)
                    else MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                view.measure(w, h)
            }
        }
        measureFixed(body)
        measureFixed(controls)
        measureHeader = false
        val bodyHeight = if (body.visibility == GONE || embeddedMode) 0 else body.measuredHeight
        val controlsHeight = if (controls.visibility == GONE) 0 else controls.measuredHeight
        val pin = if (embeddedMode) 0 else pinHeight
        val nextExpandedTop = max(bodyHeight, pin)
        val nextGeometry = listOf(width, height, bodyHeight, controlsHeight, pin,
            paddingLeft, paddingTop, paddingRight, paddingBottom)
        val preserveProgress = coordinator.isIdle
        if (geometry != nextGeometry) {
            cancelHeldNativeTouchForGeometry()
            onGeometryChanging?.invoke()
            cancelMotion()
            geometry = nextGeometry
        }
        expandedTop = nextExpandedTop
        updatingGeometry = true
        coordinator.setRange(if (embeddedMode) 0f else (expandedTop - pin).toFloat(), preserveProgress)
        updatingGeometry = false
        controlsTop = paddingTop + expandedTop - coordinator.offset.roundToInt()
        viewportTop = controlsTop + controlsHeight
        stableViewportChild?.let { child ->
            val fullHeight = (height - paddingBottom - paddingTop - pin - controlsHeight).coerceAtLeast(0)
            if (child.layoutParams.height != fullHeight) {
                child.layoutParams = child.layoutParams.apply { this.height = fullHeight }
            }
            placeStableViewportOrigin()
        }
        viewport.measure(w, MeasureSpec.makeMeasureSpec(
            (height - paddingBottom - viewportTop).coerceAtLeast(0), MeasureSpec.EXACTLY
        ))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount != 3) return
        val body = getChildAt(0)
        val controls = getChildAt(1)
        val viewport = getChildAt(2)
        val left = paddingLeft
        val right = measuredWidth - paddingRight
        val bodyTop = paddingTop - coordinator.offset.roundToInt()
        body.layout(left, bodyTop, right, bodyTop + body.measuredHeight)
        controls.layout(left, controlsTop, right, viewportTop)
        viewport.layout(left, viewportTop, right, viewportTop + viewport.measuredHeight)
        body.importantForAccessibility = if (embeddedMode || (coordinator.range > 0f && coordinator.offset == coordinator.range))
            IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS else IMPORTANT_FOR_ACCESSIBILITY_AUTO
    }

    private fun placeScrollOrigin() {
        val nextControlsTop = paddingTop + expandedTop - coordinator.offset.roundToInt()
        val delta = nextControlsTop - controlsTop
        val body = getChildAt(0)
        body.offsetTopAndBottom(paddingTop - coordinator.offset.roundToInt() - body.top)
        getChildAt(1).offsetTopAndBottom(delta)
        getChildAt(2).offsetTopAndBottom(delta)
        controlsTop = nextControlsTop
        viewportTop += delta
        placeStableViewportOrigin()
    }

    private fun placeStableViewportOrigin() {
        val child = stableViewportChild ?: return
        val inset = if (embeddedMode) 0 else
            (expandedTop - pinHeight - coordinator.offset.roundToInt()).coerceAtLeast(0)
        child.translationY = -inset.toFloat()
        updateStableViewportInset(inset)
    }

    override fun requestChildRectangleOnScreen(child: View, rectangle: Rect, immediate: Boolean): Boolean {
        if (embeddedMode || childCount != 3 || !child.isInSubtree(getChildAt(0)) || coordinator.range == 0f) {
            return super.requestChildRectangleOnScreen(child, rectangle, immediate)
        }
        // EditText asks for its cursor rectangle when selection/IME changes. A very tall
        // header may need this explicit reveal even while user collapse is frozen for editing.
        val visible = Rect(rectangle)
        offsetDescendantRectToMyCoords(child, visible)
        val top = paddingTop + pinHeight
        val bottom = height - paddingBottom
        if (bottom <= top) return false
        val largerThanViewport = visible.height() > bottom - top
        val delta = when {
            visible.bottom > bottom && visible.top > top ->
                if (largerThanViewport) visible.top - top else visible.bottom - bottom
            visible.top < top && visible.bottom < bottom ->
                if (largerThanViewport) visible.bottom - bottom else visible.top - top
            else -> 0
        }
        val target = (coordinator.offset + delta).coerceIn(0f, coordinator.range)
        if (target == coordinator.offset) return false
        cancelHeldNativeTouchForGeometry()
        onGeometryChanging?.invoke()
        coordinator.restoreProgress(target / coordinator.range)
        return true
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        if (childCount == 3 && child === getChildAt(0)) {
            if (embeddedMode) return false
            val checkpoint = canvas.save()
            canvas.clipRect(paddingLeft, paddingTop, width - paddingRight, controlsTop)
            val result = super.drawChild(canvas, child, drawingTime)
            canvas.restoreToCount(checkpoint)
            return result
        }
        return super.drawChild(canvas, child, drawingTime)
    }

    private fun View.isInSubtree(root: View?): Boolean {
        if (root == null) return false
        var current: View? = this
        while (current != null) {
            if (current === root) return true
            current = current.parent as? View
        }
        return false
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean =
        !embeddedMode && axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0 &&
            !target.isInSubtree(excludeNestedScrollTarget) && eligibleNestedScrollTarget(target)

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        helper.onNestedScrollAccepted(child, target, axes, type)
        if (type == ViewCompat.TYPE_TOUCH) {
            nativeTouchAcceptances[target] = ++nextTouchAcceptance
            if (touchInProgress && target is RecyclerView) nativeTouchInProgress = true
        }
        val old = nativeSessions[target]
        if (old == null || !coordinator.isCurrent(old)) {
            nativeSessions[target] = coordinator.begin(
                owner = "view:${System.identityHashCode(target)}",
                origin = if (type == ViewCompat.TYPE_TOUCH) MotionOrigin.User else MotionOrigin.Accessibility,
                integerPixels = true,
                cancel = { (target as? RecyclerView)?.stopScroll() }
            )
        }
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        val token = nativeSessions[target] ?: return
        consumed[1] += coordinator.consumePreScroll(dy.toFloat(), token).roundToInt()
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
        dyUnconsumed: Int, type: Int, consumed: IntArray) {
        val token = nativeSessions[target] ?: return
        consumed[1] += coordinator.consumePostScroll(dyUnconsumed.toFloat(), token).roundToInt()
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        helper.onStopNestedScroll(target, type)
        if (type == ViewCompat.TYPE_NON_TOUCH) nativeSessions.remove(target)?.let(coordinator::end)
        else {
            val stopped = nativeSessions[target]
            val acceptance = nativeTouchAcceptances[target]
            post {
                if (stopped != null && nativeSessions[target] == stopped &&
                    nativeTouchAcceptances[target] == acceptance &&
                    (target as? RecyclerView)?.scrollState != RecyclerView.SCROLL_STATE_SETTLING) {
                    nativeSessions.remove(target)
                    nativeTouchAcceptances.remove(target)
                    coordinator.end(stopped)
                }
            }
        }
    }

    // RecyclerView's ViewFlinger remains the sole owner of native fling frames.
    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float) = false
    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean) = false
    override fun getNestedScrollAxes(): Int = helper.nestedScrollAxes
    override fun onStartNestedScroll(child: View, target: View, axes: Int) = onStartNestedScroll(child, target, axes, ViewCompat.TYPE_TOUCH)
    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) = onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH)
    override fun onStopNestedScroll(target: View) = onStopNestedScroll(target, ViewCompat.TYPE_TOUCH)
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) = onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH)
    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) =
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH)
    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) =
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, IntArray(2))

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            ignoreTouchUntilDown = false
            touchInProgress = true
            nativeTouchInProgress = false
            cancelMotion()
            coordinator.beginTouch()
            if (!embeddedMode && event.y < paddingTop + pinHeight) {
                touchInProgress = false
                coordinator.endTouch()
                return false
            }
        }
        if (ignoreTouchUntilDown) return true
        touchDownTime = event.downTime
        touchEventTime = event.eventTime
        touchX = event.x
        touchY = event.y
        touchSource = event.source
        return try {
            super.dispatchTouchEvent(event)
        } finally {
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                touchInProgress = false
                nativeTouchInProgress = false
                coordinator.endTouch()
            }
        }
    }

    private fun cancelHeldNativeTouchForGeometry() {
        if (!touchInProgress || !nativeTouchInProgress) return
        // stopScroll() leaves RecyclerView's pointer, velocity tracker and TOUCH nested
        // parent intact. Cancel through View dispatch before moving its coordinate origin;
        // dispatch gating also works after RecyclerView disallows parent interception.
        ignoreTouchUntilDown = true
        touchInProgress = false
        nativeTouchInProgress = false
        val cancel = MotionEvent.obtain(
            touchDownTime, touchEventTime, MotionEvent.ACTION_CANCEL, touchX, touchY, 0
        ).apply { source = touchSource }
        try {
            super.dispatchTouchEvent(cancel)
        } finally {
            cancel.recycle()
            coordinator.endTouch()
        }
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_SCROLL &&
            (event.getAxisValue(MotionEvent.AXIS_HSCROLL) != 0f ||
                event.getAxisValue(MotionEvent.AXIS_VSCROLL) != 0f)) {
            cancelMotion()
            coordinator.notifyUserInput()
        }
        return super.dispatchGenericMotionEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            downX = event.x
            downY = event.y
            lastY = event.y
            dragging = false
            directEligible = !embeddedMode && !coordinator.isBlocked && isHeaderBackground(event.x, event.y)
        }
        // A Compose editor can acquire focus and block scrolling while handling DOWN,
        // after this parent's initial hit test. Keep its subsequent selection events.
        if (coordinator.isBlocked) {
            directEligible = false
            return false
        }
        if (event.actionMasked == MotionEvent.ACTION_MOVE && directEligible && !dragging) {
            val dy = event.y - downY
            val dx = event.x - downX
            if (abs(dx) > configuration.scaledTouchSlop && abs(dx) > abs(dy)) directEligible = false
            if (abs(dy) > configuration.scaledTouchSlop && abs(dy) > abs(dx)) {
                startDirect()
                lastY = downY + if (dy < 0) -configuration.scaledTouchSlop else configuration.scaledTouchSlop
                return true
            }
        }
        return dragging
    }

    private fun isHeaderBackground(x: Float, y: Float): Boolean {
        if (childCount != 3 || y < paddingTop + pinHeight) return false
        val surface = when {
            y < controlsTop -> getChildAt(0)
            y >= viewportTop && !hasScrollSurface(getChildAt(2)) -> getChildAt(2)
            else -> return false
        }
        return !interactiveAt(surface, x - surface.left, y - surface.top)
    }

    private fun hasScrollSurface(view: View): Boolean {
        if (view.visibility != VISIBLE) return false
        if (view === excludeNestedScrollTarget || eligibleNestedScrollTarget(view)) return true
        if (view is ViewGroup) for (index in 0 until view.childCount) {
            if (hasScrollSurface(view.getChildAt(index))) return true
        }
        return false
    }

    private fun interactiveAt(view: View, x: Float, y: Float): Boolean {
        if (view.visibility != VISIBLE || x < 0 || y < 0 || x >= view.width || y >= view.height) return false
        // Native child traversal cannot inspect Compose click targets or text selection.
        // The Compose owner must handle its own header gestures without interception.
        if (view is AbstractComposeView) return true
        if (view is ViewGroup) {
            for (index in view.childCount - 1 downTo 0) {
                val child = view.getChildAt(index)
                if (interactiveAt(child, x - child.left + view.scrollX, y - child.top + view.scrollY)) return true
            }
        }
        return view.isClickable || view.isLongClickable || view is EditText ||
            (view is TextView && view.isTextSelectable)
    }

    private fun startDirect() {
        directSession = coordinator.begin("header", integerPixels = true, cancel = ::stopDirectMotion)
        dragging = true
        velocityTracker?.recycle()
        velocityTracker = VelocityTracker.obtain()
        parent?.requestDisallowInterceptTouchEvent(true)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (coordinator.isBlocked) directEligible = false
        if (!directEligible) return false
        velocityTracker?.addMovement(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_MOVE -> {
                if (!dragging) {
                    val dy = event.y - downY
                    val dx = event.x - downX
                    if (abs(dx) > configuration.scaledTouchSlop && abs(dx) > abs(dy)) {
                        directEligible = false
                        return false
                    }
                    if (abs(dy) <= configuration.scaledTouchSlop) return true
                    startDirect()
                    lastY = downY + if (dy < 0) -configuration.scaledTouchSlop else configuration.scaledTouchSlop
                }
                val delta = (lastY - event.y).roundToInt()
                directSession?.let { consumeDirect(delta, it) }
                lastY -= delta
            }
            MotionEvent.ACTION_UP -> {
                if (dragging) {
                    velocityTracker?.computeCurrentVelocity(1000, configuration.scaledMaximumFlingVelocity.toFloat())
                    val velocity = -(velocityTracker?.yVelocity ?: 0f).roundToInt()
                    dragging = false
                    lastFlingY = coordinator.offset.roundToInt()
                    scroller.fling(0, lastFlingY, 0, velocity, 0, 0, 0, coordinator.range.roundToInt())
                    ViewCompat.postInvalidateOnAnimation(this)
                }
                velocityTracker?.recycle()
                velocityTracker = null
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_DOWN -> {
                directSession?.let(coordinator::cancel)
                stopDirectMotion()
                directEligible = false
            }
        }
        return true
    }

    private fun consumeDirect(delta: Int, token: DataviewScrollCoordinator.Session): Int =
        (coordinator.consumePreScroll(delta.toFloat(), token) +
            coordinator.consumePostScroll(delta.toFloat(), token)).roundToInt()

    override fun computeScroll() {
        val token = directSession ?: return
        if (!coordinator.isCurrent(token)) { stopDirectMotion(); return }
        if (scroller.computeScrollOffset()) {
            val delta = scroller.currY - lastFlingY
            lastFlingY = scroller.currY
            if (consumeDirect(delta, token) != delta) scroller.abortAnimation()
            ViewCompat.postInvalidateOnAnimation(this)
        } else {
            coordinator.end(token)
            directSession = null
        }
    }

    private fun stopDirectMotion() {
        scroller.abortAnimation()
        directSession = null
        dragging = false
        velocityTracker?.recycle()
        velocityTracker = null
    }
}
