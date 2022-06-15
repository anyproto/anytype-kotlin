package com.anytypeio.anytype.presentation.editor.render

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.model.Indent

/**
 * Mapping indent level to gathered information about parents (ancestors) styles during document graph traversal.
 */
typealias DecorationScheme = Map<Indent, DecorationHolder>

/**
 * Used for gathering information about parents (ancestors) styles while document graph traversal.
 * // TODO better naming needed - this one is confusing (you start to think about ViewHolder)
 */
data class DecorationHolder(
    val style: Style,
    val background: String? = null
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

fun buildDefaultDecorationScheme(
    parentScheme: DecorationScheme,
    block: Block,
    currentIndent: Int,
    currentDecoration: DecorationHolder = DecorationHolder(
        style = DecorationHolder.Style.None,
        background = block.backgroundColor
    )
): DecorationScheme = if (BuildConfig.NESTED_DECORATION_ENABLED) buildMap {
    // Normalizing parent scheme
    parentScheme.forEach { (indent, holder) ->
        when (val style = holder.style) {
            is DecorationHolder.Style.Highlight -> {
                if (block.id == style.end) {
                    // Block is a closing block for a highlight style
                    if (block.children.isEmpty()) {
                        put(
                            key = indent,
                            value = holder
                        )
                    } else {
                        // But it has children, so its last child is supposed to be a closing block.
                        put(
                            key = indent,
                            value = holder.copy(style = style.copy(end = block.children.last()))
                        )
                    }
                } else {
                    // It is not a closing block for highlight, no need to normalize it.
                    put(
                        key = indent,
                        value = holder
                    )
                }
            }
            else -> put(
                key = indent,
                value = holder
            )
        }
    }
    // Adding current style
    put(
        key = currentIndent,
        value = currentDecoration
    )
} else emptyMap()

fun DecorationScheme.toDecor(block: Block): Map<Indent, BlockView.Decor> {
    return entries.associate { (indent, holder) ->
        when (val style = holder.style) {
            is DecorationHolder.Style.Highlight -> {
                if (style.start == style.end) {
                    indent to BlockView.Decor(
                        style = BlockView.Decor.Style.Highlight.Start,
                        background = holder.background
                    )
                } else if (style.end == block.id) {
                    indent to BlockView.Decor(
                        style = BlockView.Decor.Style.Highlight.End,
                        background = holder.background
                    )
                } else {
                    indent to BlockView.Decor(
                        style = BlockView.Decor.Style.Highlight.Middle,
                        background = holder.background
                    )
                }
            }
            is DecorationHolder.Style.Header.H1 -> {
                indent to BlockView.Decor(
                    style = BlockView.Decor.Style.Header.H1,
                    background = holder.background
                )
            }
            is DecorationHolder.Style.Header.H2 -> {
                indent to BlockView.Decor(
                    style = BlockView.Decor.Style.Header.H2,
                    background = holder.background
                )
            }
            is DecorationHolder.Style.Header.H3 -> {
                indent to BlockView.Decor(
                    style = BlockView.Decor.Style.Header.H3,
                    background = holder.background
                )
            }
            is DecorationHolder.Style.None -> {
                indent to BlockView.Decor(
                    style = BlockView.Decor.Style.None,
                    background = holder.background
                )
            }
        }
    }
}

fun normalizeDecorationScheme(block: Block, parentScheme: DecorationScheme): DecorationScheme {
    return parentScheme.entries.associate { (indent, holder) ->
        when (val style = holder.style) {
            is DecorationHolder.Style.Highlight -> {
                if (block.id == style.end) {
                    if (block.children.isEmpty()) {
                        indent to holder
                    } else {
                        indent to holder.copy(
                            style = style.copy(end = block.children.last())
                        )
                    }
                } else {
                    indent to holder
                }
            }
            else -> indent to holder
        }
    }
}