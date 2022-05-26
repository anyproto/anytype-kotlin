package com.anytypeio.anytype.core_models

/**
 * User account.
 * @property id account's id
 * @property name account's name
 * @property avatar optional avatar url
 * @property color optional color (for avatar placeholder)
 */
data class Account(
    val id: String,
    val name: String,
    val avatar: Url?,
    val color: String?
)