package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.R
import kotlinx.android.synthetic.main.widget_scroll_and_move_action.view.*

class ScrollAndMoveActionWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.widget_scroll_and_move_action, this)
        orientation = HORIZONTAL
    }

    val apply get() : View = applyScrollAndMove
    val cancel get() : View = cancelScrollAndMove
}