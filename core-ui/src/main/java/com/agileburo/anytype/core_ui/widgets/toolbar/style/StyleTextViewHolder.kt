package com.agileburo.anytype.core_ui.widgets.toolbar.style

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.styling.StylingEvent
import com.agileburo.anytype.core_ui.state.ControlPanelState
import kotlinx.android.synthetic.main.block_style_toolbar_style.view.*

class StyleTextViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val bold = itemView.bold
    private val iconBold = itemView.boldIcon
    private val italic = itemView.italic
    private val iconItalic = itemView.italicIcon
    private val strike = itemView.strikethrough
    private val iconStrike = itemView.strikethroughIcon
    private val code = itemView.code
    private val iconCode = itemView.codeIcon
    private val link = itemView.setUrlButton
    private val iconLink = itemView.setUrlIcon
    private val start = itemView.alignmentLeft
    private val iconStart = itemView.alignmentLeftIcon
    private val center = itemView.alignmentMiddle
    private val iconCenter = itemView.alignmentMiddleIcon
    private val end = itemView.alignmentRight
    private val iconEnd = itemView.alignmentRightIcon

    fun bind(
        onStylingEvent: (StylingEvent) -> Unit,
        enabledMarkup: List<Markup.Type>,
        enabledAlignment: List<Alignment>,
        target: ControlPanelState.Toolbar.Styling.Props?
    ) {
        with(start) {
            enabledAlignment.contains(Alignment.START).let { isEnabled ->
                this.isEnabled = isEnabled
                iconStart.isEnabled = isEnabled
                if (isEnabled) {
                    isSelected = target?.alignment == Alignment.START
                }
            }
            setOnClickListener {
                if (it.isEnabled) onStylingEvent(StylingEvent.Alignment.Left)
            }
        }
        with(center) {
            enabledAlignment.contains(Alignment.CENTER).let { isEnabled ->
                this.isEnabled = isEnabled
                iconCenter.isEnabled = isEnabled
                if (isEnabled) {
                    isSelected = target?.alignment == Alignment.CENTER
                }
            }
            setOnClickListener {
                if (it.isEnabled) onStylingEvent(StylingEvent.Alignment.Center)
            }
        }
        with(end) {
            enabledAlignment.contains(Alignment.END).let { isEnabled ->
                this.isEnabled = isEnabled
                iconEnd.isEnabled = isEnabled
                if (isEnabled) {
                    isSelected = target?.alignment == Alignment.END
                }
            }
            setOnClickListener {
                if (it.isEnabled) onStylingEvent(StylingEvent.Alignment.Right)
            }
        }
        with(code) {
            enabledMarkup.contains(Markup.Type.KEYBOARD).let { isEnabled ->
                this.isEnabled = isEnabled
                iconCode.isEnabled = isEnabled
                if (isEnabled) {
                    isSelected = target?.isCode ?: false
                }
            }
            setOnClickListener {
                if (it.isEnabled) onStylingEvent(StylingEvent.Markup.Code)
            }
        }
        with(bold) {
            enabledMarkup.contains(Markup.Type.BOLD).let { isEnabled ->
                this.isEnabled = isEnabled
                iconBold.isEnabled = isEnabled
                if (isEnabled) {
                    isSelected = target?.isBold ?: false
                }
            }
            setOnClickListener {
                if (it.isEnabled) onStylingEvent(StylingEvent.Markup.Bold)
            }
        }
        with(italic) {
            enabledMarkup.contains(Markup.Type.ITALIC).let { isEnabled ->
                this.isEnabled = isEnabled
                iconItalic.isEnabled = isEnabled
                if (isEnabled) {
                    isSelected = target?.isItalic ?: false
                }
            }
            setOnClickListener {
                if (it.isEnabled) onStylingEvent(StylingEvent.Markup.Italic)
            }
        }
        with(strike) {
            enabledMarkup.contains(Markup.Type.STRIKETHROUGH).let { isEnabled ->
                this.isEnabled = isEnabled
                iconStrike.isEnabled = isEnabled
                if (isEnabled) {
                    isSelected = target?.isStrikethrough ?: false
                }
            }
            setOnClickListener {
                if (it.isEnabled) onStylingEvent(StylingEvent.Markup.StrikeThrough)
            }
        }
        with(link) {
            enabledMarkup.contains(Markup.Type.LINK).let { isEnabled ->
                this.isEnabled = isEnabled
                iconLink.isEnabled = isEnabled
                if (isEnabled) {
                    isSelected = target?.isLinked ?: false
                }
            }
            setOnClickListener {
                if (it.isEnabled) onStylingEvent(StylingEvent.Markup.Link)
            }
        }
    }
}