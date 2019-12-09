package com.agileburo.anytype.presentation.mapper

import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Block.Content.Text.Style

fun Block.toView(): BlockView = when (val content = this.content) {
    is Block.Content.Text -> {
        when (content.style) {
            Style.P -> BlockView.Text(
                id = this.id,
                text = content.text,
                marks = content.marks.mapNotNull { mark ->
                    when (mark.type) {
                        Block.Content.Text.Mark.Type.ITALIC -> {
                            Markup.Mark(
                                from = mark.range.first,
                                to = mark.range.last,
                                type = Markup.Type.ITALIC
                            )
                        }
                        Block.Content.Text.Mark.Type.BOLD -> {
                            Markup.Mark(
                                from = mark.range.first,
                                to = mark.range.last,
                                type = Markup.Type.BOLD
                            )
                        }
                        else -> null
                    }
                }
            )
            Style.H1 -> BlockView.HeaderOne(
                id = this.id,
                text = content.text
            )
            Style.H2 -> BlockView.HeaderTwo(
                id = this.id,
                text = content.text
            )
            Style.H3, Style.H4 -> BlockView.HeaderThree(
                id = this.id,
                text = content.text
            )
            Style.TITLE -> BlockView.Title(
                id = this.id,
                text = content.text
            )
            Style.QUOTE -> BlockView.Highlight(
                id = this.id,
                text = content.text
            )
            Style.CODE_SNIPPET -> BlockView.Code(
                id = this.id,
                snippet = content.text
            )
            Style.BULLET -> BlockView.Bulleted(
                id = this.id,
                text = content.text,
                indent = 0
            )
            Style.NUMBERED -> BlockView.Numbered(
                id = this.id,
                text = content.text,
                number = "0",
                indent = 0
            )
            Style.TOGGLE -> BlockView.Toggle(
                id = this.id,
                text = content.text,
                toggled = false,
                indent = 0
            )
        }
    }
    else -> TODO()
}