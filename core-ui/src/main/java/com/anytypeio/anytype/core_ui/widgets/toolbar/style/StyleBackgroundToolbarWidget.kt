package com.anytypeio.anytype.core_ui.widgets.toolbar.style

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.anytypeio.anytype.core_ui.databinding.WidgetBlockStyleToolbarBackgroundBinding
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
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
        binding.flowColors.backgroundColorGreen.clicks()
            .map { StylingEvent.Coloring.Background(ThemeColor.GREEN) }
    )

    fun update(state: StyleToolbarState.Background) {
        with(binding.flowColors) {
            backgroundColorDefault.isSelected = state.background == ThemeColor.DEFAULT.title
            backgroundColorGrey.isSelected = state.background == ThemeColor.GREY.title
            backgroundColorYellow.isSelected = state.background == ThemeColor.YELLOW.title
            backgroundColorOrange.isSelected = state.background == ThemeColor.ORANGE.title
            backgroundColorRed.isSelected = state.background == ThemeColor.RED.title
            backgroundColorPink.isSelected = state.background == ThemeColor.PINK.title
            backgroundColorPurple.isSelected = state.background == ThemeColor.PURPLE.title
            backgroundColorBlue.isSelected = state.background == ThemeColor.BLUE.title
            backgroundColorIce.isSelected = state.background == ThemeColor.ICE.title
            backgroundColorTeal.isSelected = state.background == ThemeColor.TEAL.title
            backgroundColorGreen.isSelected = state.background == ThemeColor.GREEN.title
        }
    }
}