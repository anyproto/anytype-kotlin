package com.agileburo.anytype.data.auth.mapper

import com.agileburo.anytype.data.auth.model.*
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.model.Image
import com.agileburo.anytype.domain.auth.model.Wallet
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.config.Config
import com.agileburo.anytype.domain.event.model.Event

fun AccountEntity.toDomain(): Account {
    return Account(
        id = id,
        name = name,
        avatar = avatar?.toDomain(),
        color = color
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        avatar = avatar?.toEntity(),
        color = color
    )
}

fun WalletEntity.toDomain(): Wallet {
    return Wallet(
        mnemonic = mnemonic
    )
}

fun ImageEntity.toDomain(): Image {
    return Image(
        id = id,
        sizes = sizes.map { it.toDomain() }
    )
}

fun Image.toEntity(): ImageEntity {
    return ImageEntity(
        id = id,
        sizes = sizes.map { it.toEntity() }
    )
}

fun ImageEntity.Size.toDomain(): Image.Size {
    return when (this) {
        ImageEntity.Size.THUMB -> Image.Size.THUMB
        ImageEntity.Size.LARGE -> Image.Size.LARGE
        ImageEntity.Size.SMALL -> Image.Size.SMALL
    }
}

fun Image.Size.toEntity(): ImageEntity.Size {
    return when (this) {
        Image.Size.THUMB -> ImageEntity.Size.THUMB
        Image.Size.SMALL -> ImageEntity.Size.SMALL
        Image.Size.LARGE -> ImageEntity.Size.LARGE
    }
}

fun BlockEntity.toDomain(): Block {
    return Block(
        id = id,
        children = children,
        fields = Block.Fields(map = fields.map.toMap()),
        content = content.toDomain()
    )
}

fun BlockEntity.Content.toDomain(): Block.Content = when (this) {
    is BlockEntity.Content.Text -> toDomain()
    is BlockEntity.Content.Dashboard -> toDomain()
    is BlockEntity.Content.Page -> toDomain()
    is BlockEntity.Content.Layout -> toDomain()
    is BlockEntity.Content.Image -> toDomain()
    is BlockEntity.Content.Link -> toDomain()
}

fun BlockEntity.Content.Text.toDomain(): Block.Content.Text {
    return Block.Content.Text(
        text = text,
        marks = marks.map { it.toDomain() },
        style = Block.Content.Text.Style.valueOf(style.name),
        isChecked = isChecked,
        color = color
    )
}

fun BlockEntity.Content.Dashboard.toDomain(): Block.Content.Dashboard {
    return Block.Content.Dashboard(
        type = Block.Content.Dashboard.Type.valueOf(type.name)
    )
}

fun BlockEntity.Content.Page.toDomain(): Block.Content.Page {
    return Block.Content.Page(
        style = Block.Content.Page.Style.valueOf(style.name)
    )
}

fun BlockEntity.Content.Link.toDomain(): Block.Content.Link {
    return Block.Content.Link(
        target = target,
        isArchived = isArchived,
        type = Block.Content.Link.Type.valueOf(type.name),
        fields = Block.Fields(map = fields.map.toMap())
    )
}

fun BlockEntity.Content.Layout.toDomain(): Block.Content.Layout {
    return Block.Content.Layout(
        type = Block.Content.Layout.Type.valueOf(type.name)
    )
}

fun Block.Content.Layout.toEntity(): BlockEntity.Content.Layout {
    return BlockEntity.Content.Layout(
        type = BlockEntity.Content.Layout.Type.valueOf(type.name)
    )
}

fun BlockEntity.Content.Image.toDomain(): Block.Content.Image {
    return Block.Content.Image(
        path = path
    )
}

fun Block.Content.Image.toEntity(): BlockEntity.Content.Image {
    return BlockEntity.Content.Image(
        path = path
    )
}

fun BlockEntity.Content.Text.Mark.toDomain(): Block.Content.Text.Mark {
    return Block.Content.Text.Mark(
        range = range,
        param = param,
        type = Block.Content.Text.Mark.Type.valueOf(type.name)
    )
}

fun Block.toEntity(): BlockEntity {
    return BlockEntity(
        id = id,
        children = children,
        fields = BlockEntity.Fields(map = fields.map.toMutableMap()),
        content = content.toEntity()
    )
}

fun Block.Content.toEntity(): BlockEntity.Content = when (this) {
    is Block.Content.Text -> toEntity()
    is Block.Content.Dashboard -> toEntity()
    is Block.Content.Page -> toEntity()
    is Block.Content.Layout -> toEntity()
    is Block.Content.Image -> toEntity()
    is Block.Content.Link -> toEntity()
}

fun Block.Content.Text.toEntity(): BlockEntity.Content.Text {
    return BlockEntity.Content.Text(
        text = text,
        marks = marks.map { it.toEntity() },
        style = BlockEntity.Content.Text.Style.valueOf(style.name)
    )
}

