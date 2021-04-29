package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.anytypeio.anytype.core_ui.R

class StyleToolbarWidgetNew @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.widget_styling_toolbar_main, this)
    }
}