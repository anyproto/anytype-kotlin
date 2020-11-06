package com.anytypeio.anytype.middleware.converters

import anytype.model.Block
import anytype.model.Range
import com.anytypeio.anytype.data.auth.model.BlockEntity

typealias Mark = Block.Content.Text.Mark
typealias File = Block.Content.File
typealias FileState = Block.Content.File.State
typealias FileType = Block.Content.File.Type
typealias Link = Block.Content.Link
typealias LinkType = Block.Content.Link.Style
typealias Bookmark = Block.Content.Bookmark
typealias Marks = Block.Content.Text.Marks
typealias Text = Block.Content.Text
typealias Layout = Block.Content.Layout
typealias LayoutStyle = Block.Content.Layout.Style
typealias Divider = Block.Content.Div
typealias DividerStyle = Block.Content.Div.Style

//region block mapping

fun BlockEntity.block(): Block {
    return when (val content = content) {
        is BlockEntity.Content.Text -> {
            Block(
                text = content.text(),
                backgroundColor = content.backgroundColor.orEmpty(),
                align = content.align?.toMiddleware() ?: Block.Align.AlignLeft
            )
        }
        is BlockEntity.Content.Bookmark -> {
            Block(bookmark = content.bookmark())
        }
        is BlockEntity.Content.File -> {
            Block(file_ = content.file())
        }
        is BlockEntity.Content.Link -> {
            Block(link = content.link())
        }
        is BlockEntity.Content.Layout -> {
            Block(layout = content.layout())
        }
        is BlockEntity.Content.Divider -> {
            Block(div = content.divider())
        }
        else -> Block()
    }
}

//endregion

//region text block mapping

fun BlockEntity.Content.Text.text(): Text = Text(
    text = text,
    marks = marks(),
    style = style.toMiddleware(),
    color = color.orEmpty(),
    checked = isChecked ?: false
)

fun BlockEntity.Content.Text.marks(): Marks = Marks(marks = marks.map { it.mark() })

fun BlockEntity.Content.Text.Mark.mark(): Mark = when (type) {
    BlockEntity.Content.Text.Mark.Type.BOLD -> {
        Mark(
            type = Block.Content.Text.Mark.Type.Bold,
            range = range.range()
        )
    }
    BlockEntity.Content.Text.Mark.Type.ITALIC -> {
        Mark(
            type = Block.Content.Text.Mark.Type.Italic,
            range = range.range()
        )
    }
    BlockEntity.Content.Text.Mark.Type.STRIKETHROUGH -> {
        Mark(
            type = Block.Content.Text.Mark.Type.Strikethrough,
            range = range.range()
        )
    }
    BlockEntity.Content.Text.Mark.Type.TEXT_COLOR -> {
        Mark(
            type = Block.Content.Text.Mark.Type.TextColor,
            range = range.range(),
            param_ = param.orEmpty()
        )
    }
    BlockEntity.Content.Text.Mark.Type.LINK -> {
        Mark(
            type = Block.Content.Text.Mark.Type.Link,
            range = range.range(),
            param_ = param.orEmpty()
        )
    }
    BlockEntity.Content.Text.Mark.Type.BACKGROUND_COLOR -> {
        Mark(
            type = Block.Content.Text.Mark.Type.BackgroundColor,
            range = range.range(),
            param_ = param.orEmpty()
        )
    }
    BlockEntity.Content.Text.Mark.Type.KEYBOARD -> {
        Mark(
            type = Block.Content.Text.Mark.Type.Keyboard,
            range = range.range(),
        )
    }
    BlockEntity.Content.Text.Mark.Type.MENTION -> {
        Mark(
            type = Block.Content.Text.Mark.Type.Mention,
            range = range.range(),
            param_ = param.orEmpty()
        )
    }
    else -> throw IllegalStateException("Unsupported mark type: ${type.name}")
}

//endregion

//region bookmark block mapping

fun BlockEntity.Content.Bookmark.bookmark(): Bookmark = Bookmark(
    description = description.orEmpty(),
    faviconHash = favicon.orEmpty(),
    title = title.orEmpty(),
    url = url.orEmpty(),
    imageHash = image.orEmpty()
)

//endregion

//region file block mapping

fun BlockEntity.Content.File.file(): File = File(
    hash = hash.orEmpty(),
    name = name.orEmpty(),
    mime = mime.orEmpty(),
    size = size ?: 0,
    state = state?.state() ?: Block.Content.File.State.Empty,
    type = type?.type() ?: Block.Content.File.Type.None
)

fun BlockEntity.Content.File.State.state(): FileState = when (this) {
    BlockEntity.Content.File.State.EMPTY -> FileState.Empty
    BlockEntity.Content.File.State.UPLOADING -> FileState.Uploading
    BlockEntity.Content.File.State.DONE -> FileState.Done
    BlockEntity.Content.File.State.ERROR -> FileState.Error
}

fun BlockEntity.Content.File.Type.type(): FileType = when (this) {
    BlockEntity.Content.File.Type.NONE -> FileType.None
    BlockEntity.Content.File.Type.FILE -> FileType.File
    BlockEntity.Content.File.Type.IMAGE -> FileType.Image
    BlockEntity.Content.File.Type.VIDEO -> FileType.Video
}

//endregion

//region link mapping

fun BlockEntity.Content.Link.link(): Link = Link(
    targetBlockId = target,
    style = type.type(),
    fields = fields.map
)

fun BlockEntity.Content.Link.Type.type() : LinkType = when(this) {
    BlockEntity.Content.Link.Type.ARCHIVE -> LinkType.Archive
    BlockEntity.Content.Link.Type.DASHBOARD -> LinkType.Dashboard
    BlockEntity.Content.Link.Type.DATA_VIEW -> LinkType.Dataview
    BlockEntity.Content.Link.Type.PAGE -> LinkType.Page
}

//endregion

//region layout mapping

fun BlockEntity.Content.Layout.layout(): Layout = when (type) {
    BlockEntity.Content.Layout.Type.ROW -> Layout(style = LayoutStyle.Row)
    BlockEntity.Content.Layout.Type.COLUMN -> Layout(style = LayoutStyle.Column)
    BlockEntity.Content.Layout.Type.DIV -> Layout(style = LayoutStyle.Div)
    BlockEntity.Content.Layout.Type.HEADER -> Layout(style = LayoutStyle.Header)
}

//endregion

//region divider mapping

fun BlockEntity.Content.Divider.divider(): Divider = when (style) {
    BlockEntity.Content.Divider.Style.LINE -> Divider(style = Block.Content.Div.Style.Line)
    BlockEntity.Content.Divider.Style.DOTS -> Divider(style = Block.Content.Div.Style.Dots)
}

//endregion

//region other mapping

fun IntRange.range(): Range = Range(from = first, to = last)

//endregion