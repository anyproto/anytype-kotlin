package com.agileburo.anytype.presentation.page.editor

import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.ext.addMark
import com.agileburo.anytype.domain.ext.content
import com.agileburo.anytype.domain.misc.Reducer
import com.agileburo.anytype.presentation.page.model.TextUpdate

sealed class Transformation {

    data class ApplyMarkup(
        val target: Id,
        val type: Markup.Type,
        val range: IntRange,
        val param: String?
    ) : Reducer<List<Block>, ApplyMarkup> {

        override fun reduce(state: List<Block>, event: ApplyMarkup): List<Block> {

            val target = state.first { it.id == target }
            val content = target.content<Block.Content.Text>()

            val mark = Block.Content.Text.Mark(
                range = range,
                type = when (type) {
                    Markup.Type.BOLD -> Block.Content.Text.Mark.Type.BOLD
                    Markup.Type.ITALIC -> Block.Content.Text.Mark.Type.ITALIC
                    Markup.Type.STRIKETHROUGH -> Block.Content.Text.Mark.Type.STRIKETHROUGH
                    Markup.Type.TEXT_COLOR -> Block.Content.Text.Mark.Type.TEXT_COLOR
                    Markup.Type.LINK -> Block.Content.Text.Mark.Type.LINK
                    Markup.Type.BACKGROUND_COLOR -> Block.Content.Text.Mark.Type.BACKGROUND_COLOR
                    Markup.Type.KEYBOARD -> Block.Content.Text.Mark.Type.KEYBOARD
                    Markup.Type.MENTION -> Block.Content.Text.Mark.Type.MENTION
                },
                param = param
            )

            val marks = content.marks.addMark(mark)

            val new = target.copy(
                content = content.copy(marks = marks)
            )

            return state.map { block ->
                if (block.id != event.target)
                    block
                else
                    new
            }
        }
    }

    data class UpdateText(
        val update: TextUpdate
    ) : Reducer<List<Block>, UpdateText> {

        override fun reduce(
            state: List<Block>,
            event: UpdateText
        ): List<Block> = state.map { block ->
            if (block.id == update.target) {
                block.copy(
                    content = block.content<Block.Content.Text>().copy(
                        text = update.text,
                        marks = update.markup.filter {
                            it.range.first != it.range.last
                        }
                    )
                )
            } else
                block
        }
    }

}

fun Block.updateText(update: TextUpdate): Block {
    return copy(
        content = content<Block.Content.Text>().copy(
            text = update.text,
            marks = update.markup.filter {
                it.range.first != it.range.last
            }
        )
    )
}

fun Block.markup(
    type: Markup.Type,
    range: IntRange,
    param: String?
): Block {

    val content = content<Block.Content.Text>()

    val mark = Block.Content.Text.Mark(
        range = range,
        type = when (type) {
            Markup.Type.BOLD -> Block.Content.Text.Mark.Type.BOLD
            Markup.Type.ITALIC -> Block.Content.Text.Mark.Type.ITALIC
            Markup.Type.STRIKETHROUGH -> Block.Content.Text.Mark.Type.STRIKETHROUGH
            Markup.Type.TEXT_COLOR -> Block.Content.Text.Mark.Type.TEXT_COLOR
            Markup.Type.LINK -> Block.Content.Text.Mark.Type.LINK
            Markup.Type.BACKGROUND_COLOR -> Block.Content.Text.Mark.Type.BACKGROUND_COLOR
            Markup.Type.KEYBOARD -> Block.Content.Text.Mark.Type.KEYBOARD
            Markup.Type.MENTION -> Block.Content.Text.Mark.Type.MENTION
        },
        param = param
    )

    val marks = content.marks.addMark(mark)

    return copy(content = content.copy(marks = marks))
}