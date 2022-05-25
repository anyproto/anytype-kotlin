package com.anytypeio.anytype.ui.menu

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.Spanned
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.core_ui.menu.ContextMenuType
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.ext.isSpanInRange

class ContextPopupWindow @JvmOverloads constructor(
    type: ContextMenuType,
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0,
    onDismissListener: OnDismissListener,
    onTouchInterceptor: View.OnTouchListener,
    editable: Editable,
    textRange: IntRange,
    private val onContextMenuButtonClicked: (ContextMenuButtonClick) -> Unit,
    private val gravity: Int = Gravity.NO_GRAVITY,
    private val tintColor: ColorStateList,
    private val textDefaultColor: Int
) : PopupWindow(context, attrs, defStyle, defStyleRes) {

    companion object {
        val HEADER = listOf(
            R.id.btnCopy,
            R.id.btnCut,
            R.id.btnPaste,
            R.id.btnItalic,
            R.id.btnLink,
            R.id.btnCode,
            R.id.btnColor,
            R.id.btnStroke,
            R.id.btnBackground
        )
        val HIGHLIGHT = listOf(
            R.id.btnCopy,
            R.id.btnCut,
            R.id.btnPaste,
            R.id.btnBold,
            R.id.btnLink,
            R.id.btnCode,
            R.id.btnColor,
            R.id.btnStroke,
            R.id.btnBackground
        )
        val DEFAULT = listOf(
            R.id.btnCopy,
            R.id.btnCut,
            R.id.btnPaste,
            R.id.btnBold,
            R.id.btnItalic,
            R.id.btnLink,
            R.id.btnCode,
            R.id.btnColor,
            R.id.btnStroke,
            R.id.btnBackground
        )
    }

    private var popupHeight: Int
    private var popupMargin: Int
    private var mShowAnimation: AnimatorSet?

    init {
        contentView = LayoutInflater.from(context).inflate(R.layout.popup_context_menu, null)
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isOutsideTouchable = true
        isTouchable = true
        isClippingEnabled = false
        isFocusable = false
        setOnDismissListener(onDismissListener)
        setTouchInterceptor(onTouchInterceptor)
        popupHeight = context.resources
            .getDimensionPixelSize(R.dimen.popup_context_menu_height)
        popupMargin = context.resources
            .getDimensionPixelSize(R.dimen.popup_context_menu_margin)
        mShowAnimation = createEnterAnimation(contentView)
        when (type) {
            ContextMenuType.TEXT -> init(contentView, DEFAULT, editable, textRange)
            ContextMenuType.HEADER -> init(contentView, HEADER, editable, textRange)
            ContextMenuType.HIGHLIGHT -> init(contentView, HIGHLIGHT, editable, textRange)
        }
    }

    private fun init(view: View, ids: List<Int>, editable: Editable, textRange: IntRange) {
        view.findViewById<View>(R.id.btnCopy).apply {
            if (this.id in ids) {
                visible()
                view.findViewById<View>(R.id.divCopy).visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Copy)
            }
        }
        view.findViewById<View>(R.id.btnCut).apply {
            if (this.id in ids) {
                visible()
                view.findViewById<View>(R.id.divCut).visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Cut)
            }
        }
        view.findViewById<View>(R.id.btnPaste).apply {
            if (this.id in ids) {
                visible()
                view.findViewById<View>(R.id.divPaste).visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Paste)
            }
        }
        view.findViewById<ImageView>(R.id.btnBold).apply {
            if (this.id in ids) {
                visible()
                view.findViewById<View>(R.id.divBold).visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Bold)
            }
            if (editable.isSpanInRange(
                    textRange = textRange,
                    type = Span.Bold::class.java
                )
            ) {
                imageTintList = tintColor
            }
        }
        view.findViewById<ImageView>(R.id.btnItalic).apply {
            if (this.id in ids) {
                visible()
                view.findViewById<View>(R.id.divItalic).visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Italic)
            }
            if (editable.isSpanInRange(
                    textRange = textRange,
                    type = Span.Italic::class.java
                )
            ) {
                imageTintList = tintColor
            }
        }
        view.findViewById<ImageView>(R.id.btnStroke).apply {
            if (this.id in ids) {
                visible()
                view.findViewById<View>(R.id.divStroke).visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Stroke)
            }
            if (editable.isSpanInRange(
                    textRange = textRange,
                    type = Span.Strikethrough::class.java
                )
            ) {
                imageTintList = tintColor
            }
        }
        view.findViewById<ImageView>(R.id.btnCode).apply {
            if (this.id in ids) {
                visible()
                view.findViewById<View>(R.id.divCode).visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Code)
            }
            if (editable.isSpanInRange(
                    textRange = textRange,
                    type = Span.Keyboard::class.java
                )
            ) {
                imageTintList = tintColor
            }
        }
        view.findViewById<ImageView>(R.id.btnLink).apply {
            if (this.id in ids) {
                visible()
                view.findViewById<View>(R.id.divLink).visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Link)
            }
            if (editable.isSpanInRange(
                    textRange = textRange,
                    type = Span.Url::class.java
                )
            ) {
                imageTintList = tintColor
            }
        }
        view.findViewById<TextView>(R.id.btnColor).apply {
            if (this.id in ids) {
                visible()
                view.findViewById<View>(R.id.divColor).visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Color)
            }
            if (editable.isSpanInRange(
                    textRange = textRange,
                    type = Span.TextColor::class.java
                )
            ) {
                setTextColor(tintColor)
            }
        }
        view.findViewById<TextView>(R.id.btnBackground).apply {
            if (this.id in ids) {
                visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Background)
            }
            if (editable.isSpanInRange(
                    textRange = textRange,
                    type = Span.Highlight::class.java
                )
            ) {
                setTextColor(tintColor)
            }
        }

        val arrowRight = view.findViewById<FrameLayout>(R.id.arrowRightContainer)
        arrowRight.setOnClickListener {
            view.findViewById<HorizontalScrollView>(R.id.scrollContainer)
                .fullScroll(HorizontalScrollView.FOCUS_RIGHT)
        }
        view.findViewById<HorizontalScrollView>(R.id.scrollContainer)
            .setOnScrollChangeListener { _, scrollX, _, oldScrollX, _ ->
                if (scrollX != oldScrollX) {
                    if (scrollX == 0) arrowRight.visible() else arrowRight.invisible()
                } else {
                    arrowRight.visible()
                }
            }
    }

    private fun createEnterAnimation(view: View): AnimatorSet? {
        val animation = AnimatorSet()
        animation.playTogether(
            ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
                .setDuration(200)
        )
        return animation
    }

    /**
     * Get the coordinates of this popup for positioning on the screen..
     *
     * @param viewPortOnScreen portion of screen we can draw in.
     * @param selectedContentBounds This is the area of the interesting content that this popup
     * should avoid obstructing
     * @param windowLeftOnScreen parent window X margins
     * @param windowTopOnScreen parent window Y margins
     *
     * @return x and y coordinates of popup window
     */
    fun refreshCoordinates(
        viewPortOnScreen: Rect,
        selectedContentBounds: Rect,
        windowLeftOnScreen: Int,
        windowTopOnScreen: Int
    ): Point {

        val availableHeightAboveContent = selectedContentBounds.top - viewPortOnScreen.top
        val availableHeightBelowContent = viewPortOnScreen.bottom - selectedContentBounds.bottom
        val margin = popupMargin * 2
        val toolbarHeightWithVerticalMargin = popupHeight + margin

        val y = if (availableHeightAboveContent >= toolbarHeightWithVerticalMargin) {
            // There is enough space at the top of the content.
            selectedContentBounds.top - toolbarHeightWithVerticalMargin
        } else if (availableHeightBelowContent >= toolbarHeightWithVerticalMargin) {
            // There is enough space at the bottom of the content.
            selectedContentBounds.bottom
        } else if (availableHeightBelowContent >= popupHeight) {
            // Just enough space to fit the toolbar with no vertical margins.
            selectedContentBounds.bottom - popupMargin
        } else {
            // Not enough space. Prefer to position as high as possible.
            viewPortOnScreen.top.coerceAtLeast(selectedContentBounds.top - toolbarHeightWithVerticalMargin)
        }
        return Point(0, 0.coerceAtLeast(y - windowTopOnScreen))
    }

    fun runShowAnimation() {
        mShowAnimation?.start()
    }

    /**
     * Updates buttons state, when markup changed in text
     */
    fun updateMarkupButtons(textRange: IntRange, spanned: Spanned) {
        contentView.findViewById<ImageView>(R.id.btnBold).apply {
            imageTintList = if (spanned.isSpanInRange(
                    textRange = textRange,
                    type = Span.Bold::class.java
                )
            ) tintColor else null
        }
        contentView.findViewById<ImageView>(R.id.btnItalic).apply {
            imageTintList = if (spanned.isSpanInRange(
                    textRange = textRange,
                    type = Span.Italic::class.java
                )
            ) tintColor else null
        }
        contentView.findViewById<ImageView>(R.id.btnStroke).apply {
            imageTintList = if (spanned.isSpanInRange(
                    textRange = textRange,
                    type = Span.Strikethrough::class.java
                )
            ) tintColor else null
        }
        contentView.findViewById<ImageView>(R.id.btnCode).apply {
            imageTintList = if (spanned.isSpanInRange(
                    textRange = textRange,
                    type = Span.Keyboard::class.java
                )
            ) tintColor else null
        }
        contentView.findViewById<ImageView>(R.id.btnLink).apply {
            imageTintList = if (spanned.isSpanInRange(
                    textRange = textRange,
                    type = Span.Url::class.java
                )
            ) tintColor else null
        }
        contentView.findViewById<TextView>(R.id.btnColor).apply {
            if (spanned.isSpanInRange(
                    textRange = textRange,
                    type = Span.TextColor::class.java
                )
            ) setTextColor(tintColor) else setTextColor(textDefaultColor)
        }
        contentView.findViewById<TextView>(R.id.btnBackground).apply {
            if (spanned.isSpanInRange(
                    textRange = textRange,
                    type = Span.Highlight::class.java
                )
            ) setTextColor(tintColor) else setTextColor(textDefaultColor)
        }
    }
}

sealed class ContextMenuButtonClick {
    object Copy : ContextMenuButtonClick()
    object Cut : ContextMenuButtonClick()
    object Paste : ContextMenuButtonClick()
    object Bold : ContextMenuButtonClick()
    object Italic : ContextMenuButtonClick()
    object Stroke : ContextMenuButtonClick()
    object Code : ContextMenuButtonClick()
    object Color : ContextMenuButtonClick()
    object Background : ContextMenuButtonClick()
    object Link : ContextMenuButtonClick()
}
