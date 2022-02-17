package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.anytypeio.anytype.core_ui.databinding.WidgetMultiSelectTopToolbarBinding

class MultiSelectTopToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    val binding = WidgetMultiSelectTopToolbarBinding.inflate(
        LayoutInflater.from(context), this
    )

    val selectText get() : TextView = binding.tvToolbarTitle
    val doneButton get() : View = binding.btnDone
}