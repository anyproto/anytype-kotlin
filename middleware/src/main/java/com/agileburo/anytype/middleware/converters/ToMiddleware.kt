package com.agileburo.anytype.middleware.converters

import anytype.model.Models.Block
import anytype.model.Models.Range
import com.agileburo.anytype.data.auth.model.BlockEntity
import com.google.protobuf.Struct
import com.google.protobuf.Value

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

    val builder = Block.newBuilder()

    builder.id = id

    when (val content = content) {
        is BlockEntity.Content.Text -> {
            builder.text = content.text()
        }
        is BlockEntity.Content.Bookmark -> {
            builder.bookmark = content.bookmark()
        }
        is BlockEntity.Content.File -> {
            builder.file = content.file()
        }
        is BlockEntity.Content.Link -> {
            builder.link = content.link()
        }
        is BlockEntity.Content.Layout -> {
            builder.layout = content.layout()
        }
        is BlockEntity.Content.Divider -> {
            builder.div = content.divider()
        }
    }

    return builder.build()
}

//endregion

//region text block mapping

fun BlockEntity.Content.Text.text(): Text {
    return Text
        .newBuilder()
        .setText(text)
        .setMarks(marks())
        .setStyle(style.toMiddleware())
        .build()
}

fun BlockEntity.Content.Text.marks(): Marks {
    return Marks
        .newBuilder()
        .addAllMarks(marks.map { it.mark() })
        .build()
}

fun BlockEntity.Content.Text.Mark.mark(): Mark = when (type) {
    BlockEntity.Content.Text.Mark.Type.BOLD -> {
        Mark.newBuilder()
            .setType(Block.Content.Text.Mark.Type.Bold)
            .setRange(range.range())
            .build()
    }
    BlockEntity.Content.Text.Mark.Type.ITALIC -> {
        Mark.newBuilder()
            .setType(Block.Content.Text.Mark.Type.Italic)
            .setRange(range.range())
            .build()
    }
    BlockEntity.Content.Text.Mark.Type.STRIKETHROUGH -> {
        Mark.newBuilder()
            .setType(Block.Content.Text.Mark.Type.Strikethrough)
            .setRange(range.range())
            .build()
    }
    BlockEntity.Content.Text.Mark.Type.TEXT_COLOR -> {
        Mark.newBuilder()
            .setType(Block.Content.Text.Mark.Type.TextColor)
            .setRange(range.range())
            .setParam(param)
            .build()
    }
    BlockEntity.Content.Text.Mark.Type.LINK -> {
        Mark.newBuilder()
            .setType(Block.Content.Text.Mark.Type.Link)
            .setRange(range.range())
            .setParam(param)
            .build()
    }
    BlockEntity.Content.Text.Mark.Type.BACKGROUND_COLOR -> {
        Mark.newBuilder()
            .setType(Block.Content.Text.Mark.Type.BackgroundColor)
            .setRange(range.range())
            .setParam(param)
            .build()
    }
    BlockEntity.Content.Text.Mark.Type.KEYBOARD -> {
        Mark.newBuilder()
            .setType(Block.Content.Text.Mark.Type.Keyboard)
            .setRange(range.range())
            .build()
    }
    else -> throw IllegalStateException("Unsupported mark type: ${type.name}")
}

//endregion

//region bookmark block mapping

fun BlockEntity.Content.Bookmark.bookmark(): Bookmark {
    val builder = Bookmark.newBuilder()
    description?.let { builder.setDescription(it) }
    favicon?.let { builder.setFaviconHash(it) }
    title?.let { builder.setTitle(it) }
    url?.let { builder.setUrl(it) }
    image?.let { builder.setImageHash(it) }
    return builder.build()
}

//endregion

//region file block mapping

fun BlockEntity.Content.File.file(): File {
    val builder = File.newBuilder()
    hash?.let { builder.setHash(it) }
    name?.let { builder.setName(it) }
    mime?.let { builder.setMime(it) }
    size?.let { builder.setSize(it) }
    state?.let { builder.setState(it.state()) }
    type?.let { builder.setType(it.type()) }
    return builder.build()
}

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

fun BlockEntity.Content.Link.link(): Link {
    return Link.newBuilder()
        .setTargetBlockId(target)
        .setStyle(type.type())
        .setFields(fields.fields())
        .build()
}

fun BlockEntity.Content.Link.Type.type() : LinkType = when(this) {
    BlockEntity.Content.Link.Type.ARCHIVE -> LinkType.Archive
    BlockEntity.Content.Link.Type.DASHBOARD -> LinkType.Dashboard
    BlockEntity.Content.Link.Type.DATA_VIEW -> LinkType.Dataview
    BlockEntity.Content.Link.Type.PAGE -> LinkType.Page
}

//endregion

//region layout mapping

fun BlockEntity.Content.Layout.layout() : Layout {
    val builder = Layout.newBuilder()
    when(type) {
        BlockEntity.Content.Layout.Type.ROW -> builder.style = LayoutStyle.Row
        BlockEntity.Content.Layout.Type.COLUMN -> builder.style = LayoutStyle.Column
        BlockEntity.Content.Layout.Type.DIV -> builder.style = LayoutStyle.Div
    }
    return builder.build()
}

//endregion

//region divider mapping

fun BlockEntity.Content.Divider.divider() : Divider {
    return Divider.newBuilder().setStyle(DividerStyle.Line).build()
}

//endregion

//region other mapping

fun BlockEntity.Fields.fields() : Struct {
    val builder = Struct.newBuilder()
    map.forEach { (key, value) ->
        if (key != null && value != null)
            when(value) {
                is String -> {
                    builder.putFields(key, Value.newBuilder().setStringValue(value).build())
                }
                is Boolean -> {
                    builder.putFields(key, Value.newBuilder().setBoolValue(value).build())
                }
                is Double-> {
                    builder.putFields(key, Value.newBuilder().setNumberValue(value).build())
                }
                else -> throw IllegalStateException("Unexpected value type: ${value::class.java}")
            }
    }
    return builder.build()
}

fun IntRange.range(): Range = Range.newBuilder().setFrom(first).setTo(last).build()

//endregion