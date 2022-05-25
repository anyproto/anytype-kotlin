package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.databinding.WidgetScrollAndMoveActionBinding

class ScrollAndMoveActionWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    val binding = WidgetScrollAndMoveActionBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        orientation = HORIZONTAL
    }

    val apply get() : View = binding.applyScrollAndMove
    val cancel get() : View = binding.cancelScrollAndMove
}