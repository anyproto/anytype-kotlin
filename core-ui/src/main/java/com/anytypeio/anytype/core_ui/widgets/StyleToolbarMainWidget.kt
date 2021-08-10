package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.marginRight
import com.anytypeio.anytype.core_models.TextStyle
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.toast
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.presentation.editor.editor.model.UiBlock
import kotlinx.android.synthetic.main.widget_style_toolbar_main.view.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class StyleToolbarMainWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.widget_style_toolbar_main, this)

        callout.setOnClickListener {
            context.toast(
                msg = "Will be implemented later",
                duration = Toast.LENGTH_SHORT
            )
        }
    }

    val styles = merge(
        tvStyleTitle.clicks().map { UiBlock.HEADER_ONE },
        tvStyleHeading.clicks().map { UiBlock.HEADER_TWO },
        tvStyleSubheading.clicks().map { UiBlock.HEADER_THREE },
        tvStyleText.clicks().map { UiBlock.TEXT },
        checkbox.clicks().map { UiBlock.CHECKBOX },
        bulleted.clicks().map { UiBlock.BULLETED },
        numbered.clicks().map { UiBlock.NUMBERED },
        toggle.clicks().map { UiBlock.TOGGLE },
        highlight.clicks().map { UiBlock.HIGHLIGHTED },
    )

    val other = dots.clicks()
    val colors = markupColors.clicks()

    fun setSelectedStyle(style: TextStyle?) {
        when (style) {
            TextStyle.P -> select(tvStyleText.id)
            TextStyle.H1 -> select(tvStyleTitle.id)
            TextStyle.H2 -> select(tvStyleHeading.id)
            TextStyle.H3 -> select(tvStyleSubheading.id)
            TextStyle.H4 -> select(tvStyleSubheading.id)
            TextStyle.QUOTE -> select(highlight.id)
            TextStyle.BULLET -> select(bulleted.id)
            TextStyle.NUMBERED -> select(numbered.id)
            TextStyle.TOGGLE -> select(toggle.id)
            TextStyle.CHECKBOX -> select(checkbox.id)
            else -> select(View.NO_ID)
        }
    }

    private fun select(selectedViewId: Int) {

        // Selecting views in scrollable parent

        tvStyleText.apply { isSelected = id == selectedViewId }

        if (tvStyleText.isSelected) {
            val delta = (textStyles.right) - tvStyleText.left
            if (delta < 0) {
                textStyles.smoothScrollBy(
                    (-delta) + (tvStyleText.width) + tvStyleText.marginRight - textStyles.scrollX,
                    0
                )
            }
        }

        tvStyleTitle.apply { isSelected = id == selectedViewId }

        if (tvStyleTitle.isSelected) {
            textStyles.smoothScrollTo(0, 0)
        }

        tvStyleHeading.apply { isSelected = id == selectedViewId }
        tvStyleSubheading.apply { isSelected = id == selectedViewId }

        // Selecting views in non-scrolling parent

        checkbox.apply { isSelected = id == selectedViewId }
        bulleted.apply { isSelected = id == selectedViewId }
        numbered.apply { isSelected = id == selectedViewId }
        toggle.apply { isSelected = id == selectedViewId }
        highlight.apply { isSelected = id == selectedViewId }
    }
}