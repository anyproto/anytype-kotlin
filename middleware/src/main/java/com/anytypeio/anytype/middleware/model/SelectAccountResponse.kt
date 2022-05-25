package com.anytypeio.anytype.middleware.model

import anytype.model.Account
import com.anytypeio.anytype.core_models.AccountStatus


class SelectAccountResponse(
    val id: String,
    val name: String,
    val avatar: Account.Avatar?,
    val enableDataView: Boolean?,
    val enableDebug: Boolean?,
    val enableChannelSwitch: Boolean?,
    val enableSpaces: Boolean?,
    val accountStatus: AccountStatus?
)