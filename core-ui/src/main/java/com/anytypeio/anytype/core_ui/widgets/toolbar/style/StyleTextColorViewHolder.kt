package com.anytypeio.anytype.core_ui.widgets.toolbar.style

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.page.editor.styling.StylingEvent
import kotlinx.android.synthetic.main.block_style_toolbar_color.view.*

class StyleTextColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val default = itemView.textColorDefault
    private val grey = itemView.textColorGrey
    private val yellow = itemView.textColorYellow
    private val orange = itemView.textColorOrange
    private val red = itemView.textColorRed
    private val pink = itemView.textColorPink
    private val purple = itemView.textColorPurple
    private val blue = itemView.textColorBlue
    private val ice = itemView.textColorIce
    private val teal = itemView.textColorTeal
    private val green = itemView.textColorGreen

    fun bind(
        onStylingEvent: (StylingEvent) -> Unit,
        color: String?
    ) {
        default.isSelected = color == ThemeColor.DEFAULT.title || color == null
        grey.isSelected = color == ThemeColor.GREY.title
        yellow.isSelected = color == ThemeColor.YELLOW.title
        orange.isSelected = color == ThemeColor.ORANGE.title
        red.isSelected = color == ThemeColor.RED.title
        pink.isSelected = color == ThemeColor.PINK.title
        purple.isSelected = color == ThemeColor.PURPLE.title
        blue.isSelected = color == ThemeColor.BLUE.title
        ice.isSelected = color == ThemeColor.ICE.title
        teal.isSelected = color == ThemeColor.TEAL.title
        green.isSelected = color == ThemeColor.GREEN.title

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
        green.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Text(color = ThemeColor.GREEN))
        }
    }
}