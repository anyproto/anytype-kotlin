package com.anytypeio.anytype.middleware.model

import anytype.model.Account


class SelectAccountResponse(
    val id: String,
    val name: String,
    val avatar: Account.Avatar?,
    val enableDataView: Boolean?,
    val enableDebug: Boolean?,
    val enableChannelSwitch: Boolean?
)