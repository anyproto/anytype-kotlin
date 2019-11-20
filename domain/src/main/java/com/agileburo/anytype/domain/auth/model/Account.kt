package com.agileburo.anytype.domain.auth.model

/**
 * User account.
 * @property id account's id
 * @property name account's name
 * @property avatar optional image
 * @property color optional color (for avatar placeholder)
 */
data class Account(
    val id: String,
    val name: String,
    val avatar: Image?,
    val color: String?
)