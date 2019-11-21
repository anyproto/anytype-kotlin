package com.agileburo.anytype.data.auth.model

data class BlockEntity(
    val id: String,
    val children: List<String>,
    val fields: Fields
) {
    data class Fields(val map: MutableMap<String, Any> = mutableMapOf())
}