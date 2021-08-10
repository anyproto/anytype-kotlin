package com.anytypeio.anytype.presentation.editor.editor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Block.Content.Text.Mark
import com.anytypeio.anytype.core_models.ext.addMark
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.editor.model.TextUpdate

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

    val new = Mark(
        range = range,
        type = when (type) {
            Markup.Type.BOLD -> Mark.Type.BOLD
            Markup.Type.ITALIC -> Mark.Type.ITALIC
            Markup.Type.STRIKETHROUGH -> Mark.Type.STRIKETHROUGH
            Markup.Type.TEXT_COLOR -> Mark.Type.TEXT_COLOR
            Markup.Type.LINK -> Mark.Type.LINK
            Markup.Type.BACKGROUND_COLOR -> Mark.Type.BACKGROUND_COLOR
            Markup.Type.KEYBOARD -> Mark.Type.KEYBOARD
            Markup.Type.MENTION -> Mark.Type.MENTION
        },
        param = param
    )

    val marks = content.marks.addMark(new)

    return copy(content = content.copy(marks = marks))
}