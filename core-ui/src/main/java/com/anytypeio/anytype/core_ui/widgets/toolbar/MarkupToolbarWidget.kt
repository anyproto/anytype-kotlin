package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetMarkupToolbarMainBinding
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.extensions.tint
import com.anytypeio.anytype.core_ui.extensions.veryLight
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.presentation.editor.markup.MarkupStyleDescriptor
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class MarkupToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    val binding = WidgetMarkupToolbarMainBinding.inflate(
        LayoutInflater.from(context), this
    )

    private fun bold() = binding.bold.clicks().map { Markup.Type.BOLD }
    private fun italic() = binding.italic.clicks().map { Markup.Type.ITALIC }
    private fun strike() = binding.strike.clicks().map { Markup.Type.STRIKETHROUGH }
    private fun code() = binding.code.clicks().map { Markup.Type.KEYBOARD }

    fun linkClicks() = binding.url.clicks().map { Markup.Type.LINK }
    fun colorClicks() = binding.color.clicks()
    fun highlightClicks() = binding.highlight.clicks()
    fun markup() = merge(bold(), italic(), strike(), code())

    fun setProps(
        props: MarkupStyleDescriptor?,
        supportedTypes: List<Markup.Type>,
        isBackgroundColorSelected: Boolean,
        isTextColorSelected: Boolean
    ) = with(binding) {
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
            val code = ThemeColor.values().first { it.code == props.markupTextColor }
            val default = resources.getColor(R.color.text_primary, null)
            val value = resources.dark(code, default)
            textColorCircle.tint(value)
        } else {
            val code = ThemeColor.values().find { it.code == props?.blockTextColor }
            if (code != null) {
                val default = resources.getColor(R.color.text_primary, null)
                val value = resources.dark(code, default)
                textColorCircle.tint(value)
            } else {
                textColorCircle.backgroundTintList = null
            }
        }

        if (props?.markupHighlightColor != null) {
            val code = ThemeColor.values().first { it.code == props.markupHighlightColor }
            if (code == ThemeColor.DEFAULT) {
                backgroundColorCircle.backgroundTintList = null
            } else {
                val default = resources.getColor(R.color.background_primary, null)
                val value = resources.light(code, default)
                backgroundColorCircle.tint(value)
            }
        } else {
            val code = ThemeColor.values().find { it.code == props?.blockBackroundColor }
            if (code != null) {
                val default = resources.getColor(R.color.background_primary, null)
                val value = resources.veryLight(code, default)
                backgroundColorCircle.tint(value)
            } else
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