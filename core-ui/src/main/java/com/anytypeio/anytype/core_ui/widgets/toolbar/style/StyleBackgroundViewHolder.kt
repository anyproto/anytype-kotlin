package com.anytypeio.anytype.core_ui.widgets.toolbar.style

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.BlockStyleToolbarBackgroundBinding
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingEvent

class StyleBackgroundViewHolder(
    val binding: BlockStyleToolbarBackgroundBinding
) : RecyclerView.ViewHolder(binding.root) {

    private val default = binding.backgroundColorDefault
    private val grey = binding.backgroundColorGrey
    private val yellow = binding.backgroundColorYellow
    private val orange = binding.backgroundColorOrange
    private val red = binding.backgroundColorRed
    private val pink = binding.backgroundColorPink
    private val purple = binding.backgroundColorPurple
    private val blue = binding.backgroundColorBlue
    private val ice = binding.backgroundColorIce
    private val teal = binding.backgroundColorTeal
    private val lime = binding.backgroundColorLime

    fun bind(
        onStylingEvent: (StylingEvent) -> Unit,
        background: String?
    ) {

        default.isSelected = background == ThemeColor.DEFAULT.code
        grey.isSelected = background == ThemeColor.GREY.code
        yellow.isSelected = background == ThemeColor.YELLOW.code
        orange.isSelected = background == ThemeColor.ORANGE.code
        red.isSelected = background == ThemeColor.RED.code
        pink.isSelected = background == ThemeColor.PINK.code
        purple.isSelected = background == ThemeColor.PURPLE.code
        blue.isSelected = background == ThemeColor.BLUE.code
        ice.isSelected = background == ThemeColor.ICE.code
        teal.isSelected = background == ThemeColor.TEAL.code
        lime.isSelected = background == ThemeColor.LIME.code

        default.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Background(color = ThemeColor.DEFAULT))
        }
        grey.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Background(color = ThemeColor.GREY))
        }
        yellow.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Background(color = ThemeColor.YELLOW))
        }
        orange.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Background(color = ThemeColor.ORANGE))
        }
        red.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Background(color = ThemeColor.RED))
        }
        pink.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Background(color = ThemeColor.PINK))
        }
        purple.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Background(color = ThemeColor.PURPLE))
        }
        blue.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Background(color = ThemeColor.BLUE))
        }
        ice.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Background(color = ThemeColor.ICE))
        }
        teal.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Background(color = ThemeColor.TEAL))
        }
        lime.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Background(color = ThemeColor.LIME))
        }
    }
}