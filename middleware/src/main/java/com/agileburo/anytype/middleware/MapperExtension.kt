package com.agileburo.anytype.middleware

import anytype.Events
import anytype.model.Models
import anytype.model.Models.Account
import anytype.model.Models.Block
import com.agileburo.anytype.data.auth.model.AccountEntity
import com.agileburo.anytype.data.auth.model.BlockEntity
import com.agileburo.anytype.data.auth.model.ImageEntity
import com.google.protobuf.Value

fun Models.Image.toEntity(): ImageEntity? {
    return if (id.isNullOrBlank())
        null
    else
        ImageEntity(
            id = id,
            sizes = sizesList.map { size -> size.toEntity() }
        )
}

fun ImageEntity.Size.toMiddleware(): Models.Image.Size = when (this) {
    ImageEntity.Size.SMALL -> Models.Image.Size.Small
    ImageEntity.Size.LARGE -> Models.Image.Size.Large
    ImageEntity.Size.THUMB -> Models.Image.Size.Thumb
}

fun Models.Image.Size.toEntity(): ImageEntity.Size = when (this) {
    Models.Image.Size.Small -> ImageEntity.Size.SMALL
    Models.Image.Size.Large -> ImageEntity.Size.LARGE
    Models.Image.Size.Thumb -> ImageEntity.Size.THUMB
    else -> throw IllegalStateException("Unexpected image size from middleware")
}

fun Events.Event.Account.Show.toAccountEntity(): AccountEntity {
    return AccountEntity(
        id = account.id,
        name = account.name,
        avatar = if (account.avatar.avatarCase == Account.Avatar.AvatarCase.IMAGE)
            account.avatar.image.toEntity()
        else null,
        color = if (account.avatar.avatarCase == Account.Avatar.AvatarCase.COLOR)
            account.avatar.color else null
    )
}

fun BlockEntity.Content.Text.Mark.toMiddleware(): Block.Content.Text.Mark {
    val rangeModel = Models.Range.newBuilder()
        .setFrom(range.first)
        .setTo(range.last)
        .build()

    return when (type) {
        BlockEntity.Content.Text.Mark.Type.BOLD -> {
            Block.Content.Text.Mark
                .newBuilder()
                .setType(Block.Content.Text.Mark.Type.Bold)
                .setRange(rangeModel)
                .build()
        }
        BlockEntity.Content.Text.Mark.Type.ITALIC -> {
            Block.Content.Text.Mark
                .newBuilder()
                .setType(Block.Content.Text.Mark.Type.Italic)
                .setRange(rangeModel)
                .build()
        }
        BlockEntity.Content.Text.Mark.Type.STRIKETHROUGH -> {
            Block.Content.Text.Mark
                .newBuilder()
                .setType(Block.Content.Text.Mark.Type.Strikethrough)
                .setRange(rangeModel)
                .build()
        }
        BlockEntity.Content.Text.Mark.Type.TEXT_COLOR -> {
            Block.Content.Text.Mark
                .newBuilder()
                .setType(Block.Content.Text.Mark.Type.TextColor)
                .setRange(rangeModel)
                .setParam(param as String)
                .build()
        }
        else -> throw IllegalStateException("Unsupported mark type: ${type.name}")
    }
}

fun Block.fields(): BlockEntity.Fields = BlockEntity.Fields().also { result ->
    fields.fieldsMap.forEach { (key, value) ->
        result.map[key] = when (val case = value.kindCase) {
            Value.KindCase.NUMBER_VALUE -> value.numberValue
            Value.KindCase.STRING_VALUE -> value.stringValue
            else -> throw IllegalStateException("$case is not supported.")
        }
    }
}

fun Block.dashboard(): BlockEntity.Content.Dashboard = BlockEntity.Content.Dashboard(
    type = when {
        dashboard.style == Block.Content.Dashboard.Style.Archive -> {
            BlockEntity.Content.Dashboard.Type.ARCHIVE
        }
        dashboard.style == Block.Content.Dashboard.Style.MainScreen -> {
            BlockEntity.Content.Dashboard.Type.MAIN_SCREEN
        }
        else -> throw IllegalStateException("Unexpected dashboard style: ${dashboard.style}")
    }
)

fun Block.page(): BlockEntity.Content.Page = BlockEntity.Content.Page(
    style = when {
        page.style == Block.Content.Page.Style.Empty -> {
            BlockEntity.Content.Page.Style.EMPTY
        }
        page.style == Block.Content.Page.Style.Task -> {
            BlockEntity.Content.Page.Style.TASK
        }
        page.style == Block.Content.Page.Style.Set -> {
            BlockEntity.Content.Page.Style.SET
        }
        else -> throw IllegalStateException("Unexpected page style: ${page.style}")
    }
)

