package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.extensions.lighter
import com.anytypeio.anytype.core_ui.extensions.tint
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.markup.MarkupStyleDescriptor
import kotlinx.android.synthetic.main.widget_markup_toolbar_main.view.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class MarkupToolbarWidget : LinearLayout {

    constructor(
        context: Context
    ) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        inflate()
    }

    private fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.widget_markup_toolbar_main, this)
    }

    private fun bold() = bold.clicks().map { Markup.Type.BOLD }
    private fun italic() = italic.clicks().map { Markup.Type.ITALIC }
    private fun strike() = strike.clicks().map { Markup.Type.STRIKETHROUGH }
    private fun code() = code.clicks().map { Markup.Type.KEYBOARD }

    fun linkClicks() = url.clicks().map { Markup.Type.LINK }
    fun colorClicks() = color.clicks()
    fun highlightClicks() = highlight.clicks()
    fun markup() = merge(bold(), italic(), strike(), code())

    fun setProps(
        props: MarkupStyleDescriptor?,
        supportedTypes: List<Markup.Type>,
        isBackgroundColorSelected: Boolean,
        isTextColorSelected: Boolean
    ) {
        bold.isSelected = props?.isBold ?: false
        bold.isEnabled = supportedTypes.contains(Markup.Type.BOLD)
        boldIcon.isEnabled = bold.isEnabled
        italic.isSelected = props?.isItalic ?: false
        italic.isEnabled = supportedTypes.contains(Markup.Type.ITALIC)
        italicIcon.isEnabled = italic.isEnabled
        strike.isSelected = props?.isStrikethrough ?: false
        strike.isEnabled = supportedTypes.contains(Markup.Type.STRIKETHROUGH)
        strikeIcon.isEnabled = strike.isEnabled
        code.isSelected = props?.isCode ?: false
        code.isEnabled = supportedTypes.contains(Markup.Type.KEYBOARD)
        codeIcon.isEnabled = code.isEnabled
        url.isSelected = props?.isLinked ?: false
        url.isEnabled = supportedTypes.contains(Markup.Type.LINK)
        urlIcon.isEnabled = url.isEnabled

        if (props?.markupTextColor != null) {
            val code = ThemeColor.values().first { it.title == props.markupTextColor }
            val default = resources.getColor(R.color.text_primary, null)
            val value = resources.dark(code, default)
            textColorCircle.tint(value)
        } else {
            val code = ThemeColor.values().find { it.title == props?.blockTextColor }
            if (code != null) {
                val default = resources.getColor(R.color.text_primary, null)
                val value = resources.dark(code, default)
                textColorCircle.tint(value)
            }
            else {
                textColorCircle.backgroundTintList = null
            }
        }

        if (props?.markupHighlightColor != null) {
            val code = ThemeColor.values().first { it.title == props.markupHighlightColor }
            if (code == ThemeColor.DEFAULT) {
                backgroundColorCircle.backgroundTintList = null
            } else {
                val default = resources.getColor(R.color.background_primary, null)
                val value = resources.light(code, default)
                backgroundColorCircle.tint(value)
            }
        } else {
            val code = ThemeColor.values().find { it.title == props?.blockBackroundColor }
            if (code != null) {
                val default = resources.getColor(R.color.background_primary, null)
                val value = resources.lighter(code, default)
                backgroundColorCircle.tint(value)
            }
            else
                backgroundColorCircle.backgroundTintList = null
        }

        if (isBackgroundColorSelected)
            selectedBackgroundCircle.visible()
        else
            selectedBackgroundCircle.invisible()

        if (isTextColorSelected)
            selectedTextColorCircle.visible()
        else
            selectedTextColorCircle.invisible()
    }

    sealed class Action

    sealed class Event {
        data class OnMarkupClicked(val type: Markup.Type) : Event()
        object OnColorClicked : Event()
    }
}