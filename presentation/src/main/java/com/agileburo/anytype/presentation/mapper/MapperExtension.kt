package com.agileburo.anytype.presentation.mapper

import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Block.Content.Text.Style
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.presentation.desktop.DashboardView

fun Block.toView(focused: Boolean = false): BlockView = when (val content = this.content) {
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
                        Block.Content.Text.Mark.Type.STRIKETHROUGH -> {
                            Markup.Mark(
                                from = mark.range.first,
                                to = mark.range.last,
                                type = Markup.Type.STRIKETHROUGH
                            )
                        }
                        else -> null
                    }
                },
                focused = focused
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
    is Block.Content.Image -> {
        BlockView.Picture(
            id = id
        )
    }
    else -> TODO()
}

fun HomeDashboard.toView(): List<DashboardView.Document> = children.map { id ->
    blocks.first { block -> block.id == id }.let { model ->
        DashboardView.Document(
            id = model.id,
            title = if (model.fields.hasName()) model.fields.name else "Untitled"
        )
    }
}