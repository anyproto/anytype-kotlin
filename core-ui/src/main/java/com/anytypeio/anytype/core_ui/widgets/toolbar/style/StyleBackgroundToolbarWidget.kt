package com.anytypeio.anytype.core_ui.widgets.toolbar.style

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.anytypeio.anytype.core_ui.databinding.WidgetBlockStyleToolbarBackgroundBinding
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.styling.StyleToolbarState
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class StyleBackgroundToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {

    val binding = WidgetBlockStyleToolbarBackgroundBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    val actions: Flow<StylingEvent> = merge(
        binding.flowColors.backgroundColorDefault.clicks()
            .map { StylingEvent.Coloring.Background(ThemeColor.DEFAULT) },
        binding.flowColors.backgroundColorGrey.clicks()
            .map { StylingEvent.Coloring.Background(ThemeColor.GREY) },
        binding.flowColors.backgroundColorYellow.clicks()
            .map { StylingEvent.Coloring.Background(ThemeColor.YELLOW) },
        binding.flowColors.backgroundColorOrange.clicks()
            .map { StylingEvent.Coloring.Background(ThemeColor.ORANGE) },
        binding.flowColors.backgroundColorRed.clicks()
            .map { StylingEvent.Coloring.Background(ThemeColor.RED) },
        binding.flowColors.backgroundColorPink.clicks()
            .map { StylingEvent.Coloring.Background(ThemeColor.PINK) },
        binding.flowColors.backgroundColorPurple.clicks()
            .map { StylingEvent.Coloring.Background(ThemeColor.PURPLE) },
        binding.flowColors.backgroundColorBlue.clicks()
            .map { StylingEvent.Coloring.Background(ThemeColor.BLUE) },
        binding.flowColors.backgroundColorIce.clicks()
            .map { StylingEvent.Coloring.Background(ThemeColor.ICE) },
        binding.flowColors.backgroundColorTeal.clicks()
            .map { StylingEvent.Coloring.Background(ThemeColor.TEAL) },
        binding.flowColors.backgroundColorLime.clicks()
            .map { StylingEvent.Coloring.Background(ThemeColor.LIME) }
    )

    fun update(state: StyleToolbarState.Background) {
        with(binding.flowColors) {
            backgroundColorDefault.isSelected = state.background == ThemeColor.DEFAULT.code
            backgroundColorGrey.isSelected = state.background == ThemeColor.GREY.code
            backgroundColorYellow.isSelected = state.background == ThemeColor.YELLOW.code
            backgroundColorOrange.isSelected = state.background == ThemeColor.ORANGE.code
            backgroundColorRed.isSelected = state.background == ThemeColor.RED.code
            backgroundColorPink.isSelected = state.background == ThemeColor.PINK.code
            backgroundColorPurple.isSelected = state.background == ThemeColor.PURPLE.code
            backgroundColorBlue.isSelected = state.background == ThemeColor.BLUE.code
            backgroundColorIce.isSelected = state.background == ThemeColor.ICE.code
            backgroundColorTeal.isSelected = state.background == ThemeColor.TEAL.code
            backgroundColorLime.isSelected = state.background == ThemeColor.LIME.code
        }
    }
}