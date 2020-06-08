package com.agileburo.anytype.ui.menu

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.agileburo.anytype.R

class ContextPopupWindow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0,
    onDismissListener: OnDismissListener,
    onTouchInterceptor: View.OnTouchListener,
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
    }

    fun show(anchor: View, x: Int, y: Int) {
        showAtLocation(anchor, gravity, x, y)
    }
}
