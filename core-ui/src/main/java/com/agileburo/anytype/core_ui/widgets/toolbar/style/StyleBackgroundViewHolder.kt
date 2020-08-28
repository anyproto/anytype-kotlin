package com.agileburo.anytype.core_ui.widgets.toolbar.style

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.common.ThemeColor
import com.agileburo.anytype.core_ui.features.page.styling.StylingEvent
import kotlinx.android.synthetic.main.block_style_toolbar_background.view.*

class StyleBackgroundViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val default = itemView.backgroundColorDefault
    private val grey = itemView.backgroundColorGrey
    private val yellow = itemView.backgroundColorYellow
    private val orange = itemView.backgroundColorOrange
    private val red = itemView.backgroundColorRed
    private val pink = itemView.backgroundColorPink
    private val purple = itemView.backgroundColorPurple
    private val blue = itemView.backgroundColorBlue
    private val ice = itemView.backgroundColorIce
    private val teal = itemView.backgroundColorTeal
    private val green = itemView.backgroundColorGreen

    fun bind(
        onStylingEvent: (StylingEvent) -> Unit,
        background: String?
    ) {

        default.isSelected = background == ThemeColor.DEFAULT.title
        grey.isSelected = background == ThemeColor.GREY.title
        yellow.isSelected = background == ThemeColor.YELLOW.title
        orange.isSelected = background == ThemeColor.ORANGE.title
        red.isSelected = background == ThemeColor.RED.title
        pink.isSelected = background == ThemeColor.PINK.title
        purple.isSelected = background == ThemeColor.PURPLE.title
        blue.isSelected = background == ThemeColor.BLUE.title
        ice.isSelected = background == ThemeColor.ICE.title
        teal.isSelected = background == ThemeColor.TEAL.title
        green.isSelected = background == ThemeColor.GREEN.title

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
        green.setOnClickListener {
            onStylingEvent(StylingEvent.Coloring.Background(color = ThemeColor.GREEN))
        }
    }
}