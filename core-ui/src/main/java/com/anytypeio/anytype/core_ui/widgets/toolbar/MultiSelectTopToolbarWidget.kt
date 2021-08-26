package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import kotlinx.android.synthetic.main.widget_multi_select_top_toolbar.view.*

class MultiSelectTopToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_multi_select_top_toolbar, this)
    }

    val selectText get() : TextView = tvToolbarTitle
    val doneButton get() : View = btnDone
}