fun Block.text(): BlockEntity.Content.Text = BlockEntity.Content.Text(
    text = text.text,
    marks = text.marks.marksList.map { mark ->
        BlockEntity.Content.Text.Mark(
            range = IntRange(mark.range.from, mark.range.to),
            param = if (mark.param.isNotEmpty()) mark.param else null,
            type = when (mark.type) {
                Block.Content.Text.Mark.Type.Bold -> {
                    BlockEntity.Content.Text.Mark.Type.BOLD
                }
                Block.Content.Text.Mark.Type.Italic -> {
                    BlockEntity.Content.Text.Mark.Type.ITALIC
                }
                Block.Content.Text.Mark.Type.Strikethrough -> {
                    BlockEntity.Content.Text.Mark.Type.STRIKETHROUGH
                }
                Block.Content.Text.Mark.Type.Underscored -> {
                    BlockEntity.Content.Text.Mark.Type.UNDERSCORED
                }
                Block.Content.Text.Mark.Type.Keyboard -> {
                    BlockEntity.Content.Text.Mark.Type.KEYBOARD
                }
                Block.Content.Text.Mark.Type.TextColor -> {
                    BlockEntity.Content.Text.Mark.Type.TEXT_COLOR
                }
                Block.Content.Text.Mark.Type.BackgroundColor -> {
                    BlockEntity.Content.Text.Mark.Type.BACKGROUND_COLOR
                }
                else -> throw IllegalStateException("Unexpected mark type: ${mark.type.name}")
            }
        )
    },
    style = text.style.entity(),
    isChecked = text.checked,
    color = if (text.color.isNotEmpty()) text.color else null
)

fun Block.layout(): BlockEntity.Content.Layout = BlockEntity.Content.Layout(
    type = when {
        layout.style == Block.Content.Layout.Style.Column -> {
            BlockEntity.Content.Layout.Type.COLUMN
        }
        layout.style == Block.Content.Layout.Style.Row -> {
            BlockEntity.Content.Layout.Type.ROW
        }
        else -> throw IllegalStateException("Unexpected layout style: ${layout.style}")
    }
)

fun List<Block>.blocks(): List<BlockEntity> = mapNotNull { block ->
    when (block.contentCase) {
        Block.ContentCase.DASHBOARD -> {
            BlockEntity(
                id = block.id,
                children = block.childrenIdsList.toList(),
                fields = block.fields(),
                content = block.dashboard()
            )
        }
        Block.ContentCase.PAGE -> {
            BlockEntity(
                id = block.id,
                children = block.childrenIdsList.toList(),
                fields = block.fields(),
                content = block.page()
            )
        }
        Block.ContentCase.TEXT -> {
            BlockEntity(
                id = block.id,
                children = block.childrenIdsList.toList(),
                fields = block.fields(),
                content = block.text()
            )
        }
        Block.ContentCase.LAYOUT -> {
            BlockEntity(
                id = block.id,
                children = block.childrenIdsList,
                fields = block.fields(),
                content = block.layout()
            )
        }
        /*
        Models.Block.ContentCase.IMAGE -> {
            BlockEntity(
                id = block.id,
                children = block.childrenIdsList,
                fields = extractFields(block),
                content = BlockEntity.Content.Image(
                    path = block.image.localFilePath
                )
            )
        }
         */
        else -> {
            null
        }
    }
}

fun Block.Content.Text.Style.entity(): BlockEntity.Content.Text.Style = when (this) {
    Block.Content.Text.Style.Paragraph -> BlockEntity.Content.Text.Style.P
    Block.Content.Text.Style.Header1 -> BlockEntity.Content.Text.Style.H1
    Block.Content.Text.Style.Header2 -> BlockEntity.Content.Text.Style.H2
    Block.Content.Text.Style.Header3 -> BlockEntity.Content.Text.Style.H3
    Block.Content.Text.Style.Title -> BlockEntity.Content.Text.Style.TITLE
    Block.Content.Text.Style.Quote -> BlockEntity.Content.Text.Style.QUOTE
    Block.Content.Text.Style.Marked -> BlockEntity.Content.Text.Style.BULLET
    Block.Content.Text.Style.Numbered -> BlockEntity.Content.Text.Style.NUMBERED
    Block.Content.Text.Style.Toggle -> BlockEntity.Content.Text.Style.TOGGLE
    Block.Content.Text.Style.Checkbox -> BlockEntity.Content.Text.Style.CHECKBOX
    else -> throw IllegalStateException("Unexpected text style: $this")
}