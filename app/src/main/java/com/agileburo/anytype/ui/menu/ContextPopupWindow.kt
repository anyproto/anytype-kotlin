package com.agileburo.anytype.ui.menu

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.PopupWindow
import android.widget.TextView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.common.Span
import com.agileburo.anytype.core_utils.ext.invisible
import com.agileburo.anytype.core_utils.ext.visible
import com.agileburo.anytype.ext.isSpanInRange
import kotlinx.android.synthetic.main.popup_context_menu.view.*

class ContextPopupWindow @JvmOverloads constructor(
    type: AnytypeContextMenuType,
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0,
    onDismissListener: OnDismissListener,
    onTouchInterceptor: View.OnTouchListener,
    private val onContextMenuButtonClicked: (ContextMenuButtonClick) -> Unit,
    private val gravity: Int = Gravity.NO_GRAVITY,
    editable: Editable,
    textRange: IntRange,
    private val tintColor: ColorStateList
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
        when (type) {
            AnytypeContextMenuType.DEFAULT -> init(contentView, DEFAULT, editable, textRange)
            AnytypeContextMenuType.HEADER -> init(contentView, HEADER, editable, textRange)
            AnytypeContextMenuType.HIGHLIGHT -> init(contentView, HIGHLIGHT, editable, textRange)
        }
    }

    private fun init(view: View, ids: List<Int>, editable: Editable, textRange: IntRange) {
        view.btnCopy.apply {
            if (this.id in ids) {
                visible()
                view.divCopy.visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Copy)
            }
        }
        view.btnCut.apply {
            if (this.id in ids) {
                visible()
                view.divCut.visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Cut)
            }
        }
        view.btnPaste.apply {
            if (this.id in ids) {
                visible()
                view.divPaste.visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Paste)
            }
        }
        view.btnBold.apply {
            if (this.id in ids) {
                visible()
                view.divBold.visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Bold)
            }
            if (isSpanInRange(
                    textRange = textRange,
                    text = editable,
                    type = Span.Bold::class.java
                )
            ) {
                imageTintList = tintColor
            }
        }
        view.btnItalic.apply {
            if (this.id in ids) {
                visible()
                view.divItalic.visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Italic)
            }
            if (isSpanInRange(
                    textRange = textRange,
                    text = editable,
                    type = Span.Italic::class.java
                )
            ) {
                imageTintList = tintColor
            }
        }
        view.btnStroke.apply {
            if (this.id in ids) {
                visible()
                view.divStroke.visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Stroke)
            }
            if (isSpanInRange(
                    textRange = textRange,
                    text = editable,
                    type = Span.Strikethrough::class.java
                )
            ) {
                imageTintList = tintColor
            }
        }
        view.btnCode.apply {
            if (this.id in ids) {
                visible()
                view.divCode.visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Code)
            }
            if (isSpanInRange(
                    textRange = textRange,
                    text = editable,
                    type = Span.Keyboard::class.java
                )
            ) {
                imageTintList = tintColor
            }
        }
        view.btnLink.apply {
            if (this.id in ids) {
                visible()
                view.divLink.visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Link)
            }
            if (isSpanInRange(
                    textRange = textRange,
                    text = editable,
                    type = Span.Url::class.java
                )
            ) {
                imageTintList = tintColor
            }
        }
        view.btnColor.apply {
            if (this.id in ids) {
                visible()
                view.divColor.visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Color)
            }
        }
        view.btnBackground.apply {
            if (this.id in ids) {
                visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Background)
            }
        }

        val arrowRight = view.arrowRightContainer
        arrowRight.setOnClickListener {
            view.scrollContainer.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
        }
        view.scrollContainer.setOnScrollChangeListener { _, scrollX, _, oldScrollX, _ ->
            if (scrollX != oldScrollX) {
                arrowRight.invisible()
            } else {
                arrowRight.visible()
            }
        }
    }

    fun updateMarkupButtons(textRange: IntRange, editable: Editable) {
        contentView.btnBold.apply {
            imageTintList = if (isSpanInRange(
                    textRange = textRange,
                    text = editable,
                    type = Span.Bold::class.java
                )
            ) tintColor else null
        }
        contentView.btnItalic.apply {
            imageTintList = if (isSpanInRange(
                    textRange = textRange,
                    text = editable,
                    type = Span.Italic::class.java
                )
            ) tintColor else null
        }
        contentView.btnStroke.apply {
            imageTintList = if (isSpanInRange(
                    textRange = textRange,
                    text = editable,
                    type = Span.Strikethrough::class.java
                )
            ) tintColor else null
        }
        contentView.btnCode.apply {
            imageTintList = if (isSpanInRange(
                    textRange = textRange,
                    text = editable,
                    type = Span.Keyboard::class.java
                )
            ) tintColor else null
        }
        contentView.btnLink.apply {
            imageTintList = if (isSpanInRange(
                    textRange = textRange,
                    text = editable,
                    type = Span.Url::class.java
                )
            ) tintColor else null
        }
    }

    fun show(anchorView: TextView, x: Int, y: Int) {
        showAtLocation(anchorView, gravity, x, y)
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
