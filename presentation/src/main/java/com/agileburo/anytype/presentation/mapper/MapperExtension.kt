package com.agileburo.anytype.presentation.mapper

import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.domain.emoji.Emojifier
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.presentation.desktop.DashboardView

fun Block.Content.File.toPictureView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.Picture.Placeholder(id = id, indent = indent)
    Block.Content.File.State.UPLOADING -> BlockView.Picture.Upload(id = id, indent = indent)
    Block.Content.File.State.DONE -> BlockView.Picture.View(
        id = id,
        size = size,
        name = name,
        mime = mime,
        hash = hash,
        url = urlBuilder.video(hash),
        indent = indent
    )
    Block.Content.File.State.ERROR -> BlockView.Picture.Error(id = id, indent = indent)
    else -> throw IllegalStateException("Unexpected state: $state")
}

fun Block.Content.File.toVideoView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.Video.Placeholder(id = id, indent = indent)
    Block.Content.File.State.UPLOADING -> BlockView.Video.Upload(id = id, indent = indent)
    Block.Content.File.State.DONE -> BlockView.Video.View(
        id = id,
        size = size,
        name = name,
        mime = mime,
        hash = hash,
        url = urlBuilder.video(hash),
        indent = indent
    )
    Block.Content.File.State.ERROR -> BlockView.Video.Error(id = id, indent = indent)
    else -> throw IllegalStateException("Unexpected state: $state")
}

fun Block.Content.File.toFileView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.File.Placeholder(id = id, indent = indent)
    Block.Content.File.State.UPLOADING -> BlockView.File.Upload(id = id, indent = indent)
    Block.Content.File.State.DONE -> BlockView.File.View(
        id = id,
        size = size,
        name = name,
        mime = mime,
        hash = hash,
        url = urlBuilder.video(hash),
        indent = indent
    )
    Block.Content.File.State.ERROR -> BlockView.File.Error(id = id, indent = indent)
    else -> throw IllegalStateException("Unexpected state: $state")
}

fun Block.Content.Text.marks(): List<Markup.Mark> = marks.mapNotNull { mark ->
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