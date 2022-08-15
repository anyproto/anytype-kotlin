package com.anytypeio.anytype.presentation.editor.editor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Block.Content.Text.Mark
import com.anytypeio.anytype.core_models.ext.addClickableMark
import com.anytypeio.anytype.core_models.ext.addMark
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.ext.sortByType
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
        type =type.toCoreModel(),
        param = param
    )

    return copy(content = content.addMarkToContent(new))
}

fun Markup.Type.toCoreModel(): Mark.Type = when (this) {
    Markup.Type.BOLD -> Mark.Type.BOLD
    Markup.Type.ITALIC -> Mark.Type.ITALIC
    Markup.Type.STRIKETHROUGH -> Mark.Type.STRIKETHROUGH
    Markup.Type.TEXT_COLOR -> Mark.Type.TEXT_COLOR
    Markup.Type.LINK -> Mark.Type.LINK
    Markup.Type.BACKGROUND_COLOR -> Mark.Type.BACKGROUND_COLOR
    Markup.Type.KEYBOARD -> Mark.Type.KEYBOARD
    Markup.Type.MENTION -> Mark.Type.MENTION
    Markup.Type.OBJECT -> Mark.Type.OBJECT
    Markup.Type.UNDERLINE -> Mark.Type.UNDERLINE
}

fun Block.Content.Text.addMarkToContent(mark: Mark): Block.Content.Text {
    return if (mark.isClickableMark()) {
        this.copy(marks = marks.addClickableMark(mark).sortByType())
    } else {
        this.copy(marks = marks.addMark(mark))
    }
}