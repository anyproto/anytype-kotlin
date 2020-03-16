package com.agileburo.anytype.presentation.mapper

import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Block.Content.Text.Style
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.domain.emoji.Emojifier
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.presentation.desktop.DashboardView
import com.vdurmont.emoji.EmojiManager

fun Block.toView(
    focused: Boolean = false,
    numbers: Map<String, Int> = emptyMap(),
    urlBuilder: UrlBuilder
): BlockView = when (val content = this.content) {
    is Block.Content.Text -> {
        when (content.style) {
            Style.P -> BlockView.Paragraph(
                id = this.id,
                text = content.text,
                marks = mapMarks(content),
                focused = focused,
                color = content.color,
                backgroundColor = content.backgroundColor
            )
            Style.H1 -> BlockView.HeaderOne(
                id = id,
                text = content.text,
                color = content.color,
                backgroundColor = content.backgroundColor
            )
            Style.H2 -> BlockView.HeaderTwo(
                id = id,
                text = content.text,
                color = content.color,
                backgroundColor = content.backgroundColor
            )
            Style.H3, Style.H4 -> BlockView.HeaderThree(
                id = id,
                text = content.text,
                color = content.color,
                backgroundColor = content.backgroundColor
            )
            Style.TITLE -> BlockView.Title(
                id = id,
                text = content.text,
                emoji = fields.icon?.let { name ->
                    EmojiManager.getForAlias(
                        name.substring(
                            1,
                            name.lastIndex
                        )
                    ).unicode
                }
            )
            Style.QUOTE -> BlockView.Highlight(
                id = id,
                text = content.text
            )
            Style.CODE_SNIPPET -> BlockView.Code(
                id = id,
                snippet = content.text
            )
            Style.BULLET -> BlockView.Bulleted(
                id = id,
                text = content.text,
                indent = 0,
                marks = mapMarks(content),
                focused = focused,
                color = content.color,
                backgroundColor = content.backgroundColor
            )
            Style.NUMBERED -> BlockView.Numbered(
                id = id,
                text = content.text,
                number = numbers[id].toString(),
                focused = focused,
                color = content.color,
                backgroundColor = content.backgroundColor,
                indent = 0
            )
            Style.CHECKBOX -> BlockView.Checkbox(
                id = id,
                text = content.text,
                marks = mapMarks(content),
                isChecked = content.isChecked == true,
                color = content.color,
                backgroundColor = content.backgroundColor,
                focused = focused
            )
            Style.TOGGLE -> BlockView.Toggle(
                id = id,
                text = content.text,
                toggled = false,
                indent = 0
            )
        }
    }
    is Block.Content.File -> {
        when (content.type) {
            Block.Content.File.Type.IMAGE -> BlockView.Picture(
                id = id,
                url = urlBuilder.image(content.hash)
            )
            Block.Content.File.Type.FILE -> BlockView.File(
                id = id,
                size = content.size,
                name = content.name,
                mime = content.mime,
                url = urlBuilder.file(content.hash)
            )
            Block.Content.File.Type.VIDEO -> content.toVideoView(
                id = id,
                urlBuilder = urlBuilder
            )
            Block.Content.File.Type.NONE ->
                throw UnsupportedOperationException("File block type None, not implemented")
            else -> TODO()
        }
    }
    is Block.Content.Link -> {
        BlockView.Page(
            id = id,
            isEmpty = true,
            emoji = null,
            text = if (content.fields.hasName()) content.fields.name else null
        )
    }
    is Block.Content.Divider -> {
        BlockView.Divider(
            id = id
        )
    }
    else -> TODO()
}

fun Block.Content.File.toVideoView(id: String, urlBuilder: UrlBuilder): BlockView =
    when (this.state) {
        Block.Content.File.State.EMPTY -> BlockView.VideoEmpty(id = id)
        Block.Content.File.State.UPLOADING -> BlockView.VideoUpload(id = id)
        Block.Content.File.State.DONE -> BlockView.Video(
            id = id,
            size = size,
            name = name,
            mime = mime,
            hash = hash,
            url = urlBuilder.video(hash)
        )
        Block.Content.File.State.ERROR -> BlockView.VideoError(id = id)
        null -> throw NotImplementedError("File block state, should not be null")
    }

private fun mapMarks(content: Block.Content.Text): List<Markup.Mark> =
    content.marks.mapNotNull { mark ->
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
            Block.Content.Text.Mark.Type.TEXT_COLOR -> {
                Markup.Mark(
                    from = mark.range.first,
                    to = mark.range.last,
                    type = Markup.Type.TEXT_COLOR,
                    param = checkNotNull(mark.param)
                )
            }
            Block.Content.Text.Mark.Type.LINK -> {
                Markup.Mark(
                    from = mark.range.first,
                    to = mark.range.last,
                    type = Markup.Type.LINK,
                    param = checkNotNull(mark.param)
                )
            }
            Block.Content.Text.Mark.Type.BACKGROUND_COLOR -> {
                Markup.Mark(
                    from = mark.range.first,
                    to = mark.range.last,
                    type = Markup.Type.BACKGROUND_COLOR,
                    param = checkNotNull(mark.param)
                )
            }
            Block.Content.Text.Mark.Type.KEYBOARD -> {
                Markup.Mark(
                    from = mark.range.first,
                    to = mark.range.last,
                    type = Markup.Type.KEYBOARD
                )
            }
            else -> null
        }
    }

suspend fun HomeDashboard.toView(
    defaultTitle: String = "Untitled",
    emojifier: Emojifier
): List<DashboardView.Document> = children.mapNotNull { id ->
    blocks.find { block -> block.id == id }?.let { model ->
        when (val content = model.content) {
            is Block.Content.Link -> {
                if (content.type == Block.Content.Link.Type.PAGE) {
                    DashboardView.Document(
                        id = content.target,
                        title = if (content.fields.hasName()) content.fields.name else defaultTitle,
                        emoji = content.fields.icon?.let { name ->
                            if (name.isNotEmpty())
                                emojifier.fromShortName(name).unicode
                            else
                                null
                        }
                    )
                } else {
                    null
                }
            }
            else -> null
        }
    }
}