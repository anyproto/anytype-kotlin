package com.agileburo.anytype.middleware

import anytype.Events
import anytype.model.Models
import anytype.model.Models.Account
import com.agileburo.anytype.data.auth.model.AccountEntity
import com.agileburo.anytype.data.auth.model.BlockEntity
import com.agileburo.anytype.data.auth.model.ImageEntity

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

fun BlockEntity.Content.Text.Mark.toMiddleware(): Models.Block.Content.Text.Mark {
    val rangeModel = Models.Range.newBuilder()
        .setFrom(range.first)
        .setTo(range.last)
        .build()

    return when (type) {
        BlockEntity.Content.Text.Mark.Type.BOLD -> {
            Models.Block.Content.Text.Mark
                .newBuilder()
                .setType(Models.Block.Content.Text.Mark.Type.Bold)
                .setRange(rangeModel)
                .build()
        }
        BlockEntity.Content.Text.Mark.Type.ITALIC -> {
            Models.Block.Content.Text.Mark
                .newBuilder()
                .setType(Models.Block.Content.Text.Mark.Type.Italic)
                .setRange(rangeModel)
                .build()
        }
        BlockEntity.Content.Text.Mark.Type.STRIKETHROUGH -> {
            Models.Block.Content.Text.Mark
                .newBuilder()
                .setType(Models.Block.Content.Text.Mark.Type.Strikethrough)
                .setRange(rangeModel)
                .build()
        }
        else -> throw IllegalStateException("Unsupported mark type: ${type.name}")
    }
}