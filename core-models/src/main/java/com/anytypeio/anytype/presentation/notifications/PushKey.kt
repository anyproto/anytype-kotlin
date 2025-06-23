package com.anytypeio.anytype.presentation.notifications

import kotlinx.serialization.Serializable

@Serializable
data class PushKey(
    val id: String,
    val value: String
) {
    companion object {
        val EMPTY = PushKey(value = "", id = "")
    }
}