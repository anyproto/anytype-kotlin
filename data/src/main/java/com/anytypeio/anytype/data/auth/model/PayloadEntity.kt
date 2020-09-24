package com.anytypeio.anytype.data.auth.model

data class PayloadEntity(
    val context: String,
    val events: List<EventEntity>
)