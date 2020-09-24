package com.anytypeio.anytype.domain.auth.model

import com.anytypeio.anytype.domain.common.Url

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