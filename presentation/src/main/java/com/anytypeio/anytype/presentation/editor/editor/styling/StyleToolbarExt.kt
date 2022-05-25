package com.anytypeio.anytype.presentation.editor.editor.styling

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment
import timber.log.Timber

fun Editor.Mode.getIds(): List<Id>? = when (this) {
    is Editor.Mode.Styling.Multi -> targets.toList()
    is Editor.Mode.Styling.Single -> listOf(target)
    else -> {
        Timber.e("Couldn't get ids of selected blocks, wrong Editor Mode : $this")
        null
    }
}

/**
 * Checks that all block text contents have the same value of style and returns state with this value
 * otherwise returns state with null
 */
fun List<Block.Content.Text>.getStyleTextToolbarState(): StyleToolbarState.Text {
    val result = map { it.style }.distinct()
    return if (result.size == 1) {
        StyleToolbarState.Text(
            textStyle = result[0]
        )
    } else {
        StyleToolbarState.Text(
            textStyle = null
        )
    }
}

/**
 * Checks that all blocks have the same value of background color and returns state with this value
 * otherwise returns state with null
 * We take as a statement that if block background value is null, then this block have default color
 */
fun List<Block>.getStyleBackgroundToolbarState(): StyleToolbarState.Background {
    val result = map { it.backgroundColor ?: ThemeColor.DEFAULT.title }.distinct()
    return if (result.size == 1) {
        StyleToolbarState.Background(
            background = result[0]
        )
    } else {
        StyleToolbarState.Background(
            background = null
        )
    }
}

/**
 * Checks that all blocks have the same value of background color and text color and returns state with this values
 * otherwise returns state with null
 * We take as a statement that if the block's background value is null
 * or the block's text color is null, then this block has default background or text colors
 */
fun List<Block>.getStyleColorBackgroundToolbarState(): StyleToolbarState.ColorBackground {
    val isAllText = all { it.content is Block.Content.Text }
    if (!isAllText) return StyleToolbarState.ColorBackground.empty()
    val resultColor = map { it.content.asText().color ?: ThemeColor.DEFAULT.title }.distinct()
    val resultBackground = map { it.backgroundColor ?: ThemeColor.DEFAULT.title }.distinct()
    return StyleToolbarState.ColorBackground(
        background = if (resultBackground.size == 1) resultBackground[0] else null,
        color = if (resultColor.size == 1) resultColor[0] else null
    )
}

fun List<Block.Content.Text>.getStyleOtherToolbarState(): StyleToolbarState.Other {
    val isSupportBold = all { it.isSupportBold() }
    val isBoldSelected = if (isSupportBold) {
        all { it.isBold() }
    } else false

    return StyleToolbarState.Other(
        isSupportBold = isSupportBold,
        isSupportItalic = true,
        isSupportCode = true,
        isSupportLinked = true,
        isSupportStrikethrough = true,
        isSupportAlignStart = all { it.alignmentSupport().contains(Alignment.START) },
        isSupportAlignCenter = all { it.alignmentSupport().contains(Alignment.CENTER) },
        isSupportAlignEnd = all { it.alignmentSupport().contains(Alignment.END) },
        isBoldSelected = isBoldSelected,
        isItalicSelected = all { it.isItalic() },
        isStrikethroughSelected = all { it.isStrikethrough() },
        isCodeSelected = all { it.isCode() },
        isLinkedSelected = all { it.isLinked() },
        isAlignCenterSelected = all { it.isAlignCenter() },
        isAlignStartSelected = all { it.isAlignStart() },
        isAlignEndSelected = all { it.isAlignEnd() }
    )
}

private fun Block.Content.Text.isSupportBold(): Boolean = when (this.style) {
    Block.Content.Text.Style.H1,
    Block.Content.Text.Style.H2,
    Block.Content.Text.Style.H3,
    Block.Content.Text.Style.H4,
    Block.Content.Text.Style.TITLE,
    Block.Content.Text.Style.CODE_SNIPPET -> false
    else -> true
}

private fun Block.Content.Text.alignmentSupport(): List<Alignment> = when (this.style) {
    Block.Content.Text.Style.P,
    Block.Content.Text.Style.H1,
    Block.Content.Text.Style.H2,
    Block.Content.Text.Style.H3,
    Block.Content.Text.Style.H4,
    Block.Content.Text.Style.TITLE -> listOf(Alignment.START, Alignment.CENTER, Alignment.END)
    Block.Content.Text.Style.QUOTE -> listOf(Alignment.START, Alignment.END)
    else -> emptyList()
}

fun Block.Content.Text.getSupportedMarkupTypes(): List<Markup.Type> = when (style) {
    Block.Content.Text.Style.P, Block.Content.Text.Style.QUOTE,
    Block.Content.Text.Style.BULLET, Block.Content.Text.Style.NUMBERED,
    Block.Content.Text.Style.TOGGLE, Block.Content.Text.Style.CHECKBOX -> {
        listOf(
            Markup.Type.BOLD,
            Markup.Type.ITALIC,
            Markup.Type.STRIKETHROUGH,
            Markup.Type.KEYBOARD,
            Markup.Type.LINK
        )
    }
    Block.Content.Text.Style.H1, Block.Content.Text.Style.H2,
    Block.Content.Text.Style.H3, Block.Content.Text.Style.H4 -> {
        listOf(
            Markup.Type.ITALIC,
            Markup.Type.STRIKETHROUGH,
            Markup.Type.KEYBOARD,
            Markup.Type.LINK
        )
    }
    else -> emptyList()
}

fun Block.Content.Text.isBold(): Boolean = marks.any { mark ->
    mark.type == Block.Content.Text.Mark.Type.BOLD && mark.range.first == 0 && mark.range.last == text.length
}

fun Block.Content.Text.isItalic(): Boolean = marks.any { mark ->
    mark.type == Block.Content.Text.Mark.Type.ITALIC && mark.range.first == 0 && mark.range.last == text.length
}

fun Block.Content.Text.isStrikethrough(): Boolean = marks.any { mark ->
    mark.type == Block.Content.Text.Mark.Type.STRIKETHROUGH && mark.range.first == 0 && mark.range.last == text.length
}

fun Block.Content.Text.isCode(): Boolean = marks.any { mark ->
    mark.type == Block.Content.Text.Mark.Type.KEYBOARD && mark.range.first == 0 && mark.range.last == text.length
}

fun Block.Content.Text.isLinked(): Boolean = marks.any { mark ->
    mark.type == Block.Content.Text.Mark.Type.LINK && mark.range.first == 0 && mark.range.last == text.length
}

fun Block.Content.Text.isAlignStart(): Boolean = this.align == Block.Align.AlignLeft
fun Block.Content.Text.isAlignCenter(): Boolean = this.align == Block.Align.AlignCenter
fun Block.Content.Text.isAlignEnd(): Boolean = this.align == Block.Align.AlignRight
