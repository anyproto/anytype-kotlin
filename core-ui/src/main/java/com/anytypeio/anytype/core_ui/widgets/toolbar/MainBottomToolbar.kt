package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.reactive.clicks
import kotlinx.android.synthetic.main.widget_main_bottom_toolbar.view.*

class MainBottomToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_main_bottom_toolbar, this, true)
        isBaselineAligned = false
        orientation = HORIZONTAL
    }

    fun searchClicks() = btnSearch.clicks()
    fun homeClicks() = btnHome.clicks()
    fun backClicks() = btnBack.clicks()
}