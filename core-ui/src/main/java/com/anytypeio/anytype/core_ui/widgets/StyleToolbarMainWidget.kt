package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.marginRight
import com.anytypeio.anytype.core_models.TextStyle
import com.anytypeio.anytype.core_ui.databinding.WidgetStyleToolbarMainBinding
import com.anytypeio.anytype.core_ui.extensions.toast
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.presentation.editor.editor.model.UiBlock
import com.anytypeio.anytype.presentation.editor.editor.styling.StyleToolbarState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class StyleToolbarMainWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {

    val binding = WidgetStyleToolbarMainBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        binding.callout.setOnClickListener {
            context.toast(
                msg = "Will be implemented later",
                duration = Toast.LENGTH_SHORT
            )
        }
    }

    val styles = merge(
        binding.tvStyleTitle.clicks().map { UiBlock.HEADER_ONE },
        binding.tvStyleHeading.clicks().map { UiBlock.HEADER_TWO },
        binding.tvStyleSubheading.clicks().map { UiBlock.HEADER_THREE },
        binding.tvStyleText.clicks().map { UiBlock.TEXT },
        binding.checkbox.clicks().map { UiBlock.CHECKBOX },
        binding.bulleted.clicks().map { UiBlock.BULLETED },
        binding.numbered.clicks().map { UiBlock.NUMBERED },
        binding.toggle.clicks().map { UiBlock.TOGGLE },
        binding.highlight.clicks().map { UiBlock.HIGHLIGHTED },
    )

    val other = binding.dots.clicks()
    val colors = binding.markupColors.clicks()

    fun setSelectedStyle(state: StyleToolbarState.Text?) {
        when (state?.textStyle) {
            TextStyle.P -> select(binding.tvStyleText.id)
            TextStyle.H1 -> select(binding.tvStyleTitle.id)
            TextStyle.H2 -> select(binding.tvStyleHeading.id)
            TextStyle.H3 -> select(binding.tvStyleSubheading.id)
            TextStyle.H4 -> select(binding.tvStyleSubheading.id)
            TextStyle.QUOTE -> select(binding.highlight.id)
            TextStyle.BULLET -> select(binding.bulleted.id)
            TextStyle.NUMBERED -> select(binding.numbered.id)
            TextStyle.TOGGLE -> select(binding.toggle.id)
            TextStyle.CHECKBOX -> select(binding.checkbox.id)
            else -> select(View.NO_ID)
        }
    }

    private fun select(selectedViewId: Int) = with(binding/**/) {

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