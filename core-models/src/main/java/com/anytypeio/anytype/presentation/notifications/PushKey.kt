package com.anytypeio.anytype.presentation.notifications

data class PushKey(
    val id: String,
    val value: String
) {
    companion object {
        val EMPTY = PushKey(value = "", id = "")
    }
}