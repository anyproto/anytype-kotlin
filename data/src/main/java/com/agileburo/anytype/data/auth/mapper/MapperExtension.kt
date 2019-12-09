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
    is BlockEntity.Content.Text -> this.toDomain()
    is BlockEntity.Content.Dashboard -> this.toDomain()
    is BlockEntity.Content.Page -> this.toDomain()
}

fun BlockEntity.Content.Text.toDomain(): Block.Content.Text {
    return Block.Content.Text(
        text = text,
        marks = marks.map { it.toDomain() },
        style = Block.Content.Text.Style.valueOf(style.name)
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
    is Block.Content.Text -> this.toEntity()
    is Block.Content.Dashboard -> this.toEntity()
    is Block.Content.Page -> this.toEntity()
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

fun Block.Content.Text.Mark.toEntity(): BlockEntity.Content.Text.Mark {
    return BlockEntity.Content.Text.Mark(
        range = range,
        param = param,
        type = BlockEntity.Content.Text.Mark.Type.valueOf(type.name)
    )
}

fun ConfigEntity.toDomain(): Config {
    return Config(
        homeId = homeId
    )
}

fun Command.Update.toEntity(): CommandEntity.Update {
    return CommandEntity.Update(
        contextId = contextId,
        blockId = blockId,
        text = text
    )
}

fun Command.Create.toEntity(): CommandEntity.Create {
    return CommandEntity.Create(
        contextId = contextId,
        targetId = targetId,
        block = block.toEntity(),
        position = position.toEntity()
    )
}

fun Position.toEntity(): PositionEntity {
    return PositionEntity.valueOf(name)
}

fun EventEntity.toDomain(): Event {
    return when (this) {
        is EventEntity.Command.ShowBlock -> {
            Event.Command.ShowBlock(
                rootId = rootId,
                blocks = blocks.map { it.toDomain() }
            )
        }
        is EventEntity.Command.AddBlock -> {
            Event.Command.AddBlock(
                blocks = blocks.map { it.toDomain() }
            )
        }
        is EventEntity.Command.UpdateBlockText -> {
            Event.Command.UpdateBlockText(
                id = id,
                text = text
            )
        }
    }
}