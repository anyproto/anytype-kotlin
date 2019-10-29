package com.agileburo.anytype.data.auth.mapper

import com.agileburo.anytype.data.auth.model.AccountEntity
import com.agileburo.anytype.data.auth.model.ImageEntity
import com.agileburo.anytype.data.auth.model.WalletEntity
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.model.Image
import com.agileburo.anytype.domain.auth.model.Wallet

fun AccountEntity.toDomain(): Account {
    return Account(
        id = id,
        name = name,
        avatar = avatar?.toDomain()
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        avatar = avatar?.toEntity()
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