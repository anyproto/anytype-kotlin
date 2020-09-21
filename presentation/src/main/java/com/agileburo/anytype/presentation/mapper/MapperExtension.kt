package com.agileburo.anytype.presentation.mapper

import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.navigation.PageLinkView
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.model.UiBlock
import com.agileburo.anytype.core_ui.widgets.toolbar.adapter.Mention
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.config.DebugSettings
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.navigation.DocumentInfo
import com.agileburo.anytype.presentation.desktop.DashboardView
import com.agileburo.anytype.presentation.settings.EditorSettings

fun Block.Content.File.toPictureView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int,
    mode: BlockView.Mode
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.MediaPlaceholder.Picture(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.UPLOADING -> BlockView.Upload.Picture(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.DONE -> BlockView.Media.Picture(
        id = id,
        size = size,
        name = name,
        mime = mime,
        hash = hash,
        url = urlBuilder.image(hash),
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.ERROR -> BlockView.Error.Picture(
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
    Block.Content.File.State.EMPTY -> BlockView.MediaPlaceholder.Video(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.UPLOADING -> BlockView.Upload.Video(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.DONE -> BlockView.Media.Video(
        id = id,
        size = size,
        name = name,
        mime = mime,
        hash = hash,
        url = urlBuilder.video(hash),
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.ERROR -> BlockView.Error.Video(
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
    Block.Content.File.State.EMPTY -> BlockView.MediaPlaceholder.File(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.UPLOADING -> BlockView.Upload.File(
        id = id,
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.DONE -> BlockView.Media.File(
        id = id,
        size = size,
        name = name,
        mime = mime,
        hash = hash,
        url = urlBuilder.video(hash),
        indent = indent,
        mode = mode
    )
    Block.Content.File.State.ERROR -> BlockView.Error.File(
        id = id,
        indent = indent,
        mode = mode
    )
    else -> throw IllegalStateException("Unexpected state: $state")
}

fun Block.Align.toView(): Alignment = when (this) {
    Block.Align.AlignLeft -> Alignment.START
    Block.Align.AlignCenter -> Alignment.CENTER
    Block.Align.AlignRight -> Alignment.END
}

fun Block.Content.Text.marks(
    urlBuilder: UrlBuilder? = null,
    details: Block.Details? = null
): List<Markup.Mark> = marks.mapNotNull { mark ->
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
        Block.Content.Text.Mark.Type.MENTION -> {

            val emoji: String?
            val image: String?

            if (details != null) {
                emoji = details.details[mark.param]?.iconEmoji?.let { icon ->
                    if (icon.isEmpty()) null else icon
                }
                image = details.details[mark.param]?.iconImage?.let { icon ->
                    if (icon.isEmpty()) null else icon
                }
            } else {
                emoji = null
                image = null
            }

            Markup.Mark(
                from = mark.range.first,
                to = mark.range.last,
                type = Markup.Type.MENTION,
                param = mark.param,
                extras = mapOf(
                    "image" to image?.let { urlBuilder?.thumbnail(it) },
                    "emoji" to emoji
                )
            )
        }
        else -> null
    }
}

fun List<Block>.toDashboardViews(
    details: Block.Details = Block.Details(),
    builder: UrlBuilder
): List<DashboardView> = this.mapNotNull { block ->
    when (val content = block.content) {
        is Block.Content.Smart -> {
            when (content.type) {
                Block.Content.Smart.Type.PROFILE -> {
                    DashboardView.Profile(
                        id = block.id,
                        name = details.details[block.id]?.name.orEmpty(),
                        avatar = details.details[block.id]?.iconImage.let {
                            if (it.isNullOrEmpty()) null
                            else builder.image(it)
                        }
                    )
                }
                else -> null
            }
        }
        is Block.Content.Link -> {
            when (content.type) {
                Block.Content.Link.Type.PAGE -> content.toPageView(block.id, details, builder)
                Block.Content.Link.Type.ARCHIVE -> content.toArchiveView(block.id, details)
                else -> null
            }
        }
        else -> null
    }
}

fun Block.Content.Link.toArchiveView(
    id: String,
    details: Block.Details
): DashboardView.Archive? {
    return DashboardView.Archive(
        id = id,
        target = target,
        title = details.details[target]?.name.orEmpty()
    )
}

fun Block.Content.Link.toPageView(
    id: String,
    details: Block.Details,
    builder: UrlBuilder
): DashboardView.Document? {
    return DashboardView.Document(
        id = id,
        target = target,
        title = details.details[target]?.name,
        emoji = details.details[target]?.iconEmoji?.let { name ->
            if (name.isNotEmpty()) name else null
        },
        image = details.details[target]?.iconImage?.let { name ->
            if (name.isNotEmpty()) builder.image(name) else null
        },
        isArchived = details.details[target]?.isArchived ?: false
    )
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

fun DocumentInfo.toView(urlBuilder: UrlBuilder): PageLinkView = PageLinkView(
    id = id,
    title = fields.name.orEmpty(),
    subtitle = snippet.orEmpty(),
    image = fields.toImageView(urlBuilder),
    emoji = fields.toEmojiView()
)

fun Block.Fields.toImageView(urlBuilder: UrlBuilder): String? = this.iconImage.let { url ->
    if (url.isNullOrBlank()) null else urlBuilder.image(url)
}

fun Block.Fields.toEmojiView(): String? = this.iconEmoji.let { emoji ->
    if (emoji.isNullOrBlank()) null else emoji
}

fun DocumentInfo.toMentionView(urlBuilder: UrlBuilder) = Mention(
    id = id,
    title = fields.getName(),
    image = fields.iconImage?.let { if (it.isNotEmpty()) urlBuilder.thumbnail(it) else null },
    emoji = fields.iconEmoji
)

fun Block.Fields.getName(): String =
    this.name.let { name ->
        if (name.isNullOrBlank()) "Untitled" else name
    }

fun Markup.Mark.mark(): Block.Content.Text.Mark = when (type) {
    Markup.Type.BOLD -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.BOLD
    )
    Markup.Type.ITALIC -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.ITALIC
    )
    Markup.Type.STRIKETHROUGH -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
    )
    Markup.Type.TEXT_COLOR -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.TEXT_COLOR,
        param = param
    )
    Markup.Type.BACKGROUND_COLOR -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.BACKGROUND_COLOR,
        param = param
    )
    Markup.Type.LINK -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.LINK,
        param = param
    )
    Markup.Type.KEYBOARD -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.KEYBOARD
    )
    Markup.Type.MENTION -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.MENTION,
        param = param
    )
}