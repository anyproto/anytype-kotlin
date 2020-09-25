package com.anytypeio.anytype.presentation.page.editor

import com.anytypeio.anytype.core_ui.common.Markup
import com.anytypeio.anytype.core_ui.common.ThemeColor
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.block.model.Block.Content.Text.Mark
import com.anytypeio.anytype.domain.ext.addMark
import com.anytypeio.anytype.domain.ext.content
import com.anytypeio.anytype.presentation.page.model.TextUpdate

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

    val marks = content.marks.addMark(new).filter { mark ->
        if (mark.type == Mark.Type.BACKGROUND_COLOR || mark.type == Mark.Type.TEXT_COLOR) {
            mark.param != ThemeColor.DEFAULT.title
        } else {
            true
        }
    }

    return copy(content = content.copy(marks = marks))
}