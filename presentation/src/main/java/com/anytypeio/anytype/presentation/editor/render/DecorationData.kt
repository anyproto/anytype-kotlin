package com.anytypeio.anytype.presentation.editor.render

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.model.Indent

/**
 * Mapping indent level to gathered information about parents (ancestors) styles during document graph traversal.
 */
typealias NestedDecorationData = List<DecorationData>

/**
 * Used for gathering information about parents (ancestors) styles while document graph traversal.
 */
data class DecorationData(
    val style: Style = Style.None,
    val background: ThemeColor = ThemeColor.DEFAULT
) {
    sealed class Style {
        object None : Style()

        /**
         * @property [start] id of the starting block for highlight style
         * @property [end] id of the last block for highlight style.
         * [end] value is calculated while graph traversal and depends on the depth / indent level.
         */
        data class Highlight(val start: Id, val end: Id) : Style()
        sealed class Header : Style() {
            object H1 : Header()
            object H2 : Header()
            object H3 : Header()
        }
        /**
         * Add style for [Block.Content.Text.Style.CALLOUT] when it is supported by [DefaultBlockViewRenderer]
         */
        //
        //data class Callout(val start: Id, val end: Id) : Style()
    }
}

fun buildNestedDecorationData(
    parentScheme: NestedDecorationData,
    block: Block,
    currentDecoration: DecorationData = DecorationData(
        style = DecorationData.Style.None,
        background = block.parseThemeBackgroundColor()
    )
): NestedDecorationData = if (BuildConfig.NESTED_DECORATION_ENABLED) buildList {
    // Normalizing parent scheme
    parentScheme.forEach { holder ->
        when (val style = holder.style) {
            is DecorationData.Style.Highlight -> {
                if (block.id == style.end) {
                    // Block is a closing block for a highlight style
                    if (block.children.isEmpty()) {
                        add(holder)
                    } else {
                        // But it has children, so its last child is supposed to be a closing block.
                        add(holder.copy(style = style.copy(end = block.children.last())))
                    }
                } else {
                    // It is not a closing block for highlight, no need to normalize it.
                    add(holder)
                }
            }
            else -> add(holder)
        }
    }
    // Adding current style
    add(currentDecoration)
} else emptyList()

fun NestedDecorationData.toBlockViewDecoration(block: Block): List<BlockView.Decoration> {
    return map { holder ->
        when (val style = holder.style) {
            is DecorationData.Style.Highlight -> {
                if (style.start == style.end) {
                    BlockView.Decoration(
                        style = BlockView.Decoration.Style.Highlight.Start,
                        background = holder.background
                    )
                } else if (style.end == block.id) {
                    BlockView.Decoration(
                        style = BlockView.Decoration.Style.Highlight.End,
                        background = holder.background
                    )
                } else {
                    BlockView.Decoration(
                        style = BlockView.Decoration.Style.Highlight.Middle,
                        background = holder.background
                    )
                }
            }
            is DecorationData.Style.Header.H1 -> {
                BlockView.Decoration(
                    style = BlockView.Decoration.Style.Header.H1,
                    background = holder.background
                )
            }
            is DecorationData.Style.Header.H2 -> {
                BlockView.Decoration(
                    style = BlockView.Decoration.Style.Header.H2,
                    background = holder.background
                )
            }
            is DecorationData.Style.Header.H3 -> {
                BlockView.Decoration(
                    style = BlockView.Decoration.Style.Header.H3,
                    background = holder.background
                )
            }
            is DecorationData.Style.None -> {
                BlockView.Decoration(
                    style = BlockView.Decoration.Style.None,
                    background = holder.background
                )
            }
        }
    }
}

fun normalizeNestedDecorationData(
    block: Block,
    parentScheme: NestedDecorationData
): NestedDecorationData {
    return parentScheme.map { holder ->
        when (val style = holder.style) {
            is DecorationData.Style.Highlight -> {
                if (block.id == style.end) {
                    if (block.children.isEmpty()) {
                        holder
                    } else {
                        holder.copy(
                            style = style.copy(end = block.children.last())
                        )
                    }
                } else {
                    holder
                }
            }
            else -> holder
        }
    }
}

fun Block.parseThemeBackgroundColor() : ThemeColor {
    return backgroundColor?.let { ThemeColor.fromCode(it) } ?: ThemeColor.DEFAULT
}