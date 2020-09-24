package com.anytypeio.anytype.middleware.model

import anytype.model.Models.Account.Avatar

class SelectAccountResponse(
    val id: String,
    val name: String,
    val avatar: Avatar
)