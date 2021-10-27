package com.anytypeio.anytype.data.auth.mapper

import com.anytypeio.anytype.data.auth.model.AccountEntity
import com.anytypeio.anytype.data.auth.model.FlavourConfigEntity
import com.anytypeio.anytype.data.auth.model.WalletEntity
import com.anytypeio.anytype.domain.auth.model.Account
import com.anytypeio.anytype.domain.auth.model.Wallet
import com.anytypeio.anytype.core_models.FlavourConfig

fun AccountEntity.toDomain(): Account {
    return Account(
        id = id,
        name = name,
        color = color,
        avatar = null
    )
}

fun FlavourConfigEntity.toDomain(): FlavourConfig {
    return FlavourConfig(
        enableDataView = enableDataView,
        enableDebug = enableDebug,
        enableChannelSwitch = enableChannelSwitch,
        enableSpaces = enableSpaces
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        color = color
    )
}

fun WalletEntity.toDomain(): Wallet {
    return Wallet(
        mnemonic = mnemonic
    )
}