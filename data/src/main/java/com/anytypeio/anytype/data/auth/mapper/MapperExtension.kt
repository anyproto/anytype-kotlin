package com.anytypeio.anytype.data.auth.mapper

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.data.auth.model.AccountEntity
import com.anytypeio.anytype.data.auth.model.WalletEntity
import com.anytypeio.anytype.domain.auth.model.Wallet

fun AccountEntity.toDomain(): Account {
    return Account(
        id = id
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id
    )
}

fun WalletEntity.toDomain(): Wallet {
    return Wallet(
        mnemonic = mnemonic
    )
}