package com.agileburo.anytype.data.auth.model

data class AccountEntity(
    val id: String,
    val name: String,
    val avatar: ImageEntity?,
    val color: String?
)