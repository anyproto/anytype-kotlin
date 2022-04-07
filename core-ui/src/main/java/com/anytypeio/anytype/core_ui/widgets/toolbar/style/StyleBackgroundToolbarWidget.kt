package com.anytypeio.anytype.core_ui.widgets.toolbar.style

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.anytypeio.anytype.core_ui.databinding.WidgetBlockStyleToolbarBackgroundBinding
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingEvent
import kotlinx.coroutines.flow.*

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

    fun update(background: String?) {
        with(binding.flowColors) {
            backgroundColorDefault.isSelected = background == ThemeColor.DEFAULT.title
            backgroundColorGrey.isSelected = background == ThemeColor.GREY.title
            backgroundColorYellow.isSelected = background == ThemeColor.YELLOW.title
            backgroundColorOrange.isSelected = background == ThemeColor.ORANGE.title
            backgroundColorRed.isSelected = background == ThemeColor.RED.title
            backgroundColorPink.isSelected = background == ThemeColor.PINK.title
            backgroundColorPurple.isSelected = background == ThemeColor.PURPLE.title
            backgroundColorBlue.isSelected = background == ThemeColor.BLUE.title
            backgroundColorIce.isSelected = background == ThemeColor.ICE.title
            backgroundColorTeal.isSelected = background == ThemeColor.TEAL.title
            backgroundColorGreen.isSelected = background == ThemeColor.GREEN.title
        }
    }
}