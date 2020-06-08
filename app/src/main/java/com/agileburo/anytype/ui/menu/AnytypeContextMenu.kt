package com.agileburo.anytype.ui.menu

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.PopupWindow
import android.widget.TextView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_utils.ext.PopupExtensions.calculateFloatToolbarPosition
import com.agileburo.anytype.core_utils.ext.PopupExtensions.lerp
import com.agileburo.anytype.core_utils.ext.invisible
import com.agileburo.anytype.core_utils.ext.visible
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

class AnytypeContextMenu constructor(
    private val contextRef: WeakReference<Context>,
    private val anchorViewRef: WeakReference<TextView>,
    private val onMarkupActionClicked: (Markup.Type) -> Unit,
    private val dismissOnTouchOutside: Boolean = false
) : PopupWindow.OnDismissListener {

    companion object {
        const val DEFAULT_X = 20
        const val POPUP_OFFSET = 20
        const val WIDTH_IGNORE = -1
        const val HEIGHT_IGNORE = -1
        const val ANIM_DURATION = 150L
    }

    private var scrollListener: ViewTreeObserver.OnScrollChangedListener? = null

    private val isShowable: Boolean
        get() = !isDismissed && contextRef.get() != null && popupWindowRef.get() != null && anchorViewRef.get() != null
    private var isDismissed: Boolean = false
    private var popupWindowRef: WeakReference<PopupWindow>
    private var currentLocation = PointF()
    private var popupHeight: Float = 0f

    init {

        scrollListener = ViewTreeObserver.OnScrollChangedListener {
            updatePosition()
        }
        anchorViewRef.get()?.viewTreeObserver?.addOnScrollChangedListener(
            scrollListener
        )

        val popupWindow = createPopupWindow(
            contextRef.get()
                ?: throw Throwable("null context")
        )

        popupWindowRef = WeakReference(popupWindow)
        popupHeight =
            contextRef.get()
                ?.resources
                ?.getDimensionPixelSize(R.dimen.popup_context_menu_height)
                ?.toFloat()
                ?: throw Throwable("null context")
    }

    private fun cleanup() {
        anchorViewRef.get()?.viewTreeObserver?.removeOnScrollChangedListener(scrollListener)
        scrollListener = null
    }

    private fun createPopupWindow(context: Context): PopupWindow =
        PopupWindow(context, null, android.R.attr.popupWindowStyle).apply {
            val view = LayoutInflater.from(context).inflate(R.layout.popup_context_menu, null)
            contentView = view
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isOutsideTouchable = true
            isTouchable = true
            setOnDismissListener(this@AnytypeContextMenu)
            setTouchInterceptor(View.OnTouchListener { v, event ->
                if (!isShowable) return@OnTouchListener false
                when {
                    (!dismissOnTouchOutside && event.action == MotionEvent.ACTION_OUTSIDE) -> {
                        v.performClick()
                        return@OnTouchListener true
                    }
                    else -> return@OnTouchListener false
                }
            })
            isClippingEnabled = false
            isFocusable = false
            addOnMenuItemClicks(contentView)
        }

    private fun updatePosition() {
        val anchorView = anchorViewRef.get()
        val popupWindow = popupWindowRef.get()
        if (anchorView != null && popupWindow != null) {
            if (popupWindow.isShowing) {
                val rect = calculateFloatToolbarPosition(
                    anchorView = anchorView,
                    popupWindowHeight = popupHeight,
                    tooltipOffsetY = POPUP_OFFSET
                )
                popupWindow.update(
                    DEFAULT_X, rect.y.toInt(),
                    WIDTH_IGNORE,
                    HEIGHT_IGNORE
                )
            }
        }
    }

    private fun addOnMenuItemClicks(view: View) {
        view.findViewById<View>(R.id.menu_copy).setOnClickListener {
            anchorViewRef.get()?.onTextContextMenuItem(android.R.id.copy)
        }
        view.findViewById<View>(R.id.menu_cut).setOnClickListener {
            anchorViewRef.get()?.onTextContextMenuItem(android.R.id.cut)
        }
        view.findViewById<View>(R.id.menu_paste).setOnClickListener {
            anchorViewRef.get()?.onTextContextMenuItem(android.R.id.paste)
        }
        view.findViewById<View>(R.id.menu_bold).setOnClickListener {
            onMarkupActionClicked.invoke(Markup.Type.BOLD)
        }
        view.findViewById<View>(R.id.menu_italic).setOnClickListener {
            onMarkupActionClicked.invoke(Markup.Type.ITALIC)
        }
        view.findViewById<View>(R.id.menu_stroke).setOnClickListener {
            onMarkupActionClicked.invoke(Markup.Type.STRIKETHROUGH)
        }
        view.findViewById<View>(R.id.menu_code).setOnClickListener {

        }
        view.findViewById<View>(R.id.menu_link).setOnClickListener {
            onMarkupActionClicked.invoke(Markup.Type.LINK)
        }
        view.findViewById<View>(R.id.menu_color).setOnClickListener {
            onMarkupActionClicked.invoke(Markup.Type.TEXT_COLOR)
        }
        view.findViewById<View>(R.id.menu_background).setOnClickListener {
            onMarkupActionClicked.invoke(Markup.Type.BACKGROUND_COLOR)
        }

        val scrollView = view.findViewById<HorizontalScrollView>(R.id.scroll_view)
        val arrowRight = view.findViewById<FrameLayout>(R.id.container_arrow_right)
        arrowRight.setOnClickListener {
            scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
        }
        scrollView.setOnScrollChangeListener { _, scrollX, _, oldScrollX, _ ->
                if (scrollX != oldScrollX) {
                    arrowRight.invisible()
                } else {
                    arrowRight.visible()
                }
            }
    }

    override fun onDismiss() {
        isDismissed = true
    }

    fun showAtLocation() {
        val anchorView = anchorViewRef.get()
        val popupWindow = popupWindowRef.get()

        if (anchorView != null && anchorView.isShown) {
            if (popupWindow?.isShowing == true) {
                val contentView = popupWindow.contentView
                contentView?.handler?.removeCallbacksAndMessages(null)
                contentView.post {
                    val currY = currentLocation.y
                    val rect = calculateFloatToolbarPosition(
                        anchorView = anchorView,
                        popupWindowHeight = popupHeight,
                        tooltipOffsetY = POPUP_OFFSET
                    )
                    currentLocation = rect
                    val anim = ValueAnimator.ofFloat(0f, 1f)
                    anim.addUpdateListener { animation ->
                        val v = animation.animatedValue as Float
                        val y = lerp(currY, rect.y, v).roundToInt()
                        popupWindow.update(
                            DEFAULT_X, y,
                            WIDTH_IGNORE,
                            HEIGHT_IGNORE
                        )
                    }
                    anim.duration =
                        ANIM_DURATION
                    anim.start()
                }
            } else {
                val rect = calculateFloatToolbarPosition(
                    anchorView = anchorView,
                    popupWindowHeight = popupHeight,
                    tooltipOffsetY = POPUP_OFFSET
                )
                popupWindow?.showAtLocation(
                    anchorView,
                    Gravity.NO_GRAVITY,
                    DEFAULT_X,
                    rect.y.toInt()
                )
                currentLocation = rect
            }
        }
    }

    fun finish() {
        popupWindowRef.get()?.dismiss()
        cleanup()
    }
}