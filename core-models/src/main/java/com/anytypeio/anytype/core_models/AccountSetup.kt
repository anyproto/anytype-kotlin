package com.anytypeio.anytype.core_models

data class AccountSetup(
    val account: Account,
    val config: Config,
    val features: FeaturesConfig,
    val status: AccountStatus
)