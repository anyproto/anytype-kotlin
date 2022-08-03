package com.anytypeio.anytype.core_ui.widgets.toolbar.style

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.BlockStyleToolbarColorBinding
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingEvent

class StyleTextColorViewHolder(
    val binding: BlockStyleToolbarColorBinding
) : RecyclerView.ViewHolder(binding.root) {

    private val default = binding.textColorDefault
    private val grey = binding.textColorGrey
    private val yellow = binding.textColorYellow
    private val orange = binding.textColorOrange
    private val red = binding.textColorRed
    private val pink = binding.textColorPink
    private val purple = binding.textColorPurple
    private val blue = binding.textColorBlue
    private val ice = binding.textColorIce
    private val teal = binding.textColorTeal
    private val lime = binding.textColorGreen

    fun bind(
        onStylingEvent: (StylingEvent) -> Unit,
        color: String?
    ) {
        default.isSelected = color == ThemeColor.DEFAULT.code || color == null
        grey.isSelected = color == ThemeColor.GREY.code
        yellow.isSelected = color == ThemeColor.YELLOW.code
        orange.isSelected = color == ThemeColor.ORANGE.code
        red.isSelected = color == ThemeColor.RED.code
        pink.isSelected = color == ThemeColor.PINK.code
        purple.isSelected = color == ThemeColor.PURPLE.code
        blue.isSelected = color == ThemeColor.BLUE.code
        ice.isSelected = color == ThemeColor.ICE.code
        teal.isSelected = color == ThemeColor.TEAL.code
        lime.isSelected = color == ThemeColor.LIME.code

        default.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Text(color = ThemeColor.DEFAULT))
        }
        grey.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Text(color = ThemeColor.GREY))
        }
        yellow.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Text(color = ThemeColor.YELLOW))
        }
        orange.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Text(color = ThemeColor.ORANGE))
        }
        red.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Text(color = ThemeColor.RED))
        }
        pink.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Text(color = ThemeColor.PINK))
        }
        purple.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Text(color = ThemeColor.PURPLE))
        }
        blue.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Text(color = ThemeColor.BLUE))
        }
        ice.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Text(color = ThemeColor.ICE))
        }
        teal.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Text(color = ThemeColor.TEAL))
        }
        lime.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Text(color = ThemeColor.LIME))
        }
    }
}