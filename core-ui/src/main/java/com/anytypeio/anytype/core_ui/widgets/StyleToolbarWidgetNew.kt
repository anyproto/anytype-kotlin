package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.presentation.page.editor.model.UiBlock
import kotlinx.android.synthetic.main.widget_styling_toolbar_main.view.*
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class StyleToolbarWidgetNew @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.widget_styling_toolbar_main, this)
    }

    val styles = flowOf(
        tvStyleTitle.clicks().map { UiBlock.HEADER_ONE },
        tvStyleHeading.clicks().map { UiBlock.HEADER_TWO },
        tvStyleSubheading.clicks().map { UiBlock.HEADER_THREE },
        tvStyleText.clicks().map { UiBlock.TEXT },
        checkbox.clicks().map { UiBlock.CHECKBOX },
        bulleted.clicks().map { UiBlock.BULLETED },
        numbered.clicks().map { UiBlock.NUMBERED },
        toggle.clicks().map { UiBlock.TOGGLE },
        highlight.clicks().map { UiBlock.HIGHLIGHTED },
    ).flattenMerge()

}