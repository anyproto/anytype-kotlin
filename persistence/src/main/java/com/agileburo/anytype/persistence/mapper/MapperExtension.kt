package com.agileburo.anytype.persistence.mapper

import com.agileburo.anytype.data.auth.model.AccountEntity
import com.agileburo.anytype.data.auth.model.ImageEntity
import com.agileburo.anytype.persistence.model.AccountTable

fun AccountTable.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        avatar = avatar?.let { cached ->
            ImageEntity(
                id = cached.avatarId,
                sizes = cached.sizes.map { it.toEntity() }
            )
        },
        color = color
    )
}

fun AccountEntity.toTable(): AccountTable {
    return AccountTable(
        id = id,
        name = name,
        timestamp = System.currentTimeMillis(),
        avatar = avatar?.let { avatar ->
            AccountTable.Avatar(
                avatarId = avatar.id,
                sizes = avatar.sizes.map { it.toTable() }
            )
        }
    )
}

fun ImageEntity.Size.toTable(): AccountTable.Size = when (this) {
    ImageEntity.Size.SMALL -> AccountTable.Size.SMALL
    ImageEntity.Size.THUMB -> AccountTable.Size.THUMB
    ImageEntity.Size.LARGE -> AccountTable.Size.LARGE
}

fun AccountTable.Size.toEntity(): ImageEntity.Size = when (this) {
    AccountTable.Size.SMALL -> ImageEntity.Size.SMALL
    AccountTable.Size.LARGE -> ImageEntity.Size.LARGE
    AccountTable.Size.THUMB -> ImageEntity.Size.THUMB
}