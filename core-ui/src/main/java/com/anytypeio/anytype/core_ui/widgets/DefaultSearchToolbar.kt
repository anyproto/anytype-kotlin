package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetDefaultSearchToolbarBinding

class DefaultSearchToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    val binding = WidgetDefaultSearchToolbarBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        setBackgroundResource(R.drawable.rect_search_input)
    }
}