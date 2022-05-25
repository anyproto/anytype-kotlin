package com.anytypeio.anytype.ui.menu

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Point
import android.text.Editable
import android.text.Spanned
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.PopupWindow
import android.widget.TextView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.menu.ContextMenuType
import com.anytypeio.anytype.core_utils.ext.PopupExtensions.calculateContentBounds
import com.anytypeio.anytype.core_utils.ext.PopupExtensions.calculateRectInWindow
import com.anytypeio.anytype.core_utils.ext.PopupExtensions.lerp
import com.anytypeio.anytype.presentation.editor.editor.Markup
import java.lang.ref.WeakReference

class AnytypeContextMenu constructor(
    context: Context,
    anchorView: TextView,
    parent: View,
    type: ContextMenuType,
    private val onMarkupActionClicked: (Markup.Type) -> Unit,
    private val dismissOnTouchOutside: Boolean = false
) : PopupWindow.OnDismissListener {

    companion object {
        const val DEFAULT_X = 20
        const val WIDTH_CHANGE_IGNORE = -1
        const val HEIGHT_CHANGE_IGNORE = -1
        const val ANIM_DURATION = 150L
    }

    private val contextRef: WeakReference<Context>
    private val anchorViewRef: WeakReference<TextView>
    private val parentViewRef: WeakReference<View>

    private var scrollListener: ViewTreeObserver.OnScrollChangedListener? = null

    private val isShowable: Boolean
        get() = !isDismissed && contextRef.get() != null && popupWindowRef.get() != null && anchorViewRef.get() != null
    private var isDismissed: Boolean = false
    private var popupWindowRef: WeakReference<ContextPopupWindow>
    private var popupWindowCurrentLocation = Point()

    /**
     * We need to add this bottom padding below selected content,
     * because of [android.widget.Editor.SelectionHandleView]
     */
    private var bottomAllowance: Int = 0

    /**
     * Callback for all touch events being dispatched to the popup
     * window.
     */
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
            tintColor = ColorStateList.valueOf(context.color(R.color.context_menu_selected_item)),
            textDefaultColor = context.color(R.color.white)
        )
        contextRef = WeakReference(context)
        anchorViewRef = WeakReference(anchorView)
        parentViewRef = WeakReference(parent)
        popupWindowRef = WeakReference(popupWindow)
        bottomAllowance = context.resources
            .getDimensionPixelSize(R.dimen.popup_context_menu_bottom_allowance)
    }

    private fun cleanup() {
        anchorViewRef.get()?.viewTreeObserver?.removeOnScrollChangedListener(scrollListener)
        scrollListener = null
    }

    private fun updatePosition() {
        val anchorView = anchorViewRef.get()
        val popupWindow = popupWindowRef.get()
        val parentView = parentViewRef.get()
        if (anchorView != null && popupWindow != null && parentView != null) {
            if (popupWindow.isShowing) {
                val updatedLocation = getUpdatedCoordinates(
                    parentView = parentView,
                    anchorView = anchorView,
                    bottomAllowance = bottomAllowance,
                    popupWindow = popupWindow
                )
                if (updatedLocation != popupWindowCurrentLocation) {
                    popupWindowCurrentLocation = updatedLocation
                    popupWindow.update(
                        DEFAULT_X,
                        popupWindowCurrentLocation.y,
                        WIDTH_CHANGE_IGNORE,
                        HEIGHT_CHANGE_IGNORE
                    )
                }
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

    override fun onDismiss() {
        isDismissed = true
    }

    fun showAtLocation() {
        val anchorView = anchorViewRef.get()
        val popupWindow = popupWindowRef.get()
        val parentView = parentViewRef.get()
        if (anchorView != null && popupWindow != null && parentView != null) {
            if (popupWindow.isShowing) {
                val contentView = popupWindow.contentView
                contentView?.handler?.removeCallbacksAndMessages(null)
                contentView.post {
                    val updatedLocation = getUpdatedCoordinates(
                        parentView = parentView,
                        anchorView = anchorView,
                        bottomAllowance = bottomAllowance,
                        popupWindow = popupWindow
                    )
                    popupWindow.updateMarkupButtons(
                        textRange = IntRange(
                            anchorView.selectionStart,
                            anchorView.selectionEnd
                        ),
                        spanned = anchorView.text as Spanned
                    )
                    if (updatedLocation != popupWindowCurrentLocation) {
                        val currY = popupWindowCurrentLocation.y
                        popupWindowCurrentLocation = updatedLocation
                        ValueAnimator.ofFloat(0f, 1f).apply {
                            addUpdateListener { animation ->
                                val v = animation.animatedValue as Float
                                val y = lerp(currY, updatedLocation.y, v)
                                popupWindow.update(
                                    DEFAULT_X,
                                    y,
                                    WIDTH_CHANGE_IGNORE,
                                    HEIGHT_CHANGE_IGNORE
                                )
                            }
                            duration = ANIM_DURATION
                            start()
                        }
                    }
                }
            } else {
                with(popupWindow) {
                    popupWindowCurrentLocation = getUpdatedCoordinates(
                        parentView = parentView,
                        anchorView = anchorView,
                        bottomAllowance = bottomAllowance,
                        popupWindow = this
                    )
                    updateMarkupButtons(
                        textRange = IntRange(
                            anchorView.selectionStart,
                            anchorView.selectionEnd
                        ),
                        spanned = anchorView.text as Spanned
                    )
                    showAtLocation(
                        anchorView,
                        Gravity.NO_GRAVITY,
                        DEFAULT_X,
                        popupWindowCurrentLocation.y
                    )
                    runShowAnimation()
                }
            }
        }
    }

    private fun getUpdatedCoordinates(
        parentView: View,
        anchorView: TextView,
        popupWindow: ContextPopupWindow,
        bottomAllowance: Int
    ): Point {
        val windowPointOnScreen = getWindowPointOnScreen(parentView)
        val contentRect = calculateContentBounds(anchorView, bottomAllowance)
        val parentRect = calculateRectInWindow(parentView)
        return popupWindow.refreshCoordinates(
            selectedContentBounds = contentRect,
            viewPortOnScreen = parentRect,
            windowTopOnScreen = windowPointOnScreen.y,
            windowLeftOnScreen = windowPointOnScreen.x
        )
    }

    private fun getWindowPointOnScreen(parentView: View): Point {
        val tempCoords = IntArray(2)
        var windowLeftOnScreen = 0
        var windowTopOnScreen = 0
        parentView.rootView?.let {
            it.getLocationOnScreen(tempCoords)
            val rootViewLeftOnScreen = tempCoords[0]
            val rootViewTopOnScreen = tempCoords[1]
            it.getLocationInWindow(tempCoords)
            val rootViewLeftOnWindow = tempCoords[0]
            val rootViewTopOnWindow = tempCoords[1]
            windowLeftOnScreen = rootViewLeftOnScreen - rootViewLeftOnWindow
            windowTopOnScreen = rootViewTopOnScreen - rootViewTopOnWindow
        }
        return Point(windowLeftOnScreen, windowTopOnScreen)
    }

    fun finish() {
        popupWindowRef.get()?.dismiss()
        cleanup()
    }
}