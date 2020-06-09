package com.agileburo.anytype.ui.menu

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PointF
import android.text.Editable
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.PopupWindow
import android.widget.TextView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.Span
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_utils.ext.PopupExtensions.calculateFloatToolbarPosition
import com.agileburo.anytype.core_utils.ext.PopupExtensions.lerp
import com.agileburo.anytype.ext.isSpanInRange
import kotlinx.android.synthetic.main.popup_context_menu.view.*
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

class AnytypeContextMenu constructor(
    context: Context,
    anchorView: TextView,
    private val type: AnytypeContextMenuType,
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

    private val contextRef: WeakReference<Context>
    private val anchorViewRef: WeakReference<TextView>

    private var scrollListener: ViewTreeObserver.OnScrollChangedListener? = null

    private val isShowable: Boolean
        get() = !isDismissed && contextRef.get() != null && popupWindowRef.get() != null && anchorViewRef.get() != null
    private var isDismissed: Boolean = false
    private var popupWindowRef: WeakReference<ContextPopupWindow>
    private var currentLocation = PointF()
    private var popupHeight: Float = 0f

    private var onTouchListener = View.OnTouchListener { v, event ->
        if (!isShowable) return@OnTouchListener false
        when {
            (!dismissOnTouchOutside && event.action == MotionEvent.ACTION_OUTSIDE) -> {
                v.performClick()
                return@OnTouchListener true
            }
            else -> return@OnTouchListener false
        }
    }

    init {

        scrollListener = ViewTreeObserver.OnScrollChangedListener {
            updatePosition()
        }
        anchorView.viewTreeObserver?.addOnScrollChangedListener(
            scrollListener
        )

        val popupWindow = ContextPopupWindow(
            context = context,
            onContextMenuButtonClicked = this::onContextMenuButtonClicked,
            onDismissListener = this,
            onTouchInterceptor = onTouchListener,
            type = type,
            editable = anchorView.text as Editable,
            textRange = IntRange(anchorView.selectionStart, anchorView.selectionEnd),
            tintColor = ColorStateList.valueOf(context.color(R.color.context_menu_selected_item))
        )
        contextRef = WeakReference(context)
        anchorViewRef = WeakReference(anchorView)
        popupWindowRef = WeakReference(popupWindow)
        popupHeight = context.resources
            .getDimensionPixelSize(R.dimen.popup_context_menu_height)
            .toFloat()
    }

    private fun cleanup() {
        anchorViewRef.get()?.viewTreeObserver?.removeOnScrollChangedListener(scrollListener)
        scrollListener = null
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

    private fun onContextMenuButtonClicked(click: ContextMenuButtonClick) {
        when (click) {
            ContextMenuButtonClick.Copy -> anchorViewRef.get()
                ?.onTextContextMenuItem(android.R.id.copy)
            ContextMenuButtonClick.Cut -> anchorViewRef.get()
                ?.onTextContextMenuItem(android.R.id.cut)
            ContextMenuButtonClick.Paste -> anchorViewRef.get()
                ?.onTextContextMenuItem(android.R.id.paste)
            ContextMenuButtonClick.Bold -> onMarkupActionClicked(Markup.Type.BOLD)
            ContextMenuButtonClick.Italic -> onMarkupActionClicked(Markup.Type.ITALIC)
            ContextMenuButtonClick.Stroke -> onMarkupActionClicked(Markup.Type.STRIKETHROUGH)
            ContextMenuButtonClick.Code -> onMarkupActionClicked(Markup.Type.KEYBOARD)
            ContextMenuButtonClick.Color -> onMarkupActionClicked(Markup.Type.TEXT_COLOR)
            ContextMenuButtonClick.Background -> onMarkupActionClicked(Markup.Type.BACKGROUND_COLOR)
            ContextMenuButtonClick.Link -> onMarkupActionClicked(Markup.Type.LINK)
        }
    }

    private fun initMenuItems(view: View) {
        view.btnBold.apply {
            anchorViewRef.get()?.let {
                if (isSpanInRange(
                        textRange = IntRange(it.selectionStart, it.selectionEnd),
                        text = it.text as Editable,
                        type = Span.Bold::class.java
                    )
                ) {
                    this.imageTintList =
                        ColorStateList.valueOf(view.context.color(R.color.context_menu_selected_item))
                }
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
                popupWindow?.show(
                    anchorView,
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

enum class AnytypeContextMenuType { DEFAULT, HEADER, HIGHLIGHT }