fun Block.Content.Dashboard.toEntity(): BlockEntity.Content.Dashboard {
    return BlockEntity.Content.Dashboard(
        type = BlockEntity.Content.Dashboard.Type.valueOf(type.name)
    )
}

fun Block.Content.Page.toEntity(): BlockEntity.Content.Page {
    return BlockEntity.Content.Page(
        style = BlockEntity.Content.Page.Style.valueOf(style.name)
    )
}

fun Block.Content.Link.toEntity(): BlockEntity.Content.Link {
    return BlockEntity.Content.Link(
        target = target,
        type = BlockEntity.Content.Link.Type.valueOf(type.name),
        isArchived = isArchived,
        fields = BlockEntity.Fields(map = fields.map.toMutableMap())
    )
}

fun Block.Content.Text.Mark.toEntity(): BlockEntity.Content.Text.Mark {
    return BlockEntity.Content.Text.Mark(
        range = range,
        param = param,
        type = BlockEntity.Content.Text.Mark.Type.valueOf(type.name)
    )
}

fun ConfigEntity.toDomain(): Config {
    return Config(
        home = homeId
    )
}

fun Command.UpdateText.toEntity(): CommandEntity.UpdateText {
    return CommandEntity.UpdateText(
        contextId = contextId,
        blockId = blockId,
        text = text,
        marks = marks.map { it.toEntity() }
    )
}

fun Command.UpdateStyle.toEntity(): CommandEntity.UpdateStyle = CommandEntity.UpdateStyle(
    context = context,
    target = target,
    style = BlockEntity.Content.Text.Style.valueOf(style.name)
)

fun Command.UpdateTextColor.toEntity(): CommandEntity.UpdateTextColor =
    CommandEntity.UpdateTextColor(
        context = context,
        target = target,
        color = color
    )

fun Command.UpdateCheckbox.toEntity(): CommandEntity.UpdateCheckbox = CommandEntity.UpdateCheckbox(
    context = context,
    target = target,
    isChecked = isChecked
)

fun Command.Create.toEntity(): CommandEntity.Create {
    return CommandEntity.Create(
        contextId = contextId,
        targetId = targetId,
        prototype = prototype.toEntity(),
        position = position.toEntity()
    )
}

fun Command.Dnd.toEntity(): CommandEntity.Dnd {
    return CommandEntity.Dnd(
        contextId = contextId,
        dropTargetId = targetId,
        dropTargetContextId = targetContextId,
        blockIds = blockIds,
        position = position.toEntity()
    )
}

fun Command.Unlink.toEntity(): CommandEntity.Unlink = CommandEntity.Unlink(
    context = context,
    targets = targets
)

fun Command.Duplicate.toEntity(): CommandEntity.Duplicate = CommandEntity.Duplicate(
    context = context,
    original = original
)

fun Command.Merge.toEntity(): CommandEntity.Merge = CommandEntity.Merge(
    context = context,
    pair = pair
)

fun Position.toEntity(): PositionEntity {
    return PositionEntity.valueOf(name)
}

fun EventEntity.toDomain(): Event {
    return when (this) {
        is EventEntity.Command.ShowBlock -> {
            Event.Command.ShowBlock(
                rootId = rootId,
                blocks = blocks.map { it.toDomain() },
                context = context
            )
        }
        is EventEntity.Command.AddBlock -> {
            Event.Command.AddBlock(
                blocks = blocks.map { it.toDomain() },
                context = context
            )
        }
        is EventEntity.Command.UpdateBlockText -> {
            Event.Command.UpdateBlockText(
                id = id,
                text = text,
                context = context
            )
        }
        is EventEntity.Command.UpdateStructure -> {
            Event.Command.UpdateStructure(
                context = context,
                id = id,
                children = children
            )
        }
        is EventEntity.Command.DeleteBlock -> {
            Event.Command.DeleteBlock(
                context = context,
                target = target
            )
        }
        is EventEntity.Command.GranularChange -> {
            Event.Command.GranularChange(
                context = context,
                id = id,
                text = text,
                style = if (style != null)
                    Block.Content.Text.Style.valueOf(style.name)
                else
                    null,
                color = color,
                marks = marks?.map { it.toDomain() }
            )
        }
        is EventEntity.Command.LinkGranularChange -> {
            Event.Command.LinkGranularChange(
                context = context,
                id = id,
                target = target,
                fields = fields?.let { Block.Fields(it.map) }
            )
        }
    }
}

fun Block.Prototype.toEntity(): BlockEntity.Prototype = when (this) {
    is Block.Prototype.Text -> {
        BlockEntity.Prototype.Text(
            style = BlockEntity.Content.Text.Style.valueOf(this.style.name)
        )
    }
}