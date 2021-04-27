package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.tint
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.page.editor.Markup
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.page.markup.MarkupStyleDescriptor
import kotlinx.android.synthetic.main.widget_markup_toolbar_main.view.*
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

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

    fun markup() = flowOf(bold(), italic(), strike(), code()).flattenMerge()
    private fun bold() = bold.clicks().map { Markup.Type.BOLD }
    private fun italic() = italic.clicks().map { Markup.Type.ITALIC }
    private fun strike() = strike.clicks().map { Markup.Type.STRIKETHROUGH }
    fun linkClicks() = url.clicks().map { Markup.Type.LINK }
    private fun code() = code.clicks().map { Markup.Type.KEYBOARD }

    fun colorClicks() = color.clicks()
    fun highlightClicks() = highlight.clicks()

    fun setProps(
        props: MarkupStyleDescriptor?,
        isBackgroundColorSelected: Boolean,
        isTextColorSelected: Boolean
    ) {
        bold.isSelected = props?.isBold ?: false
        italic.isSelected = props?.isItalic ?: false
        strike.isSelected = props?.isStrikethrough ?: false
        code.isSelected = props?.isCode ?: false
        url.isSelected = props?.isLinked ?: false

        if (props?.markupTextColor != null) {
            val code = ThemeColor.values().first { it.title == props.markupTextColor }
            textColorCircle.tint(code.text)
        } else {
            val code = ThemeColor.values().find { it.title == props?.blockTextColor }
            if (code != null)
                textColorCircle.tint(code.text)
            else
                textColorCircle.backgroundTintList = null
        }

        if (props?.markupHighlightColor != null) {
            val code = ThemeColor.values().first { it.title == props.markupHighlightColor }
            if (code == ThemeColor.DEFAULT)
                backgroundColorCircle.backgroundTintList = null
            else
                backgroundColorCircle.tint(code.background)
        } else {
            val code = ThemeColor.values().find { it.title == props?.blockBackroundColor }
            if (code != null)
                backgroundColorCircle.tint(code.background)
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