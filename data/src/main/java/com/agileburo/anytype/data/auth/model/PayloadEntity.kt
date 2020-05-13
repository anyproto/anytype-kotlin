package com.agileburo.anytype.data.auth.model

data class PayloadEntity(
    val context: String,
    val events: List<EventEntity>
)