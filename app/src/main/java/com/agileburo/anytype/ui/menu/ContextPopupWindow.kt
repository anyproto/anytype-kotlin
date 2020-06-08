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
        init(contentView)
    }

    private fun init(view: View) {
        view.btnCopy.apply {
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Copy)
            }
        }
        view.btnCut.apply {
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Cut)
            }
        }
        view.btnPaste.apply {
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Paste)
            }
        }
        view.btnBold.apply {
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Bold)
            }
        }
        view.btnItalic.apply {
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Italic)
            }
        }
        view.btnStroke.apply {
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Stroke)
            }
        }
        view.btnCode.apply {
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Code)
            }
        }
        view.btnLink.apply {
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.Link)
            }
        }
        view.btnColor.apply {
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.TextColor)
            }
        }
        view.btnBackground.apply {
            setOnClickListener {
                onContextMenuButtonClicked(ContextMenuButtonClick.BackgroundColor)
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
    object TextColor : ContextMenuButtonClick()
    object BackgroundColor : ContextMenuButtonClick()
    object Link : ContextMenuButtonClick()
}
