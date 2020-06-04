package com.agileburo.anytype.presentation.mapper

import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.model.UiBlock
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.config.DebugSettings
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.presentation.desktop.DashboardView
import com.agileburo.anytype.presentation.settings.EditorSettings

fun Block.Content.File.toPictureView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int,
    mode: BlockView.Mode
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.Picture.Placeholder(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.UPLOADING -> BlockView.Picture.Upload(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.DONE -> BlockView.Picture.View(
        id = id,
        size = size,
        name = name,
        mime = mime,
        hash = hash,
        url = urlBuilder.video(hash),
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.ERROR -> BlockView.Picture.Error(
        id = id,
        indent = indent,
        mode = mode
    )
    else -> throw IllegalStateException("Unexpected state: $state")
}

fun Block.Content.File.toVideoView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int,
    mode: BlockView.Mode
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.Video.Placeholder(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.UPLOADING -> BlockView.Video.Upload(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.DONE -> BlockView.Video.View(
        id = id,
        size = size,
        name = name,
        mime = mime,
        hash = hash,
        url = urlBuilder.video(hash),
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.ERROR -> BlockView.Video.Error(
        id = id,
        indent = indent,
        mode = mode
    )
    else -> throw IllegalStateException("Unexpected state: $state")
}

fun Block.Content.File.toFileView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int,
    mode: BlockView.Mode
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.File.Placeholder(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.UPLOADING -> BlockView.File.Upload(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.DONE -> BlockView.File.View(
        id = id,
        size = size,
        name = name,
        mime = mime,
        hash = hash,
        url = urlBuilder.video(hash),
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.ERROR -> BlockView.File.Error(
        id = id,
        indent = indent,
        mode = mode
    )
    else -> throw IllegalStateException("Unexpected state: $state")
}

fun Block.Align.toView(): BlockView.Alignment = when (this) {
    Block.Align.AlignLeft -> BlockView.Alignment.START
    Block.Align.AlignCenter -> BlockView.Alignment.CENTER
    Block.Align.AlignRight -> BlockView.Alignment.END
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

fun HomeDashboard.toView(): List<DashboardView.Document> = children.mapNotNull { id ->
    blocks.find { block -> block.id == id }?.let { model ->
        when (val content = model.content) {
            is Block.Content.Link -> {
                if (content.type == Block.Content.Link.Type.PAGE) {
                    if (details.details[content.target]?.isArchived != true) {
                        DashboardView.Document(
                            id = model.id,
                            target = content.target,
                            title = details.details[content.target]?.name,
                            emoji = details.details[content.target]?.icon?.let { name ->
                                if (name.isNotEmpty())
                                    name
                                else
                                    null
                            }
                        )
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            else -> null
        }
    }
}

fun UiBlock.style(): Block.Content.Text.Style = when (this) {
    UiBlock.TEXT -> Block.Content.Text.Style.P
    UiBlock.HEADER_ONE -> Block.Content.Text.Style.H1
    UiBlock.HEADER_TWO -> Block.Content.Text.Style.H2
    UiBlock.HEADER_THREE -> Block.Content.Text.Style.H3
    UiBlock.HIGHLIGHTED -> Block.Content.Text.Style.QUOTE
    UiBlock.CHECKBOX -> Block.Content.Text.Style.CHECKBOX
    UiBlock.BULLETED -> Block.Content.Text.Style.BULLET
    UiBlock.NUMBERED -> Block.Content.Text.Style.NUMBERED
    UiBlock.TOGGLE -> Block.Content.Text.Style.TOGGLE
    UiBlock.CODE -> Block.Content.Text.Style.CODE_SNIPPET
    else -> throw IllegalStateException("Could not extract style from block: $this")
}

fun DebugSettings.toView(): EditorSettings =
    EditorSettings(customContextMenu = this.isAnytypeContextMenuEnabled)