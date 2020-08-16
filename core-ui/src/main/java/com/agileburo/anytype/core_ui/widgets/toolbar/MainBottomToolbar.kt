package com.agileburo.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.reactive.clicks
import kotlinx.android.synthetic.main.widget_page_bottom_toolbar.view.*

class MainBottomToolbar : ConstraintLayout {

    constructor(context: Context) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        inflate(context)
    }

    fun searchClicks() = btnSearch.clicks()
    fun navigationClicks() = btnNavigation.clicks()
    fun addPageClick() = btnAddDoc.clicks()

    private fun inflate(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.widget_page_bottom_toolbar, this, true)
    }
}