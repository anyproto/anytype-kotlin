package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.presentation.page.editor.Markup
import com.anytypeio.anytype.presentation.page.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.page.editor.model.Alignment
import com.anytypeio.anytype.presentation.page.editor.styling.StyleConfig
import com.anytypeio.anytype.presentation.page.editor.styling.StylingEvent
import kotlinx.android.synthetic.main.widget_block_style_extra.view.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class StyleToolbarExtraWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.widget_block_style_extra, this)
    }

    val actions : Flow<StylingEvent> = merge(
        bold.clicks().map { StylingEvent.Markup.Bold },
        italic.clicks().map { StylingEvent.Markup.Italic },
        strikethrough.clicks().map { StylingEvent.Markup.StrikeThrough },
        code.clicks().map { StylingEvent.Markup.Code },
        alignmentLeft.clicks().map { StylingEvent.Alignment.Left },
        alignmentMiddle.clicks().map { StylingEvent.Alignment.Center },
        alignmentRight.clicks().map { StylingEvent.Alignment.Right },
        setUrl.clicks().map { StylingEvent.Markup.Link }
    )

    fun setProperties(
        props: ControlPanelState.Toolbar.Styling.Props?,
        config: StyleConfig?
    ) {
        bold.isSelected = props?.isBold ?: false
        italic.isSelected = props?.isItalic ?: false
        strikethrough.isSelected = props?.isStrikethrough ?: false
        code.isSelected = props?.isCode ?: false
        alignmentLeft.isSelected = props?.alignment == Alignment.START
        alignmentMiddle.isSelected = props?.alignment == Alignment.CENTER
        alignmentRight.isSelected = props?.alignment == Alignment.END
        setUrl.isSelected = props?.isLinked ?: false

        config?.let {
            alignmentLeft.isEnabled = it.enabledAlignment.contains(Alignment.START)
            alignmentLeftIcon.isEnabled = alignmentLeft.isEnabled
            alignmentRight.isEnabled = it.enabledAlignment.contains(Alignment.END)
            alignmentRightIcon.isEnabled = alignmentRight.isEnabled
            alignmentMiddle.isEnabled = it.enabledAlignment.contains(Alignment.CENTER)
            alignmentMiddleIcon.isEnabled = alignmentMiddle.isEnabled

            bold.isEnabled = it.enabledMarkup.contains(Markup.Type.BOLD)
            boldIcon.isEnabled = bold.isEnabled
            italic.isEnabled = it.enabledMarkup.contains(Markup.Type.ITALIC)
            italicIcon.isEnabled = italic.isEnabled
            strikethrough.isEnabled = it.enabledMarkup.contains(Markup.Type.STRIKETHROUGH)
            strikethroughIcon.isEnabled = strikethrough.isEnabled
            code.isEnabled = it.enabledMarkup.contains(Markup.Type.KEYBOARD)
            codeIcon.isEnabled = code.isEnabled
            setUrl.isEnabled = it.enabledMarkup.contains(Markup.Type.LINK)
            setUrlIcon.isEnabled = setUrl.isEnabled
        }
    }
}