package com.anytypeio.anytype.middleware.model

import anytype.model.Account.Avatar

class CreateAccountResponse(
    val id: String,
    val name: String,
    val avatar: Avatar? = null
)