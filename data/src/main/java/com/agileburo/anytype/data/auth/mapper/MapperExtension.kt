package com.agileburo.anytype.data.auth.mapper

import com.agileburo.anytype.data.auth.model.AccountEntity
import com.agileburo.anytype.data.auth.model.BlockEntity
import com.agileburo.anytype.data.auth.model.ImageEntity
import com.agileburo.anytype.data.auth.model.WalletEntity
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.model.Image
import com.agileburo.anytype.domain.auth.model.Wallet
import com.agileburo.anytype.domain.block.model.Block

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

fun BlockEntity.Content.Text.Mark.toDomain(): Block.Content.Text.Mark {
    return Block.Content.Text.Mark(
        range = range,
        param = param,
        type = Block.Content.Text.Mark.Type.valueOf(type.name)
    )
}