package com.agileburo.anytype.ui.menu

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.PopupWindow
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ext.invisible
import com.agileburo.anytype.core_utils.ext.visible
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
    private val gravity: Int = Gravity.NO_GRAVITY
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
        val HIGHTLIGHT = listOf(
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
        val TEXT = listOf(
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
            AnytypeContextMenuType.P -> init(contentView, TEXT)
            AnytypeContextMenuType.HEADER -> init(contentView, HEADER)
            AnytypeContextMenuType.HIGHTLIGHTED -> init(contentView, HIGHTLIGHT)
        }
    }

    private fun init(view: View, ids: List<Int>) {
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
        }
        view.btnItalic.apply {
            if (this.id in ids) {
                visible()
                view.divItalic.visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Italic)
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
        }
        view.btnCode.apply {
            if (this.id in ids) {
                visible()
                view.divCode.visible()
            }
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Code)
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

    fun show(anchor: View, x: Int, y: Int) {
        showAtLocation(anchor, gravity, x, y)
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
