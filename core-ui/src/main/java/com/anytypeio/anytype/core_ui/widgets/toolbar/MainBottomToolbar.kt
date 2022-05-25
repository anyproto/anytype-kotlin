package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.databinding.WidgetMainBottomToolbarBinding
import com.anytypeio.anytype.core_ui.reactive.clicks

class MainBottomToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    val binding = WidgetMainBottomToolbarBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        isBaselineAligned = false
        orientation = HORIZONTAL
    }

    fun searchClicks() = binding.btnSearch.clicks()
    fun homeClicks() = binding.btnHome.clicks()
    fun backClicks() = binding.btnBack.clicks()
}