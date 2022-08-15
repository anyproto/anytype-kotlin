package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.anytypeio.anytype.core_ui.databinding.WidgetBlockStyleExtraBinding
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.presentation.editor.editor.styling.StyleToolbarState
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class StyleToolbarExtraWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {

    val binding = WidgetBlockStyleExtraBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    val actions: Flow<StylingEvent> = merge(
        binding.bold.clicks().map { StylingEvent.Markup.Bold },
        binding.italic.clicks().map { StylingEvent.Markup.Italic },
        binding.strikethrough.clicks().map { StylingEvent.Markup.StrikeThrough },
        binding.code.clicks().map { StylingEvent.Markup.Code },
        binding.alignmentLeft.clicks().map { StylingEvent.Alignment.Left },
        binding.alignmentMiddle.clicks().map { StylingEvent.Alignment.Center },
        binding.alignmentRight.clicks().map { StylingEvent.Alignment.Right },
        binding.setUrl.clicks().map { StylingEvent.Markup.Link },
        binding.underline.clicks().map { StylingEvent.Markup.Underline }
    )

    fun setProperties(
        state: StyleToolbarState.Other
    ) = with(binding) {
        bold.isSelected = state.isBoldSelected
        italic.isSelected = state.isItalicSelected
        strikethrough.isSelected = state.isStrikethroughSelected
        code.isSelected = state.isCodeSelected
        underline.isSelected = state.isUnderlineSelected
        alignmentLeft.isSelected = state.isAlignStartSelected
        alignmentMiddle.isSelected = state.isAlignCenterSelected
        alignmentRight.isSelected = state.isAlignEndSelected

        alignmentLeft.isEnabled = state.isSupportAlignStart
        alignmentLeftIcon.isEnabled = alignmentLeft.isEnabled
        alignmentRight.isEnabled = state.isSupportAlignEnd
        alignmentRightIcon.isEnabled = alignmentRight.isEnabled
        alignmentMiddle.isEnabled = state.isSupportAlignCenter
        alignmentMiddleIcon.isEnabled = alignmentMiddle.isEnabled
        bold.isEnabled = state.isSupportBold
        boldIcon.isEnabled = bold.isEnabled
        italic.isEnabled = state.isSupportItalic
        italicIcon.isEnabled = italic.isEnabled
        strikethrough.isEnabled = state.isSupportStrikethrough
        strikethroughIcon.isEnabled = strikethrough.isEnabled
        underline.isEnabled = state.isSupportUnderline
        underlineIcon.isEnabled = underline.isEnabled
        code.isEnabled = state.isSupportCode
        codeIcon.isEnabled = code.isEnabled
        setUrl.isEnabled = state.isSupportLinked
        setUrlIcon.isEnabled = setUrl.isEnabled
    }
}